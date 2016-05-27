package Packets;

/**
 * Created by rathma on 5/22/16.
 */
public class CreateGamePacket {
    public int gameID;
    public String player1;
    public String player2;

    public int p1;
    public int p2;

    public boolean white;

    public CreateGamePacket(){}
    public CreateGamePacket(int gameID, int player1ID, int player2ID, boolean white)
    {
        if(white)
            this.white=true;
        else
            this.white=false;
        this.gameID = gameID;
        this.p1=player1ID;
        this.p2=player2ID;
    }
}
