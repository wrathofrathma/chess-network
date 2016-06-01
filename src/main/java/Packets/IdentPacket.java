package Packets;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * This is sent to the server upon login.
 */
public class IdentPacket {
    public byte[] username;
    public boolean acceptBit;
    public IdentPacket(){}

    public IdentPacket(byte[] username)
    {
        this.username = username;
    }
    public IdentPacket(boolean acceptBit)
    {
        this.acceptBit = acceptBit;
    }

}