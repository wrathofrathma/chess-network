package Packets;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

/**
 * Customised packet object
 */
public class MessagePacket {
    public MessagePacket(){}
    public MessagePacket(String message){
        this.message = message;
    }
    public MessagePacket(int userid, String message)
    {
        this.userid=userid;
        this.message=message;
    }
    public String message;
    public int userid;
}

