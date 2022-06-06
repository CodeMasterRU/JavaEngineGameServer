package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.sql.SQLException;
import java.util.Scanner;

public class CommandLine implements Runnable {
    private final ServerContainer serverContainer;

    private static int statusCodeSocket = 0;

    public CommandLine(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

    @Override
    public void run() {
        if (serverContainer.sqlserver.isInit()) {
            System.out.println("Welcome! Type 'help' command for more information");
            System.out.println("Press 'Enter' to continue");
        }

        Scanner scanner = new Scanner(System.in);
        while (serverContainer.isRunning()) {
            String command;
            if ((command = scanner.nextLine()) != null) {
                try {
                    this.readLine(command);
                    System.out.print("> ");
                } catch (SQLException e) {
                    System.out.println("Error! " + e);
                }
            }
        }
    }

    private void readLine(String command) throws SQLException {
        switch (command) {
            case "help" -> {
                System.out.println("              SERVER SOCKET             ");
                System.out.println("----------------------------------------");
                System.out.println("start   ->    Starting the socket server");
                System.out.println("stop    ->    Stops the socket server");
                System.out.println();
                System.out.println("               SERVER SQL               ");
                System.out.println("----------------------------------------");
                System.out.println("remove  ->  Removing the server database");
                System.out.println("clear   ->  Cleaning up the database");
                System.out.println("create  ->  Creating a database");
            }

            case "remove" -> {
                if (serverContainer.sqlserver.isInit()) {
                    serverContainer.sqlserver.remove();
                    System.out.println("Done! Database is removed");
                } else {
                    System.out.println("Error! The database does not exist. You must create it");
                }
            }

            case "clear" -> {
                if (serverContainer.sqlserver.isInit()) {
                    serverContainer.sqlserver.remove();
                    serverContainer.sqlserver.create();
                    System.out.println("Done! Database is clean up");
                } else {
                    System.out.println("Error! The database does not exist. You must create it");
                }

            }

            case "create" -> {
                serverContainer.sqlserver.create();
                System.out.println("Done! Database is created!");
            }

            case "start" -> {
                if (serverContainer.sqlserver.isInit()) {
                    statusCodeSocket = 2; // Code 2 is active
                } else if (statusCodeSocket == 2) {
                    System.out.println("Error! ServerClusterSocket is already active");
                } else {
                    System.out.println("Error! The database does not exist. You must create it");
                }
            }

            case "stop" -> {
                if (statusCodeSocket == 2) {
                    statusCodeSocket = 1; // Code 1 is disabled
                } else {
                    System.out.println("Error! ServerClusterSocket is not running");
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

    public static int statusCodeSocket() {
        return statusCodeSocket;
    }
}