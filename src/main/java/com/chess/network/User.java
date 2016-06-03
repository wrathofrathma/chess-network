package com.chess.network;

/**
 * The user class is to be used in conjunction with the Player class. Players are active connections, users are the actual user data of the connection.
 */
public class User {
    public String username;
    public String password;

    public User(){}
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
