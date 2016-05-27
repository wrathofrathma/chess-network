package Packets;

/**
 * Created by rathma on 5/24/16.
 */
/**
 * Created by rathma on 5/24/16.
 */
public class GameEndPacket {
    public GameEndPacket(){}
    /* Endbit = Game is ending
     *
     */
    public GameEndPacket(boolean endbit,int gameID, int winnerID, String winner, String condition)
    {
        this.gameID=gameID;
        this.endbit=endbit;
        this.winnerID = winnerID;
        this.winnerUsername = winner;
        this.condition = condition;
    }
    public int winnerID;
    public String winnerUsername;
    public int gameID;
    public boolean endbit;
    public String condition; //Win condition. Checkmate, DC, etc.
}
