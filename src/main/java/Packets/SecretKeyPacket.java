package Packets;

/**
 * Created by rathma on 6/1/16.
 */
public class SecretKeyPacket {
    public SecretKeyPacket(){}
    public byte[] key;
    public SecretKeyPacket(byte[] key)
    {
        this.key = key;
    }
}
