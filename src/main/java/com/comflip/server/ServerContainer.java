package com.comflip.server;

import com.comflip.server.thread.CommandLine;
import com.comflip.server.thread.ServerClusterSockets;

import java.io.IOException;
import java.util.Scanner;

public class ServerContainer {
    volatile boolean running = true;

    Thread commandLineThread;
    Thread serverSocketsThread;

    public final SQL sqlserver = new SQL("127.0.0.1", "3306");


    public ServerContainer() {
    }

    public void start() throws IOException {
        CommandLine commandLine = new CommandLine(this);
        commandLineThread = new Thread(commandLine);
        commandLineThread.start();

        ServerClusterSockets serverClusterSockets = new ServerClusterSockets(this);
        serverSocketsThread = new Thread(serverClusterSockets);
        serverSocketsThread.start();
    }

    public boolean isRunning() {
        return running;
    }
}
