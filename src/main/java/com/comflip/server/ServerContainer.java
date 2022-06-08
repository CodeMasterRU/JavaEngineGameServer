package com.comflip.server;

import com.comflip.server.thread.CommandLine;
import com.comflip.server.thread.ServerSocketTCP;
import com.comflip.server.thread.ServerSocketUDP;
import com.comflip.server.thread.SessionTimer;

import java.io.IOException;

public class ServerContainer {
    volatile boolean running = true;

    private Thread sessionTimerThread;
    private Thread commandLineThread;
    private Thread serverSocketsTCPThread;
    private Thread serverSocketsUDPThread;

    public static final SQL sqlserver = new SQL("127.0.0.1", "3306");

    public void start() throws IOException {
        CommandLine commandLine = new CommandLine(this);
        commandLineThread = new Thread(commandLine);
        commandLineThread.start();

        ServerSocketTCP serverSocketsTCP = new ServerSocketTCP(this);
        serverSocketsTCPThread = new Thread(serverSocketsTCP);
        serverSocketsTCPThread.start();

        ServerSocketUDP serverSocketsUDP = new ServerSocketUDP(this);
        serverSocketsUDPThread = new Thread(serverSocketsUDP);
        serverSocketsUDPThread.start();

        SessionTimer sessionTimer = new SessionTimer(this);
        sessionTimerThread = new Thread(sessionTimer);
        sessionTimerThread.start();
    }

    public boolean isRunning() {
        return this.running;
    }
}
