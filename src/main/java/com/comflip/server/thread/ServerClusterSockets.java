package com.comflip.server.thread;

import com.comflip.server.SQL;
import com.comflip.server.ServerContainer;
import com.comflip.server.model.User;
import com.comflip.server.security.Hash;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class ServerClusterSockets implements Runnable {
    private final ServerContainer serverContainer;

    private ServerSocket serverSocket;

    public ServerClusterSockets(ServerContainer serverContainer) throws IOException {
        this.serverContainer = serverContainer;
    }

    @Override
    public void run() {
        while (serverContainer.isRunning()) {
            try {
                if (CommandLine.statucCodeSocket() == 2 && serverContainer.sqlserver.isInit()) {
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
            new ClientHandler(serverSocket.accept(), serverContainer.sqlserver).run();

            if (CommandLine.statucCodeSocket() == 1 || !serverContainer.sqlserver.isInit()) {
                this.stop();
            }
        }
    }

    public void stop() throws Exception {
        System.out.println("\b\b" + this.getClass().getSimpleName() + ": Closing socket...");
        serverSocket.close();
    }


    private record ClientHandler(Socket clientSocket, SQL sqlserver) implements Runnable {
        @Override
        public void run() {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                String[] splitInputLine;
                while ((inputLine = in.readLine()) != null) {
                    splitInputLine = inputLine.split("=");

                    switch (splitInputLine[0]) {
                        case "create-account" -> {
                            String username = splitInputLine[1].split(":")[0];
                            String password = splitInputLine[1].split(":")[1];

                            Connection con = this.sqlserver.openConnection();

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

                            Connection con = this.sqlserver.openConnection();

                            String selectUser = "SELECT * FROM user WHERE username = ?";

                            try (PreparedStatement selectStmt = con.prepareStatement(selectUser)) {
                                selectStmt.setString(1, username);
                                ResultSet rep = selectStmt.executeQuery();

                                if (rep.next() && Hash.validate(password, rep.getString("password"))) {
                                    User user = new User(rep);

                                    boolean notSession = true;
                                    while (notSession) {
                                        try {
                                            user.setSessionID();

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

                            Connection con = this.sqlserver.openConnection();
                            String updateUser = "UPDATE user SET sessionID = null, online = 0 WHERE username = '" + username + "'";

                            PreparedStatement updateStmt = con.prepareStatement(updateUser);
                            updateStmt.executeUpdate();

                            out.write("");
                            out.newLine();
                            out.flush();

                            con.close();
                        }

                        case "create-match" -> {
                            String idMatch = splitInputLine[1];

                            Connection con = this.sqlserver.openConnection();

                            String insertMatch = "INSERT INTO lobby (idMatch) VALUES (?)";

                            try (PreparedStatement stmt = con.prepareStatement(insertMatch)) {
                                stmt.setString(1, idMatch);
                                stmt.executeUpdate();

                                out.write("");
                                out.newLine();
                                out.flush();
                            } catch (Exception ignored) {
                            }

                            con.close();
                        }

                        case "lobby" -> {
                            Connection con = this.sqlserver.openConnection();

                            String selectMatch = "SELECT * FROM lobby";

                            try (PreparedStatement stmt = con.prepareStatement(selectMatch)) {
                                ResultSet resultSet = stmt.executeQuery();
                                StringBuilder response = new StringBuilder();

                                while (resultSet.next()) {
                                    response.append("row").append(resultSet.getRow()).append("=").append(resultSet.getInt("id")).append(":").append(resultSet.getString("idMatch")).append("&");
                                }

                                out.write(response.toString());
                                out.newLine();
                                out.flush();
                            } catch (Exception ignored) {
                            }

                            con.close();
                        }

                        case "ping" -> {
                            String username = splitInputLine[1].split(":")[0];
                            try {
                                SessionTimer.hashSession.put(username, String.valueOf(SessionTimer.second));
                            } catch (Exception ignored) {
                            }

                            out.write("");
                            out.newLine();
                            out.flush();
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
