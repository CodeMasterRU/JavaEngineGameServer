package com.comflip.server.thread;

import com.comflip.server.ServerContainer;
import com.comflip.server.model.User;
import com.comflip.server.security.Hash;
import com.comflip.server.util.UID;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class ServerSocketTCP implements Runnable {
    private final ServerContainer serverContainer;
    private ServerSocket serverSocket;


    public ServerSocketTCP(ServerContainer serverContainer) throws IOException {
        this.serverContainer = serverContainer;
    }

    @Override
    public void run() {
        while (serverContainer.isRunning()) {
            try {
                if (CommandLine.statusCodeSocket() == 2 && ServerContainer.sqlserver.isInit()) {
                    this.start();
                }
            } catch (Exception e) {
                System.out.println("\nError!");
                e.printStackTrace();
            }
        }

    }

    private void start() throws Exception {
        serverSocket = new ServerSocket(5555);

        System.out.println("\b\b" + this.getClass().getSimpleName() + ": Listening on port " + 5555);

        while (!serverSocket.isClosed()) {
            new ClientHandlerTCP(serverSocket.accept()).run();
            if (CommandLine.statusCodeSocket() == 1 || !ServerContainer.sqlserver.isInit()) {
                this.stop();
            }
        }
    }

    public void stop() throws Exception {
        System.out.println("\b\b" + this.getClass().getSimpleName() + ": Closing socket...");
        serverSocket.close();
    }

    private record ClientHandlerTCP(Socket clientSocket) implements Runnable {
        @Override
        public void run() {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientSocket.setSoTimeout(10000);

                String inputLine;
                String[] splitInputLine;
                while ((inputLine = in.readLine()) != null) {
                    splitInputLine = inputLine.split("=");

                    switch (splitInputLine[0]) {
                        case "create-account" -> {
                            String username = splitInputLine[1].split(":")[0];
                            String password = splitInputLine[1].split(":")[1];
                            Connection con = ServerContainer.sqlserver.openConnection();

                            String insertUser = "INSERT INTO user (username, password, creationDate) VALUES (?, ?, ?)";

                            try (PreparedStatement stmt = con.prepareStatement(insertUser)) {
                                stmt.setDate(3, new Date(new java.util.Date().getTime()));
                                stmt.setString(1, username);
                                stmt.setString(2, Hash.generateHashPassword(password));
                                stmt.executeUpdate();

                                out.write("msg=Account is created!");
                                out.newLine();
                                out.flush();
                            } catch (SQLException e) {
                                System.out.println("Error! " + e);

                                out.write("msg=This user already exists");
                                out.newLine();
                                out.flush();
                            }
                            con.close();
                        }

                        case "login-account" -> {
                            String username = splitInputLine[1].split(":")[0];
                            String password = splitInputLine[1].split(":")[1];
                            Connection con = ServerContainer.sqlserver.openConnection();

                            String selectUser = "SELECT * FROM user WHERE username = ?";

                            try (PreparedStatement selectStmt = con.prepareStatement(selectUser)) {
                                selectStmt.setString(1, username);
                                ResultSet rep = selectStmt.executeQuery();

                                if (rep.next() && Hash.validate(password, rep.getString("password"))) {
                                    User user = new User(rep);

                                    boolean notSession = true;
                                    while (notSession) {
                                        try {
                                            String updateUser = "UPDATE user SET sessionID = ?, online = 1 WHERE username = '" + username + "'";
                                            PreparedStatement updateStmt = con.prepareStatement(updateUser);

                                            updateStmt.setString(1, user.getSessionID());
                                            updateStmt.executeUpdate();
                                            notSession = false;

                                        } catch (SQLException ignored) {
                                        }
                                    }

                                    out.write("msg=Account is exist!" +
                                            "&" +
                                            "data=" + user.getUsername() + ":" + user.getSessionID() +
                                            "&" +
                                            "isAuth=true");
                                } else {
                                    out.write("msg=Wrong password or username" +
                                            "&" +
                                            "isAuth=false");
                                }

                                out.newLine();
                                out.flush();
                            } catch (SQLException e) {
                                System.out.println("\nError!");
                                e.printStackTrace();

                                out.write("msg=Wrong password or username" +
                                        "&" +
                                        "isAuth=false");
                                out.newLine();
                                out.flush();
                            }
                            con.close();
                        }

                        case "logout" -> {
                            String username = splitInputLine[1].split(":")[0];
                            Connection con = ServerContainer.sqlserver.openConnection();

                            String updateUser = "UPDATE user SET sessionID = null, online = 0 WHERE username = ?";

                            SessionTimer.hashSessionUsers.remove(username);

                            PreparedStatement updateStmt = con.prepareStatement(updateUser);
                            updateStmt.setString(1, username);
                            updateStmt.executeUpdate();

                            out.write("");
                            out.newLine();
                            out.flush();

                            con.close();
                        }

                        case "create-match" -> {
                            String hostUsername = splitInputLine[1];
                            Connection con = ServerContainer.sqlserver.openConnection();
                            String insertMatch = "INSERT INTO lobby (idMatch, hostId) VALUES (?, (SELECT id FROM user WHERE username = ?))";

                            try (PreparedStatement stmt = con.prepareStatement(insertMatch)) {
                                String idMatch = UID.setSession();

                                stmt.setString(1, idMatch);
                                stmt.setString(2, hostUsername);
                                stmt.executeUpdate();

                                out.write("idMatch=" + idMatch);
                                out.newLine();
                                out.flush();
                            } catch (Exception ignored) {
                            }
                            con.close();
                        }

                        case "cancel-match" -> {
                            String idMatch = splitInputLine[1];
                            Connection con = ServerContainer.sqlserver.openConnection();
                            String deleteMatch = "DELETE FROM lobby WHERE idMatch = ?";

                            try (PreparedStatement stmt = con.prepareStatement(deleteMatch)) {
                                stmt.setString(1, idMatch);
                                stmt.executeUpdate();

                                out.write("");
                                out.newLine();
                                out.flush();
                            } catch (Exception ignored) {
                            }
                            con.close();
                        }

                        case "isGuest" -> {
                            String idMatch = splitInputLine[1];
                            Connection con = ServerContainer.sqlserver.openConnection();
                            String deleteMatch = "SELECT guestId FROM lobby WHERE idMatch = ?";

                            try (PreparedStatement stmt = con.prepareStatement(deleteMatch)) {
                                stmt.setString(1, idMatch);
                                ResultSet resultSet = stmt.executeQuery();

                                if (resultSet.next() && resultSet.getString("guestId") == null) {
                                    out.write("guestName=null");
                                } else {
                                    String guestName = "SELECT username FROM user WHERE id = ?";

                                    try (PreparedStatement stmtName = con.prepareStatement(guestName)) {
                                        stmtName.setString(1, resultSet.getString("guestId"));
                                        ResultSet resultSetName = stmtName.executeQuery();
                                        resultSetName.next();

                                        out.write("guestName=" + resultSetName.getString("username"));
                                    } catch (Exception ignored) {
                                    }
                                }

                                out.newLine();
                                out.flush();
                            } catch (Exception ignored) {
                            }
                            con.close();
                        }

                        case "join" -> {
                            String username = splitInputLine[1].split(":")[0];
                            String idMatch = splitInputLine[1].split(":")[1];

                            Connection con = ServerContainer.sqlserver.openConnection();
                            String updateMatch = "UPDATE lobby SET guestId = (SELECT id FROM user WHERE username = ?), isOpen = 0 WHERE idMatch = ?";

                            try (PreparedStatement stmt = con.prepareStatement(updateMatch)) {
                                stmt.setString(1, username);
                                stmt.setString(2, idMatch);
                                stmt.executeUpdate();

                                String selectHost = "SELECT username FROM lobby INNER JOIN user ON lobby.hostId = user.id WHERE idMatch = ?";
                                try (PreparedStatement stmtHost = con.prepareStatement(selectHost)) {
                                    stmtHost.setString(1, idMatch);
                                    ResultSet resultSetHost = stmtHost.executeQuery();
                                    if (resultSetHost.next()) {
                                        out.write("msg=done&userHost=" + resultSetHost.getString("username"));
                                    }
                                } catch (Exception ignored) {
                                }

                                out.newLine();
                                out.flush();

                            } catch (Exception ignored) {
                            }
                            con.close();
                        }

                        default -> {
                            out.write("Error! Unknown command!");
                            out.newLine();
                            out.flush();
                        }
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("\nError!");
                e.printStackTrace();
            }
        }
    }
}
