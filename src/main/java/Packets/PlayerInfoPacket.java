package Packets;

/**
 * Created by rathma on 5/23/16.
 */
public class PlayerInfoPacket {
    public String username;
    public int userID;
    public boolean specialID=false;
    public PlayerInfoPacket(){}

    public PlayerInfoPacket(String username, int userID)
    {
        this.username = username;
        this.userID = userID;
    }
    public PlayerInfoPacket(String username, int userID, boolean special)
    {
        this.username = username;
        this.userID = userID;
        this.specialID = special;
    }
}