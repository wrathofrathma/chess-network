package com.chess.network;

import Packets.*;

import java.util.Random;
import java.util.Vector;


/**
 * Chess Game Room
 *
 *
 * Flow of a move
 * ** Receive move request
 * ** If it's the player's turn then call isValidMove(). If it isn't then queue the move?(Future)
 * ** If it returns true then call updateBoard()
 * ** Have updateBoard() send the updated board state to player1 & player2.
 */
public class GameRoom {

    int gameID;
    public BoardPosition board;
    public Player player1;
    public Player player2;
    int whiteID; //Just hacking this together quick to simplify com.chess.network.Player class.
    int blackID;
    public GameListener listener1;
    public GameListener listener2;
    public ChessNetwork network;
    int currentTurnID;
    boolean multiBoard=false; //Quick hack to get this working again.
    boolean turnshift=true;

    public GameRoom(Player p1, Player p2, ChessNetwork network)
    {
        if(p1==null || p2==null) {
            System.out.println("Can't have NULL players");
            System.exit(1);
        }
        this.network = network;
        this.player1=p1;
        this.player2=p2;
       // this.board = new int[8][8];
        Random random = new Random();
        //Assign player colours using a RNG library.

        int firstplayer = random.nextInt(10);
        System.out.println(firstplayer);
        if(firstplayer>=0 && firstplayer<=5)
        {
            blackID=player2.connection.getID();
            whiteID=player1.connection.getID();
        }
        else if(firstplayer>=6 && firstplayer<=10)
        {
            blackID=player1.connection.getID();
            whiteID=player2.connection.getID();
        }
        else
        {
            System.out.println("random.nextInt(1) returned a value other than 0 and 1");
            System.exit(1);
        }
        listener1 = new GameListener((this));
        listener2 = new GameListener((this));
        player1.connection.addListener(listener1);
        player2.connection.addListener(listener2);
        //This ensures a unique gameID.
        gameID =0;
        boolean gameExists=true;
        while(gameExists)
        {
            gameID = random.nextInt(100000);
            gameExists=false;
            for(GameRoom gr : network.gameRooms)
            {
                if(gr.gameID==gameID)
                {
                    gameExists=true;
                }
            }
        }




        this.board = new BoardPosition(gameID);
        System.out.println("Notifying clients of the game");
        //Send to white
        network.server.sendToTCP(whiteID, new CreateGamePacket(gameID, whiteID, blackID,true));
        //send to black
        network.server.sendToTCP(blackID, new CreateGamePacket(gameID, blackID, whiteID,false));
        currentTurnID = whiteID;
    }

    public void switchTurns()
    {
        if(currentTurnID==whiteID)
            currentTurnID=blackID;
        else
            currentTurnID=whiteID;
    }
    public void tryPromotion(PromotionPacket packet)
    {
        if(inBounds(packet.pawnx) && packet.pawny==0)
        {
            //Black's pawn is on white's back rank
            if(board.board[packet.pawnx][packet.pawny]==5){
                //allow promotion & change turns
                //TODO check if the id is valid.
                board.board[packet.pawnx][packet.pawny]=packet.newID;
                player1.connection.sendTCP(new PromotionAccept(true, gameID,0,packet.pawnx, packet.pawny,packet.newID));
                player2.connection.sendTCP(new PromotionAccept(true, gameID,0,packet.pawnx, packet.pawny,packet.newID));
                switchTurns();
                turnshift=true;
            }
        }
        else if(inBounds(packet.pawnx) && packet.pawny==7)
        {
            //White's pawn on black's back rank.
            if(board.board[packet.pawnx][packet.pawny]==11){
                //Allow
                board.board[packet.pawnx][packet.pawny]=packet.newID;
                player1.connection.sendTCP(new PromotionAccept(true, gameID,0,packet.pawnx, packet.pawny,packet.newID));
                player2.connection.sendTCP(new PromotionAccept(true, gameID,0,packet.pawnx, packet.pawny,packet.newID));
                switchTurns();
                turnshift=true;
            }
        }
    }

