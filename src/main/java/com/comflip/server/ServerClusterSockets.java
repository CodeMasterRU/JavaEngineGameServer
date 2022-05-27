package com.comflip.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ServerClusterSockets {
    private ServerSocket serverSocket;
    private final SQL sqlserver;

    public ServerClusterSockets(SQL sqlserver) {
        this.sqlserver = sqlserver;
    }

    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        System.out.println("\n" + this.getClass().getSimpleName() + ": Listening on port " + port);

        while (!serverSocket.isClosed()) {
            new ClientHandler(serverSocket.accept(), this.sqlserver).start();
        }
    }

    public void stop() throws Exception {
        System.out.println("\n" + this.getClass().getSimpleName() + ": Closing socket...");
        serverSocket.close();
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        private final SQL sqlserver;

        public ClientHandler(Socket clientSocket, SQL sqlserver) {
            this.clientSocket = clientSocket;
            this.sqlserver = sqlserver;
        }

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

                            String insertUser = "INSERT INTO user (username, password) VALUES ?, ?";

                            try (PreparedStatement addUser = con.prepareStatement(insertUser)){
                                addUser.setString(1,username);
                                addUser.setString(2,password);
                                addUser.executeUpdate();

                                out.write("Account is created!");
                                out.newLine();
                                out.flush();
                            } catch (SQLException e){
                                System.out.println("Error! " + e);
                            }
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
