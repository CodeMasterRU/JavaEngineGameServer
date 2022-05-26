package com.comflip.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClusterSockets {
    private ServerSocket serverSocket;
    private SQL sqlserver;

    public ServerClusterSockets(SQL sqlserver) {
        this.sqlserver = sqlserver;
    }

    public void start(int port) throws Exception {
        serverSocket = new ServerSocket(port);

        while (true) {
            new ClientHandler(serverSocket.accept(), this.sqlserver).start();
        }
    }

    public void stop() throws Exception {
        serverSocket.close();
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        private SQL sqlserver;

        public ClientHandler(Socket clientSocket, SQL sqlserver) {
            this.clientSocket = clientSocket;
            this.sqlserver = sqlserver;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("new".equals(inputLine)) {
                        this.sqlserver.openConnection().createStatement().execute("INSERT INTO lobby (username, password) VALUES (\"Huy\", \"kaka\")");

                        out.println("Done!");
                    }
                    out.println(inputLine);
                }

                in.close();
                out.close();
                clientSocket.close();

            } catch (Exception ignored) {
                System.out.println(ignored);
            }


        }
    }

    public void update() {

    }
}
