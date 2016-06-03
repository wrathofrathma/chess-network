package com.chess.network;

import com.esotericsoftware.kryonet.Connection;

import javax.crypto.SecretKey;

/**
 * Base Client class. Might later be inherited by a com.chess.network.Player class. - Renamed it, decided to just dump everything in here. Will separate at a later date?
 * We extend Connection because we can add listeners to individual players, which will be useful for Game Rooms.
 */
public class Player{
    public Connection connection;
    public User user=null;
    public boolean inGame=false;
    //Session Key
    public SecretKey key;
    //Temporary nickname, primarily used for guests.
    public String nick;
    public Player(Connection connection)
    {
        this.connection = connection;
        nick = "Guest - "+connection.getID();
        key=null;
    }

}
