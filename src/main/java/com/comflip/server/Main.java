package com.comflip.server;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SQL sqlserver = new SQL("127.0.0.1", "3306");

        if (sqlserver.isInit()) {
            System.out.println("Welcome! Type 'help' command for more information");
        }

        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String command;
                if ((command = in.nextLine()) != null) {
                    switch (command) {
                        case "help" -> {
                            System.out.println("start  -> Starting the socket server");
                            System.out.println("remove -> Removing the server database");
                            System.out.println("clear  -> Cleaning up the database");
                        }

                        case "remove" -> {
                            sqlserver.remove();
                            System.out.println("Done! Database is removed");
                        }

                        case "clear" -> {
                            sqlserver.remove();
                            sqlserver.create();
                            System.out.println("Done! Database is clean up");
                        }

                        case "create" -> {
                            sqlserver.create();
                            System.out.println("Done! Database is created!");
                        }

                        case "start" -> {
                            if (sqlserver.isInit()){
                                ServerClusterSockets serverClusterSockets = new ServerClusterSockets(sqlserver);
                                try {
                                    serverClusterSockets.start(5555);
                                } catch (Exception e) {
                                    System.out.println("Error! " + e);
                                }
                            } else {
                                System.out.println("Error! The database does not exist. You must create it");
                            }

                        }

                        case "exit" -> {
                            System.out.println("See a next time!");
                            System.exit(0);
                        }

                        case "" -> System.out.print("");
                        default -> System.out.println("Error! Unknown command!");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error! " + e);
        }
    }
}
