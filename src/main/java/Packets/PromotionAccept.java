package Packets;

/**
 * Created by rathma on 5/28/16.
 */
public class PromotionAccept {
    public PromotionAccept(){}
    public boolean accepted=false;
    public int gameID;
    public int boardID;
    public int pieceX;
    public int pieceY;
    public int newID;
    public PromotionAccept(boolean accepted, int gameID, int boardID, int pieceX, int pieceY, int newID) {
        this.accepted=accepted;
        this.gameID=gameID;
        this.boardID=boardID;
        this.newID=newID;
        this.pieceX=pieceX;
        this.pieceY=pieceY;
    }

}
