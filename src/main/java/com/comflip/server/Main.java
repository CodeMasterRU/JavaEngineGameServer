package com.comflip.server;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        SQL sqlserver  = new SQL("127.0.0.1", "3306");

        if (sqlserver.isInit()){
//            ServerContainer sc = new ServerContainer(new ServerClusterSockets());
//            sc.start();

            ServerClusterSockets serverClusterSockets = new ServerClusterSockets(sqlserver);

            try {
                serverClusterSockets.start(5555);
            } catch (Exception ignored){
                System.out.println(ignored);
            }
        }
    }
}
