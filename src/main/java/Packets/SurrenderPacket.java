package Packets;

/**
 * Created by rathma on 5/30/16.
 */
public class SurrenderPacket {
    public SurrenderPacket(){}
    public SurrenderPacket(int gameID, int playerID)
    {
        this.gameID = gameID;
        this.playerID = playerID;
    }
    public int gameID;
    public int playerID;
}
