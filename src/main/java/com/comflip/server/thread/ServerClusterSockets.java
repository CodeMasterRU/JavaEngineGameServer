package com.comflip.server.thread;

import com.comflip.server.SQL;
import com.comflip.server.ServerContainer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                System.out.println("Error! " + e);
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

                            String insertUser = "INSERT INTO user (username, password) VALUES (?, ?)";

                            try (PreparedStatement addUser = con.prepareStatement(insertUser)) {
                                addUser.setString(1, username);
                                addUser.setString(2, password);
                                addUser.executeUpdate();

                                out.write("Account is created!");
                                out.newLine();
                                out.flush();
                            } catch (SQLException e) {
                                System.out.println("Error! " + e);

                                out.write("This user already exists");
                                out.newLine();
                                out.flush();
                            }
                            con.close();
                        }

                        case "login-account" -> {
                            String username = splitInputLine[1].split(":")[0];
                            String password = splitInputLine[1].split(":")[1];

                            Connection con = this.sqlserver.openConnection();

                            String selectUser = "SELECT * FROM user WHERE username = ? AND password = ?";

                            try (PreparedStatement addUser = con.prepareStatement(selectUser)) {
                                addUser.setString(1, username);
                                addUser.setString(2, password);

                                if (addUser.executeQuery().next()){
                                    out.write("Account is exist!");
                                } else {
                                    out.write("Wrong password or username");
                                }

                                out.newLine();
                                out.flush();
                            } catch (SQLException e) {
                                System.out.println("Error! " + e);

                                out.write("Wrong password or username");
                                out.newLine();
                                out.flush();
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
                System.out.println("Error! " + e);
            }
        }
    }
}
