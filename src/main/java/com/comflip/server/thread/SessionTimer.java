package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class SessionTimer implements Runnable {
    public static HashMap<String, String> hashSession = new HashMap<>();
    public static int second = 0;
    private final ServerContainer serverContainer;

    public SessionTimer(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

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

            for (String username : hashSession.keySet()) {
                if (second - Integer.parseInt(hashSession.get(username)) > 10) {
                    try {
                        Connection con = this.serverContainer.sqlserver.openConnection();
                        String updateUser = "UPDATE user SET sessionID = null, online = 0 WHERE username = '" + username + "'";

                        PreparedStatement updateStmt = con.prepareStatement(updateUser);
                        updateStmt.executeUpdate();

                        con.close();
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
    }
}
