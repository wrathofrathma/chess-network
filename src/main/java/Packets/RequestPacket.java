package Packets;

public class RequestPacket {

    /* 0 - Requesting Player Update
     * 1 - PlayInfoPacket
     * 2 - Request Challenges
        */
    public int request;
    public RequestPacket(){}
    public RequestPacket(int request)
    {
        this.request=request;
    }
}
