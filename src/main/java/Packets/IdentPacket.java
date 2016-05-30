package Packets;

/**
 * This is sent to the server upon login.
 */
public class IdentPacket {
    public String username;
    public String passwordHash;
    public boolean acceptBit;
    public IdentPacket(){}
    public IdentPacket(String username, String passwordHash)
    {
        this.username = username;
        this.passwordHash = passwordHash;
    }
    public IdentPacket(String username)
    {
        this.username = username;
    }
    public IdentPacket(boolean acceptBit)
    {
        this.acceptBit = acceptBit;
    }
}
