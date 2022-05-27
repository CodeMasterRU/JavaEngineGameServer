package com.comflip.server.thread;

import com.comflip.server.ServerContainer;

import java.sql.SQLException;
import java.util.Scanner;

public class CommandLine implements Runnable {
    private final ServerContainer serverContainer;

    private static boolean startServerSocket = false;

    public CommandLine(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
    }

    @Override
    public void run() {
        if (serverContainer.sqlserver.isInit()) {
            System.out.println("Welcome! Type 'help' command for more information");
        }

        Scanner scanner = new Scanner(System.in);
        while (serverContainer.isRunning()) {
            System.out.print("> ");
            String command;
            if ((command = scanner.nextLine()) != null) {
                try {
                    this.readLine(command);
                } catch (SQLException e) {
                    System.out.println("Error! " + e);
                }
            }
        }
    }

    private void readLine(String command) throws SQLException {
        switch (command) {
            case "help" -> {
                System.out.println("start   -> Starting the socket server");
                System.out.println("remove  -> Removing the server database");
                System.out.println("clear   -> Cleaning up the database");
                System.out.println("create  -> Creating a database");
            }

            case "remove" -> {
                serverContainer.sqlserver.remove();
                System.out.println("Done! Database is removed");
            }

            case "clear" -> {
                serverContainer.sqlserver.remove();
                serverContainer.sqlserver.create();
                System.out.println("Done! Database is clean up");
            }

            case "create" -> {
                serverContainer.sqlserver.create();
                System.out.println("Done! Database is created!");
            }

            case "start" -> {
                if (serverContainer.sqlserver.isInit()) {
                    startServerSocket = true;
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

    public static boolean isStartServerSocket() {
        return startServerSocket;
    }
}