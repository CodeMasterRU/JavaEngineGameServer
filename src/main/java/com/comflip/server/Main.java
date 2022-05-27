package com.comflip.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerContainer serverContainer = new ServerContainer();
        serverContainer.start();
    }
}

