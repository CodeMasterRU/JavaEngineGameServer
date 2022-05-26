package com.comflip.server;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQL {
    private Connection connection;

    private boolean isInit;

    private String ip;
    private String port;

    public SQL(String ip, String port) {
        this.ip = ip;
        this.port = port;

        try {
            this.initialize();
        } catch (Exception e) {
            this.isInit = false;
            System.out.println(e);
        }
    }

    private void initialize() throws SQLException, IOException, URISyntaxException {
        String s;
        StringBuffer sb = new StringBuffer();

        URL res = Main.class.getClassLoader().getResource("test.sql");
        assert res != null;
        File file = Paths.get(res.toURI()).toFile();

        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        br.close();

        String[] inst = sb.toString().split(";");

        System.out.println("Connection to database");

        this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.ip + ":" + this.port + "/", "root", ""
        );

        Statement st = this.connection.createStatement();

        System.out.println("Connection valid: " + this.connection.isValid(5));

        for (int i = 0; i < inst.length; i++) {
            if (!inst[i].trim().equals("")) {
                st.executeUpdate(inst[i]);
                System.out.println(">>" + inst[i]);
            }
        }

        System.out.println("Initialize to database is finished!");

        this.closeConnection();

        this.isInit = true;
    }

    public Connection openConnection() throws SQLException {
        return this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.ip + ":" + this.port + "/jdbc", "root", ""
        );
    }

    public void closeConnection() throws SQLException {
        System.out.println("Closing database connection");
        this.connection.close();
    }

    public boolean isInit() { return this.isInit; }

    public Connection getConnection() {return this.connection;}

}
