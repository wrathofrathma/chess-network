package Packets;

/**
 * Created by rathma on 5/22/16.
 */
public class MovePacket {
    //Only from the server. Never outgoing.
    public MovePacket(int gameID, int boardID, int x1, int y1, int x2, int y2,boolean turnswitch)
    {
        this.boardID = boardID;
        this.x1 = x1;
        this.x2 = x2;
        this.y2 = y2;
        this.y1 = y1;
        this.gameID=gameID;
        this.turnswitch = turnswitch;
    }
    public MovePacket(int gameID, int x1, int y1, int x2, int y2,int playerID)
    {
        this.playerID = playerID;
        this.x1 = x1;
        this.x2 = x2;
        this.y2 = y2;
        this.y1 = y1;
        this.gameID=gameID;
    }
    public int specialID=0; //This is for special moves like castling or en passant.
    /* 0 = no speical move
       1 = castling
       2 = en passant
     */
    public MovePacket(int gameID, int x1, int y1, int x2, int y2,int playerID, int specialID)
    {
        this.playerID = playerID;
        this.x1 = x1;
        this.x2 = x2;
        this.y2 = y2;
        this.y1 = y1;
        this.gameID=gameID;
        this.specialID=specialID;
    }

    public int boardID;
    public int playerID;
    public MovePacket(){}
    //String structure : LOC1 LOC2
    public int x1, y1;
    public int x2, y2;
    public int gameID;
    public boolean turnswitch=true; //Wow, I fucking thought ahead.
    @Override
    public String toString() {
        return "("+x1+","+y1+") to ("+x2+","+y2+")";
    }
}