    public boolean inBounds(int test)
    {
        if(test<0 || test >7) {
            System.out.println("Invalid move received: Out of bounds");
            return false;
        }
        return true;
    }
    public boolean validCapture(int p1, int p2)
    {
        //TODO perhaps check if we're in check.
        if(p2==12) //Empty space.
        {
            return true;
        }
        if(p1>=0 && p1<=5) {//Is black
            if (p2 >= 0 && p2 <= 5) {
                return false;
            }
        }
        else if(p1>=6 && p1 <=11) //Is white
            {
                if (p2 >= 6 && p2 <= 11) {
                    return false;
                }
            }
        return true;
    }
    public boolean isLateralXValid(int x1, int x2, int y,int[][] position)
    {
        /* Detect if any pieces are along the row.
         * 1. Grab all values between x1 -> x2
         * 2. Locked along the Y axis, check each value.
          * */
        if(x1==x2)
            return true;
        if(x1>x2)
        {
            for(int i=++x2; i<x1;i++) // We only need to account for up UNTIL the landing square.
            {
                if(position[i][y]!=12)
                {
                    return false;
                }
            }
        }
        else if(x1<x2)
        {
            for(int i=++x1; i<x2;i++) // We only need to account for up UNTIL the landing square.
            {
                if(position[i][y]!=12)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isLateralYValid(int y1, int y2, int x,int[][] position)
    {
        if(y1==y2)
            return true;
        if(y1>y2)
        {
            for(int i=++y2; i<y1;i++) // We only need to account for up UNTIL the landing square.
            {
                if(position[x][i]!=12)
                {
                    return false;
                }
            }
        }
        else if(y1<y2)
        {
            for(int i=++y1; i<y2;i++) // We only need to account for up UNTIL the landing square.
            {
                if(position[x][i]!=12)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isDiagonalValid(int x1, int y1, int x2, int y2, int[][] position)
    {
        /* We are going to do some testing here. We initially ghetto rigged this movement system, but there is actually a mathematical solution we need to learn */
        /* This is relevant so that we don't calculate the X & Y separately - If we make this work, we can do them simultaneously and much easier.
         * This would also be able to be used for lateral movement if done properly.
         */
        /* Calculating the vectors between two points is as follows
         * Given point1 & point 2, to find any point along any indivdiual axis(x, y, or z). Let start & dest be our vectors
                 * point.x = start.x + ((dest.x - start.x) * progress);
                 * Progress is defined as a float between 0 & 1 to represent where it is along the translation
                 * NORMALLY you would use delta time to calculate percentage as currentTime/finalTime
                 * In chess, we don't use time. So we need to calculate progress as something else.
                 * We need progress to equal the percentage of the
         */
        //Expected input point1.x & point2.x
        //Expected output each point between them.
        //We COULD just loop through them until we reach the other, but instead we can use a multiplier to calculate Y as well.
        //So let's calculate that multiplier.
        //Progress needs to be multiplied by (point2-point1) and added to point1 to get our in between points
        /* Given point (0,1) to (4,5)
         * Formula for x would be 0 + (4 * progress)
         * * Output required would be 1, 2, 3
         * Formula for y would be 1 + (4 * progress)
         * * Output required would be 2, 3, 4
         *
         * Example 2 translating from (6,5) to (2,1)
         * * x: 6 + (-4 * progress)
         * * * Output: 5, 4, 3
         * * * Required multiplier to get output: 0.25, 0.5, 0.75 (1/4 * step)
         * * y: 5 + (-4 * progress)
         * * * Output: 4, 3, 2
         * * * x's multiplier: (1/4 * step)
         *
         * Odd Example (1,1) (4,4)
         * x: 3 + (3 * progress)
         * */



        /* Oh fun.....*/
        if(x1==x2 || y1 ==y2) //Unlike our lateral friends, these cannot move along X or Y individually.
            return false;
        else if(Math.abs(x1-x2)!=Math.abs(y1-y2)) //The absolute value of hte move should be the same if they move 1:1
            return false;

        if(x1>x2)
        {
            if(y1>y2)
            {
                //Down Left
                int j=y2;
                for(int i=++x2; i<x1; i++) //Can't use a double nexted loop due to it checking incorrect positions.
                {
                    if(++j>=y1)
                        break;
                    if(position[i][j]!=12)
                        return false;
                }
            }
            else
            {
                //Up Left
                int j=y2;
                for(int i=++x2; i<x1; i++) //Starting at our Target destination to the Left incriment ++ X.
                {
                    if(--j<=y1)
                        break;
                    if(position[i][j]!=12) {
                        return false;
                    }
                }
            }

        }
        else if(x2>x1)
        {
            if(y1>y2)
            {
                //Down Right
                int j=y2;
                for(int i=--x2;i>x1;i--)
                {
                    if(++j>=y1)
                        break;
                    if(position[i][j]!=12) {
                            return false;
                    }
                }
            }
            else
            {
                int j=y1;
                for(int i=++x1; i<x2; i++)
                {
                    if(++j>=y2)
                        break;
                    if(position[i][j]!=12)
                        return false;
                }
            }

        }
        return true;
    }
    //TODO do we want to pass this guy an array of integers to check, or should we always check the main board?
    public boolean promotionCheck(MovePacket m)
    {
        if((board.board[m.x1][m.y1]==5 && m.y2==0) ||(board.board[m.x1][m.y1]==11 && m.y2==7)){
            turnshift=false;
            network.server.sendToTCP(m.playerID,new PromotionPacket(m.x2,m.y2,0,gameID,0));
            return true;
        }
        return false;
    }
    public boolean isValid(MovePacket m, int[][] position)
    {

        /* Use x1 y1 to grab current piece. Figure out all valid moves then check against the suggested move */
        int piece = position[m.x1][m.y1]; //Piece trying to be moved.
        int x1 = m.x1;
        int x2 = m.x2;
        int y1 = m.y1;
        int y2 = m.y2;
        int xabs = Math.abs(x1-x2);
        int yabs = Math.abs(y1-y2);
        if(!inBounds(x2) || !inBounds(y2)) //May as well check this first.
            return false;
        /* We should probably check if you're trying to move your own fucking piece this time around */
        if(position[m.x1][m.y1]>=0 && position[m.x1][m.y1]<=5) //Piece being moved is black
        {
            if(m.playerID!=blackID)
                return false;
        }
        else if(position[m.x1][m.y1]>=6 && position[m.x1][m.y1]<=11) //Piece being moved is white
        {
            if(m.playerID!=whiteID)
                return false;
        }


        /* Let's set up move logic for individual pieces before checking threats */
        switch(piece){
            case 0: //Black King
                for(MovePacket movePacket : getKingMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 1: //Black Queen
                for(MovePacket movePacket : getQueenMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 2: //Black Rook
                if((xabs!=0) && (yabs!=0))
                    return false;
                for(MovePacket movePacket : getRookMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 3: //Black Knight
                //Knights can only move 2 spaces in any direction & can never land on a tile on the same X or Y it started.
                if((xabs>2 || xabs == 0) && (yabs>2 || yabs == 0))
                    return false;
                //Knight movement is restricted to 2x1
                if((xabs==1 && yabs ==2) || (xabs==2 && yabs==1))
                {
                    if(validCapture(position[x1][y1],position[x2][y2])){
                        return true;
                    }
                }
                return false;
            case 4: //Black Bishop
                for(MovePacket movePacket : getBishopMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 5: //Black Pawn
                if(y1-y2>0) //Pawns can't move laterally, so it should never need >=
                {
                    if(position[x2][y2]!=12)
                    {
                        //Check for diagonal
                        if(xabs==0)
                        {
                            //Not diagonal so we can't move forward.
                            return false;
                        }
                        else
                        {
                            if(yabs==1 && xabs == 1) //Moving forward diagonally 1. Can't move diagonally by more than 1.
                            { //Should by your standard capture by 1 diagonally.
                                if(validCapture(position[x1][y1],position[x2][y2])) {
                                    return true;
                                }
                            }
                        }
                    }
                    else //No piece is in our way.
                    {
                        if(xabs!=0) //Can't move laterally as a pawn for no reason.
                            return false;
                        if(yabs>=1)
                        {
                            if(y1==6 && yabs <=2) {
                                if(validCapture(position[x1][y1],position[x2][y2]))
                                {
                                        return true;
                                }
                            }
                            else if(yabs==1) {
                                if(validCapture(position[x1][y1], position[x2][y2]))
                                {
                                        return true;
                                }
                            }
                        }
                        else
                            return false;
                    }
                }
                break;
            case 6: //White King
                for(MovePacket movePacket : getKingMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 7: //White Queen
                /* First we'll just test what KIND of movement it is */
                for(MovePacket movePacket : getQueenMoves(m.x1,m.y1,position))
                {

                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 8: //White Rook
                //This will increase the efficiency if we do little checks like this.
                if((xabs!=0) && (yabs!=0)) //If it hasn't moved along the file the absolute value will be 0. Both being !=0 means it's moving along both axis.
                    return false;
                for(MovePacket movePacket : getRookMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 9: //White Knight
                //Knights can only move 2 spaces in any direction & can never land on a tile on the same X or Y it started.
                if((xabs>2 || xabs == 0) && (yabs>2 || yabs == 0))
                    return false;
                //Knight movement is restricted to 2x1
                if((xabs==1 && yabs ==2) || (xabs==2 && yabs==1))
                {
                    return validCapture(position[x1][y1],position[x2][y2]);
                }
                return false;
            case 10: //White Bishop
                for(MovePacket movePacket : getBishopMoves(m.x1,m.y1,position))
                {
                    if(movePacket.x1 == m.x1 && movePacket.y1 == m.y1 &&
                            movePacket.x2 == m.x2 && movePacket.y2 == m.y2 &&
                            movePacket.playerID == m.playerID)
                    {
                        if(validCapture(position[x1][y1],position[x2][y2]))
                            return true;
                    }
                }
                break;
            case 11: //White pawn - RANK 1
                if(y2-y1>0) //Pawns can't move laterally, so it should never need >=
                {
                    //Check if there is a piece on x2 y2
                    //If there is check if it is a diagonal capture, if not then return false
                    if(position[x2][y2]!=12)
                    {
                        //Check for diagonal
                        if(xabs==0)
                        {
                            //Not diagonal so we can't move forward.
                            return false;
                        }
                        else
                        {
                            if(yabs==1 && xabs == 1) //Moving forward diagonally 1. Can't move diagonally by more than 1.
                            { //Should by your standard capture by 1 diagonally.
                                if(validCapture(position[x1][y1],position[x2][y2]))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                    else //No piece is in our way.
                    {
                        if(xabs!=0) //Can't move laterally as a pawn for no reason.
                            return false;
                        if(yabs>=1)
                        {
                            if(y1==1 && yabs <=2) {
                                if(validCapture(position[x1][y1],position[x2][y2]))
                                {
                                    return true;
                                }
                            }
                            else if(yabs==1) {
                                if(validCapture(position[x1][y1], position[x2][y2])){
                                    return true;
                                }
                            }
                        }
                        else
                            return false;
                    }
                }
                break;
        }

        return false;
    }


    /* Give me a move packet and I'll let you know how fucked you are =) */
    public boolean evaluateCheck(MovePacket movePacket, int userID, int[][] currentPosition)
    {
        /* Let's grab some fun stuff, like the move for example! */
        int[][] copyboard = new int[8][8]; //Our copy that we can use to check shit.
        for(int i=0; i<8; i++)
        {
            for(int j=0; j<8; j++)
            {
                copyboard[i][j]=currentPosition[i][j];
            }
        }

        //Now we alter our little baby board
        copyboard[movePacket.x2][movePacket.y2]=copyboard[movePacket.x1][movePacket.y1];
        copyboard[movePacket.x1][movePacket.y1] = 12;

        System.out.println(board.board[movePacket.x2][movePacket.y2]);
        System.out.println(copyboard[movePacket.x2][movePacket.y2]);

        /* This should actually be pretty easy since we already have a method for checking if a move is valid
         * We can just go through each of the other colour's pieces and see if the move from point A to the Kinng is valid
          * GG EZ
          * */
        if(userID==whiteID)
        {
            /* White king ID == 6
            * Black king 0
            * black queen 1
            * black rook 2
            * black knight 3
            * black bishop 4
            * black pawn 5
            * */
            /* Our king's coordinates */
            int kx=0;
            int ky=0;
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++){
                    if(copyboard[i][j]==6){
                        kx=i;
                        ky=j;
                    }
                }
            }
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++) {
                    //I love doing this the easy way.
                    switch (copyboard[i][j]) {
                        case 0:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                        case 1:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                        case 2:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                        case 3:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                        case 4:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                        case 5:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),copyboard)) {
                                return true;
                            }
                            break;
                    }
                }
            }

        }
        else if(userID==blackID)
        {
            /* Black king ID==0
            * White king 6
            * White Queen 7
            * White Rook 8
            * White knight 9
            * White bishop 10
            * white pawn 11
            * */
            int kx=0;
            int ky=0;
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++){
                    if(copyboard[i][j]==0){
                        kx=i;
                        ky=j;
                    }
                }
            }

            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++) {
                    switch (copyboard[i][j]) {
                        case 6:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                        case 7:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                        case 8:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                        case 9:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                        case 10:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                        case 11:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),copyboard)) {
                                return true;
                            }
                            break;
                    }
                }
            }

        }
        return false;
    }
    /* Same as above, but we'll just check the userid to the array passed, instead of a copy */
    public boolean evaluateCheck(int userID, int[][] currentPosition)
    {

        if(userID==whiteID)
        {
            /* White king ID == 6
            * Black king 0
            * black queen 1
            * black rook 2
            * black knight 3
            * black bishop 4
            * black pawn 5
            * */
            /* Our king's coordinates */
            int kx=0;
            int ky=0;
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++){
                    if(currentPosition[i][j]==6){
                        kx=i;
                        ky=j;
                    }
                }
            }
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++) {
                    //I love doing this the easy way.
                    switch (currentPosition[i][j]) {
                        case 0:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 1:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 2:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 3:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 4:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 5:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,blackID),currentPosition)) {
                                return true;
                            }
                            break;
                    }
                }
            }
        }
        else if(userID==blackID)
        {
            /* Black king ID==0
            * White king 6
            * White Queen 7
            * White Rook 8
            * White knight 9
            * White bishop 10
            * white pawn 11
            * */
            int kx=0;
            int ky=0;
            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++){
                    if(currentPosition[i][j]==0){
                        kx=i;
                        ky=j;
                    }
                }
            }

            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++) {
                    switch (currentPosition[i][j]) {
                        case 6:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 7:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 8:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 9:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 10:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                        case 11:
                            if(isValid(new MovePacket(gameID,i,j,kx,ky,whiteID),currentPosition)) {
                                return true;
                            }
                            break;
                    }
                }
            }

        }
        return false;
    }

    /* Restricted to getting king moves specifically */
    public Vector<MovePacket> getKingMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==0) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==6) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;


        /* At max kings can have up to 8 moves plus 2 additional castling moves
         * They can move anywhere from x-1,y-1 to x+1,y+1.
         * Unless they haven't moved then they can still castle.
         * We'll just keep two flags for castling later.
         *
         * */
        //Add all valid moves for non-castling moves.
        for(int i=x-1; i<=x+1; i++)
        {
            if(inBounds(i)) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (inBounds(j))
                    {
                        //Move is in bounds for both directions & is a valid capture.
                        if(validCapture(position[x][y],position[i][j]))
                            validMoves.add(new MovePacket(gameID,x,y,i,j,pid));
                    }
                }
            }
        }
        //TODO add castling moves.

        return validMoves;
    }
    public Vector<MovePacket> getQueenMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==1) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==7) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;
        /* Queens can move laterally and diagonally
         * They can move at most 7 squares.
         * Their xabs & yabs must be equal or one must be 0.
         * We'll just calculate them separately.
         * */

        /* Adding valid Ylateral movements to our list */
        for(int i=0; i<8;i++)
        {
            if(isLateralYValid(y,i,x,position))
            {
                validMoves.add(new MovePacket(gameID,x,y,x,i,pid));
            }
        }
        /* Adding valid XLateral movements */
        for(int i=0; i<8; i++)
        {
            if(isLateralXValid(x,i,y,position)){
                validMoves.add(new MovePacket(gameID,x,y,i,y,pid));
            }
        }

        /* Adding diagonal movements */
        int j=y;
        int i=x;
        while(i<8)
        {
            i++;
            /* We have run into a wall sir */
            j++;
            if(j>=8 || i>=8)
                break;
            if(isDiagonalValid(x,y,i,j,position))
            {
                validMoves.add(new MovePacket(gameID,x,y,i,j,pid));
            }
        }

        /* Adding all up-left moves */
        /* x1 & y2 will be bigger - Need to loop until we hit x=0 or y=7 */
        j=y;
        i=x;
        while(i>=0)
        {
            i--;
            j++;
            if(j>=8 || i<0)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {
                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }

        /* Adding all down-right moves */
        /* x2 & y1 will be bigger - Need to loop until y=0 or x=7 */
        j=y;
        i=x;
        while(i<8)
        {
            i++;
            j--;
            if(j<0 || i>=8)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {
                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }

        /* Adding all down-left moves */
        /* x1 & y1 will be larger - Loop until we hit x=0 or y=0 */
        j=y;
        i=x;
        while(i>=0)
        {
            i--;
            j--;
            if(j<0 || i<0)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {

                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }


        return validMoves;
    }

    /* Rook movements complete */
    public Vector<MovePacket> getRookMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==2) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==8) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;

        /* Adding valid Ylateral movements to our list */
        for(int i=0; i<8;i++)
        {
            if(isLateralYValid(y,i,x,position))
            {
                validMoves.add(new MovePacket(gameID,x,y,x,i,pid));
            }
        }
        /* Adding valid XLateral movements */
        for(int i=0; i<8; i++)
        {
            if(isLateralXValid(x,i,y,position)){
                validMoves.add(new MovePacket(gameID,x,y,i,y,pid));
            }
        }

        return validMoves;
    }
    public Vector<MovePacket> getKnightMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==3) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==9) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;
        return validMoves;
    }
    public Vector<MovePacket> getBishopMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==4) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==10) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;

        /* Adding all up-right moves */
        int j=y;
        int i=x;
        while(i<8)
        {
            i++;
            /* We have run into a wall sir */
            j++;
            if(j>=8 || i>=8)
                break;
            if(isDiagonalValid(x,y,i,j,position))
            {
                validMoves.add(new MovePacket(gameID,x,y,i,j,pid));
            }
        }

        /* Adding all up-left moves */
        /* x1 & y2 will be bigger - Need to loop until we hit x=0 or y=7 */
        j=y;
        i=x;
        while(i>=0)
        {
            i--;
            j++;
            if(j>=8 || i<0)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {
                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }

        /* Adding all down-right moves */
        /* x2 & y1 will be bigger - Need to loop until y=0 or x=7 */
        j=y;
        i=x;
        while(i<8)
        {
            i++;
            j--;
            if(j<0 || i>=8)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {
                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }

        /* Adding all down-left moves */
        /* x1 & y1 will be larger - Loop until we hit x=0 or y=0 */
        j=y;
        i=x;
        while(i>=0)
        {
            i--;
            j--;
            if(j<0 || i<0)
                break;
            if(isDiagonalValid(x,y,i,j,position)) {

                validMoves.add(new MovePacket(gameID, x, y, i, j, pid));
            }
        }

        return validMoves;
    }
    public Vector<MovePacket> getPawnMoves(int x, int y, int[][] position)
    {
        Vector<MovePacket> validMoves = new Vector<MovePacket>();
        /* Let's grab our colour real quick to send in our MovePacket */
        int pid=0;
        if(position[x][y]==5) //Black
        {
            pid=blackID;
        }
        else if(position[x][y]==11) //white
        {
            pid=whiteID;
        }
        else //What?
            return null;

        /* Let's also make sure x & y are in bounds */
        if(!inBounds(x) || !inBounds(y))
            return null;
        /* This is the only one where our colour matters for which direction we can travel */
        return validMoves;
    }

    /* For the given piece in the position, evaluate every valid move */
    public Vector<MovePacket> getMoves(int x, int y, int[][] position)
    {
        /* Check if it's within bounds real quick; */
        if(!(x<=7 && x>=0 && y>=0 && y<=7))
            return null;
        switch(position[x][y]){
            case 0: //Black king
                return getKingMoves(x,y,position);
            case 1: //Black queen
                break;
            case 2: //Black rook
                break;
            case 3: //Black knight
                break;
            case 4: //Black bishop
                break;
            case 5: //Black pawn
                break;
            case 6: //White king
                return getKingMoves(x,y,position);
            case 7: //White queen
                break;
            case 8: //White rook
                break;
            case 9: //white knight
                break;
            case 10: //White bishop
                break;
            case 11: //White pawn
                break;
            case 12: //It's a fucking blank space. It has no moves.
                break;
        }
        return null;
    }


    public boolean isCheckmate(int playerID, int[][] position)
    {
        /* This is annoying to make efficient...
        * Do we have to check every single move from the player? To see if it gets them out of check? How the fuck do we do that.
        * - - I guess we can create a function to return valid moves? An array of move packets?
        *
        *
        * */

        //Let's grab the player colour
        if(playerID==whiteID)
        {
            //Checking for the other colour's IDs.
            /* White king ID == 6
             * Black king 0
             * black queen 1
             * black rook 2
             * black knight 3
             * black bishop 4
             * black pawn 5
             * */

            for(int i=0; i<8; i++)
            {
                for(int j=0; j<8; j++)
                {
                    // For each of the player's pieces, we need to evaluate if any of them can make a valid move to get out of check.
                    switch (position[i][j])
                    {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                    }
                }
            }

        }
        else if(playerID==blackID)
        {


        }
        return false;
    }

    /* We'll use this later to check if both players have enough material to go on - We can only continue if ONE player has enough to checkmate
     *
      * We can do this if we assign each piece a value
      * King 0 - Everything
      * Queen - 9
      * Rook - 5
      * Knight & Bishop - 3
      * Pawn - 1
      *
      * As long as a player has a pawn, they can continue, but if they have 0 pawns & less than 5 points of material, they can't checkmate.
      * We'll just loop through and keep a count of the numbers of each piece available.
      *
      *
      * */
    public boolean materialCheck(int playerID, int[][] position)
    {

        return false;
    }


    public void tryMove(MovePacket m)
    {

        //How about we check the fucking game id....that's a good start @.o
        if(m.gameID==gameID) {
            if (currentTurnID == m.playerID)
                if (isValid(m,board.board)) {
                    if(!evaluateCheck(m,currentTurnID,board.board)) {
                        promotionCheck(m);
                        updateBoard(m);

                        //TODO I think this is a good place to evaluate if the other player is now in check, if they are we can code for checkmate....not sure how yet.
                        /* Because updateBoard switches turn ID, we can just use it again */
                        /*/if(evaluateCheck(m,currentTurnID,board.board))
                        {
                            if(isCheckmate(currentTurnID,board.board))
                            {
                                //TODO send out a GameEnd packet with checkmate as the reason.

                            }
                        }*/
                    }
                }
        }
    }

    //Going to have this be called by the listener
    public void updateBoard(MovePacket m)
    {
        /* Update board position */
        board.board[m.x2][m.y2] = board.board[m.x1][m.y1]; //TODO code for captures. Should just overwrite currently clientside.
        board.board[m.x1][m.y1] = 12; //Empty space now that it's empty.
        /* Notify players of the move */
        MovePacket packet = new MovePacket(gameID,board.boardID,m.x1, m.y1, m.x2, m.y2,true);
        player1.connection.sendTCP(packet);
        player2.connection.sendTCP(packet);
        if(turnshift)
            switchTurns();
    }

    //Only called if one of the players disconnect.
    public void disconnected(int id)
    {
        System.out.println("Disconnected from game listener!");
        if(id==player1.connection.getID())
        {
            player2.connection.sendTCP(new GameEndPacket(true, gameID, player2.connection.getID(), player2.nick, "Opponent disconnected! Victory =D"));
        }
        else
            player1.connection.sendTCP(new GameEndPacket(true, gameID, player1.connection.getID(), player1.nick, "Opponent disconnected! Victory =D"));

        closeGame();
    }
    //Deconstructor of sorts.
    public void closeGame()
    {
        network.server.removeListener(listener1);
        network.server.removeListener(listener2);
        //We need to remove listeners, but we don't know what ID they use for listeners.
        network.gameRooms.remove(this);
    }
}
