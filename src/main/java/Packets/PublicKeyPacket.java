package Packets;

/**
 * Created by rathma on 6/1/16.
 */
public class PublicKeyPacket {
    public PublicKeyPacket(){}
    public byte[] key;
    public PublicKeyPacket(byte[] key)
    {
        this.key = key;

    }
}
