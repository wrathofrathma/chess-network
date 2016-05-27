package com.chess.network;

import Packets.MovePacket;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
/**
 * I figure it's cleanest to do this in one swoop.
 *
 * This is a listener instance designed for individual players within a game room.
 */
public class GameListener extends Listener{
    public GameRoom gameRoom; //Link back to the game room.

    public GameListener(GameRoom gameRoom)
    {
        this.gameRoom = gameRoom;
    }
    public void received(Connection connection, Object object)
    {
        if(object instanceof MovePacket)
        {
            MovePacket move = (MovePacket) object;
            gameRoom.tryMove(move);
        }
    }

    public void disconnected(Connection connection)
    {
        //If someone disconnects, we need to notify and possibly end the game room.
        super.disconnected(connection);
        gameRoom.disconnected(connection.getID());
    }


}
