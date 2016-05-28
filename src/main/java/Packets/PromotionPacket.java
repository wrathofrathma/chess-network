package Packets;

/**
 * Created by rathma on 5/27/16.
 */
public class PromotionPacket {
    public int pawnx;
    public int pawny;
    public int newID;
    public int gameID;
    public int boardID;
    public PromotionPacket(){}
    public PromotionPacket(int pawnx, int pawny, int newID, int gameID, int boardID){
        this.pawnx = pawnx;
        this.pawny=pawny;
        this.newID=newID;
        this.gameID=gameID;
        this.boardID=boardID;
    }
}
