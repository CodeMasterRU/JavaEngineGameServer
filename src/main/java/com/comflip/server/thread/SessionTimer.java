package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.util.HashMap;

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

//            Iterator<Map.Entry<String, String>> it = hashSessionUsers.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry item = (Map.Entry) it.next();
//                if (second - Integer.parseInt(hashSessionUsers.get(item.getKey())) > 10) {
//                    try {
//                        Connection con = ServerContainer.sqlserver.openConnection();
//                        String updateUser = "UPDATE user SET sessionID = null, online = 0 WHERE username = ?";
//
//                        PreparedStatement updateStmt = con.prepareStatement(updateUser);
//                        updateStmt.setString(1, (String) item.getKey());
//                        updateStmt.executeUpdate();
//
//                        con.close();
//
//                        hashSessionUsers.remove(item.getKey());
//
//                    } catch (SQLException ignored) {
//                    }
//                }
//                it.remove();
//            }

        }
    }
}
