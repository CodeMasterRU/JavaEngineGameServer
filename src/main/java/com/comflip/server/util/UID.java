package com.comflip.server.util;

public class UID {
    public static String setSession(){
        String sessionID = Integer.toString((int) (Math.random() * (999999999 + 1) + 0)); // 0 to 999 999 999
        StringBuilder fullSessionID = new StringBuilder();

        if (sessionID.length() < 10){
            String beforeSessionID = "0".repeat(10 - sessionID.length());
            fullSessionID.append(beforeSessionID);
            fullSessionID.append(sessionID);
        }
        return fullSessionID.toString();
    }
}
