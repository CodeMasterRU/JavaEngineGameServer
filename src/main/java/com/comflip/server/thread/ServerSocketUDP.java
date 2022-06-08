package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerSocketUDP implements Runnable {
    private final ServerContainer serverContainer;
    private DatagramSocket serverSocket;

    private static final byte[] buf = new byte[256];

    public ServerSocketUDP(ServerContainer serverContainer) throws IOException {
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
        serverSocket = new DatagramSocket(5556);

        System.out.println("\b\b" + this.getClass().getSimpleName() + ": Listening on port " + 5556);

        while (!serverSocket.isClosed()) {
            new ClientHandlerUDP(serverSocket).run();
            if (CommandLine.statusCodeSocket() == 1 || !ServerContainer.sqlserver.isInit()) {
                this.stop();
            }
        }
    }

    public void stop() throws Exception {
        System.out.println("\b\b" + this.getClass().getSimpleName() + ": Closing socket...");
        serverSocket.close();
    }

    private record ClientHandlerUDP(DatagramSocket serverSocket) implements Runnable {

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());

                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                switch (received.split("=")[0]) {
                    case "isOnline" -> {
                        String username = received.split("=")[1];
                        if (!username.equals("null")) {
                            SessionTimer.hashSession.put(username, String.valueOf(SessionTimer.second));
                        }
                        serverSocket.send(packet);
                    }

                    case "lobby" -> {
                        String selectMatch = "SELECT idMatch, username FROM lobby INNER JOIN user ON lobby.hostId = user.id WHERE isOpen = 1";
                        Connection con = ServerContainer.sqlserver.openConnection();
                        try (PreparedStatement stmt = con.prepareStatement(selectMatch)) {
                            ResultSet resultSet = stmt.executeQuery();
                            StringBuilder response = new StringBuilder();

                            while (resultSet.next()) {
                                response.append("row" + resultSet.getRow() + "=" +
                                        resultSet.getString("idMatch") + ":" +
                                        resultSet.getString("username") +
                                        "&");
                            }

                            if (response.length() == 0) {
                                String msg = "msg=No open matches found";
                                packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);
                            } else {
                                packet = new DatagramPacket(response.toString().getBytes(), response.toString().getBytes().length, address, port);
                            }


                        } catch (Exception ignored) {
                        }
                        con.close();
                        serverSocket.send(packet);
                    }
                }



            } catch (IOException | SQLException ignored) {
            }
        }
    }
}
