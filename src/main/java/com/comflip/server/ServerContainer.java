package com.comflip.server;

import com.comflip.server.thread.CommandLine;
import com.comflip.server.thread.ServerClusterSockets;
import com.comflip.server.thread.SessionTimer;

import java.io.IOException;

public class ServerContainer {
    volatile boolean running = true;

    private Thread sessionTimerThread;
    private Thread commandLineThread;
    private Thread serverSocketsThread;

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

        SessionTimer sessionTimer = new SessionTimer(this);
        sessionTimerThread = new Thread(sessionTimer);
        sessionTimerThread.start();
    }

    public boolean isRunning() {
        return this.running;
    }
}
