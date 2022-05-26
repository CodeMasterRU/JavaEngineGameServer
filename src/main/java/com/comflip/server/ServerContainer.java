package com.comflip.server;

public class ServerContainer implements Runnable {
    private ServerClusterSockets socket;

    public ServerContainer(ServerClusterSockets socket) {
        this.socket = socket;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        boolean pause = false;
        double firstTime;
        double lastTime = System.nanoTime() / 1000000000.0;
        double passedTime;
        double unprocessedTime = 0;

        while (true){
            firstTime = System.nanoTime() / 1000000000.0;
            passedTime = firstTime - lastTime;
            lastTime = firstTime;

            unprocessedTime += passedTime;

            double UPDATE = 1.0 / 60.0;
            while (unprocessedTime >= UPDATE) {
                unprocessedTime -= UPDATE;
                pause = true;
                socket.update();
            }

            if (!pause){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ServerClusterSockets getSocket() {
        return this.socket;
    }
}
