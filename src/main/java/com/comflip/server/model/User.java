package com.comflip.server.model;

import com.comflip.server.util.UID;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private String sessionID;
    private String username;
    private int userID;

    public User(ResultSet rep) throws SQLException {
        this.username = rep.getString("username");
        this.userID = rep.getInt("id");
        this.sessionID = UID.setSession();
    }
    public String getSessionID() {
        return this.sessionID;
    }

    public String getUsername() {
        return this.username;
    }
}
