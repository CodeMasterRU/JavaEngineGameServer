package com.comflip.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class SQL {
    private Connection connection;

    private boolean isInit;

    private final String ip;
    private final String port;

    public SQL(String ip, String port) {
        this.ip = ip;
        this.port = port;

        this.initialize();
    }

    private void initialize() {
        System.out.println("Connection to database (" + this.ip + ":" + this.port + ")");

        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.ip + ":" + this.port + "/", "root", ""
            );

            System.out.println("Connection valid: " + this.connection.isValid(5) + "\n");

            Statement st = this.connection.createStatement();

            for (String value : sqlReader("initialize.sql")) {
                if (!value.trim().equals("")) {
                    st.executeUpdate(value);
                    System.out.println(">>" + value);
                }
            }

            System.out.println("\nInitialize to database is finished!\n");

            this.closeConnection();

            this.isInit = true;
        } catch (SQLException | IOException | URISyntaxException e) {
            System.out.println("\n" + "Error! " + e);
            this.isInit = false;
        }
    }

    public void remove() throws SQLException {
        openConnection().createStatement().execute("DROP DATABASE jdbc");
        this.isInit = false;
    }
    public void create() throws SQLException {
        this.initialize();
    }

    public Connection openConnection() throws SQLException {
        return this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.ip + ":" + this.port + "/jdbc", "root", ""
        );
    }

    public void closeConnection() throws SQLException {
        this.connection.close();
    }

    private String[] sqlReader(String path) throws IOException, URISyntaxException {
        String s;
        StringBuilder sb = new StringBuilder();

        InputStream res = Main.class.getClassLoader().getResourceAsStream(path);

        BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(res)));
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        br.close();

        return sb.toString().split(";");
    }

    public boolean isInit() {
        return this.isInit;
    }
}
