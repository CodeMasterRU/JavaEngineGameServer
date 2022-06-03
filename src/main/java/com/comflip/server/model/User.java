package com.comflip.server.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String sessionID;
    private String username;
    private int userID;

    public User(ResultSet rep) throws SQLException {
        this.username = rep.getString("username");
        this.userID = rep.getInt("id");
    }

    public void setSessionID(){
        String sessionID = Integer.toString((int) (Math.random() * (999999999 + 1) + 0)); // 0 to 999 999 999
        StringBuilder fullSessionID = new StringBuilder();

        if (sessionID.length() < 10){
            String beforeSessionID = "0".repeat(10 - sessionID.length());
            fullSessionID.append(beforeSessionID);
            fullSessionID.append(sessionID);
        }

        this.sessionID = fullSessionID.toString();
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public String getUsername() {
        return this.username;
    }
}
