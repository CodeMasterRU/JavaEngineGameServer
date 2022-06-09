package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public record SessionTimer(ServerContainer serverContainer) implements Runnable {
    public static HashMap<String, String> hashSessionUsers = new HashMap<>();
    public static HashMap<String, String> hashSessionMatch = new HashMap<>();
    public static int second = 0;

    @Override
    public void run() {
        double firstTime;
        double lastTime = System.nanoTime() / 1000000000.0;
        double passedTime;
        double unprocessedTime = 0;

        double bufferTime = 0;

        while (serverContainer.isRunning()) {
            firstTime = System.nanoTime() / 1000000000.0;
            passedTime = firstTime - lastTime;
            lastTime = firstTime;

            unprocessedTime += passedTime;
            bufferTime += passedTime;

            double UPDATE = 1.0 / 60.0;
            while (unprocessedTime >= UPDATE) {
                unprocessedTime -= UPDATE;

                if (bufferTime >= 1.0) {
                    bufferTime = 0;
                    second += 1;
                }
            }

            for (Map.Entry<String, String> stringStringEntry : hashSessionUsers.entrySet()) {
                if (second - Integer.parseInt(hashSessionUsers.get(stringStringEntry.getKey())) > 10) {
                    try {
                        Connection con = ServerContainer.sqlserver.openConnection();
                        String updateUser = "UPDATE user SET sessionID = null, online = 0 WHERE username = ?";

                        PreparedStatement updateStmt = con.prepareStatement(updateUser);
                        updateStmt.setString(1, stringStringEntry.getKey());
                        updateStmt.executeUpdate();

                        con.close();

                        hashSessionUsers.remove(stringStringEntry.getKey());
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
    }
}
