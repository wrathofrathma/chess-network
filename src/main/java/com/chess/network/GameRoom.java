package com.chess.network;

import Packets.*;

import java.util.Random;

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
        System.out.println("Testing: " + packet.pawnx + " " + packet.pawny);
        if(inBounds(packet.pawnx) && packet.pawny==0)
        {
            System.out.println("Promotion is in bounds!");
            //Black's pawn is on white's back rank
            if(board.board[packet.pawnx][packet.pawny]==5){
                System.out.println("Black's pawn is on white's back rank");
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
    public boolean isLateralXValid(int x1, int x2, int y)
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
                if(board.board[i][y]!=12)
                {
                    return false;
                }
            }
        }
        else if(x1<x2)
        {
            for(int i=++x1; i<x2;i++) // We only need to account for up UNTIL the landing square.
            {
                if(board.board[i][y]!=12)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isLateralYValid(int y1, int y2, int x)
    {
        if(y1==y2)
            return true;
        if(y1>y2)
        {
            for(int i=++y2; i<y1;i++) // We only need to account for up UNTIL the landing square.
            {
                if(board.board[x][i]!=12)
                {
                    return false;
                }
            }
        }
        else if(y1<y2)
        {
            for(int i=++y1; i<y2;i++) // We only need to account for up UNTIL the landing square.
            {
                if(board.board[x][i]!=12)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isDiagonalValid(int x1, int y1, int x2, int y2)
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
/*
        int newx=0;
        int newy=0;
        System.out.println("Moving from: (" + x1 + "," + y1+") to (" + x2 + ","+y2+")");
        if(x1>x2) {
            for (int i = 1; i <(x1-x2);i++)
            {

                newx = (int) (x1 + (x2-x1)*(1.0/(x2-x1))*i);
                newy = (int)(y1 + (y2-y1)*(1.0/(y2-y1))*i);
                System.out.println("Moving from: (" + x1 + "," + y1+") to (" + x2 + ","+y2+"). Checking ("+newx + "," + newy+")");

                if(board.board[newx][newy]!=12) {
                    System.out.println("Collision on : " +newx + " "+ newy);
                    return false;
                }
                System.out.println("Moving from: (" + x1 + "," + y1+") to (" + x2 + ","+y2+"). Checking ("+newx + "," + newy+")");
            }
        }
        else {
            for (int i = 1; i <(x2-x1);i++)
            {
                newx = (int) (x1 + (x2-x1)*(1.0/(x2-x1))*i);
                newy = (int)(y1 + (y2-y1)*(1.0/(y2-y1))*i);
                System.out.println("Moving from: (" + x1 + "," + y1+") to (" + x2 + ","+y2+"). Checking ("+newx + "," + newy+")");

                if(board.board[newx][newy]!=12) {
                    System.out.println("Collision on : " +newx + " "+ newy);

                    return false;
                }
            }
        }*/
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
                    if(board.board[i][j]!=12)
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
                    if(board.board[i][j]!=12) {
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
                    if(board.board[i][j]!=12) {
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
                    if(board.board[i][j]!=12)
                        return false;
                }
            }

        }
        return true;
    }

    public boolean promotionCheck(MovePacket m)
    {
        if((board.board[m.x1][m.y1]==5 && m.y2==0) ||(board.board[m.x1][m.y1]==11 && m.y2==7)){
            turnshift=false;
            network.server.sendToTCP(m.playerID,new PromotionPacket(m.x2,m.y2,0,gameID,0));
            return true;
        }
        return false;
    }
    public boolean isValid(MovePacket m)
    {

        //TODO OH GOD. The dreaded logic segment.
        /* Use x1 y1 to grab current piece. Figure out all valid moves then check against the suggested move */
        int piece = board.board[m.x1][m.y1]; //Piece trying to be moved.
        int x1 = m.x1;
        int x2 = m.x2;
        int y1 = m.y1;
        int y2 = m.y2;
        int xabs = Math.abs(x1-x2);
        int yabs = Math.abs(y1-y2);
        if(!inBounds(x2) || !inBounds(y2)) //May as well check this first.
            return false;
        /* We should probably check if you're trying to move your own fucking piece this time around */
        if(board.board[m.x1][m.y1]>=0 && board.board[m.x1][m.y1]<=5) //Piece being moved is black
        {
            if(m.playerID!=blackID)
                return false;
        }
        else if(board.board[m.x1][m.y1]>=6 && board.board[m.x1][m.y1]<=11) //Piece being moved is white
        {
            if(m.playerID!=whiteID)
                return false;
        }


        /* Let's set up move logic for individual pieces before checking threats */
        switch(piece){
            case 0: //Black King
                /* Can move 1 square in any direction - Check for bounds */
                if(xabs<=1 && xabs >= 0 && yabs >=0 && yabs<=1)
                {
                    //If absolute value is 1, then he's only moved one space. Now we must check the bounds, then the threats.
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 1: //Black Queen
                /* First we'll just test what KIND of movement it is */
                if(xabs==0 || yabs==0) //A lateral movement
                {
                    if(isLateralXValid(x1,x2,y1) && isLateralYValid(y1,y2,x1))
                    {
                        return validCapture(board.board[x1][y1],board.board[x2][y2]);
                    }
                }
                else //Really only diagonal possible.
                {
                    if(isDiagonalValid(x1,y1,x2,y2))
                    {
                        return validCapture(board.board[x1][y1],board.board[x2][y2]);
                    }
                }
                break;
            case 2: //Black Rook
                if((xabs!=0) && (yabs!=0))
                    return false;
                if(isLateralXValid(x1,x2,y1) && isLateralYValid(y1,y2,x1))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 3: //Black Knight
                //Knights can only move 2 spaces in any direction & can never land on a tile on the same X or Y it started.
                if((xabs>2 || xabs == 0) && (yabs>2 || yabs == 0))
                    return false;
                //Knight movement is restricted to 2x1
                if((xabs==1 && yabs ==2) || (xabs==2 && yabs==1))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                return false;
            case 4: //Black Bishop
                if(isDiagonalValid(x1,y1,x2,y2))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 5: //Black Pawn
                if(y1-y2>0) //Pawns can't move laterally, so it should never need >=
                {
                    if(board.board[x2][y2]!=12)
                    {
                        //Check for diagonal
                        if(xabs==0)
                        {
                            //Not diagonal so we can't move forward.
                            System.out.println("There is something in your path!");
                            return false;
                        }
                        else
                        {
                            if(yabs==1 && xabs == 1) //Moving forward diagonally 1. Can't move diagonally by more than 1.
                            { //Should by your standard capture by 1 diagonally.
                                if(validCapture(board.board[x1][y1],board.board[x2][y2])) {
                                    promotionCheck(m); //Just needs to set a flag.
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
                                if(validCapture(board.board[x1][y1],board.board[x2][y2]))
                                {
                                    promotionCheck(m);
                                    return true;
                                }
                            }
                            else if(yabs==1) {
                                if(validCapture(board.board[x1][y1], board.board[x2][y2]))
                                {
                                    promotionCheck(m);
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
                if(xabs<=1 && xabs >= 0 && yabs >=0 && yabs<=1)
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 7: //White Queen
                /* First we'll just test what KIND of movement it is */
                if(xabs==0 || yabs==0) //A lateral movement
                {
                    if(isLateralXValid(x1,x2,y1) && isLateralYValid(y1,y2,x1))
                    {
                        return validCapture(board.board[x1][y1],board.board[x2][y2]);
                    }
                }
                else //Really only diagonal possible.
                {
                    if(isDiagonalValid(x1,y1,x2,y2))
                    {
                        return validCapture(board.board[x1][y1],board.board[x2][y2]);
                    }
                }
                break;
            case 8: //White Rook
                //We need to check if he is ONLY moving along one file.
                if((xabs!=0) && (yabs!=0)) //If it hasn't moved along the file the absolute value will be 0. Both being !=0 means it's moving along both axis.
                    return false;
                if(isLateralXValid(x1,x2,y1) && isLateralYValid(y1,y2,x1))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 9: //White Knight
                //Knights can only move 2 spaces in any direction & can never land on a tile on the same X or Y it started.
                if((xabs>2 || xabs == 0) && (yabs>2 || yabs == 0))
                    return false;
                //Knight movement is restricted to 2x1
                if((xabs==1 && yabs ==2) || (xabs==2 && yabs==1))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                return false;
            case 10: //White Bishop
                if(isDiagonalValid(x1,y1,x2,y2))
                {
                    return validCapture(board.board[x1][y1],board.board[x2][y2]);
                }
                break;
            case 11: //White pawn - RANK 1
                if(y2-y1>0) //Pawns can't move laterally, so it should never need >=
                {
                    //Check if there is a piece on x2 y2
                    //If there is check if it is a diagonal capture, if not then return false
                    if(board.board[x2][y2]!=12)
                    {
                        //Check for diagonal
                        if(xabs==0)
                        {
                            //Not diagonal so we can't move forward.
                            System.out.println("There is something in your path!");
                            return false;
                        }
                        else
                        {
                            if(yabs==1 && xabs == 1) //Moving forward diagonally 1. Can't move diagonally by more than 1.
                            { //Should by your standard capture by 1 diagonally.
                                if(validCapture(board.board[x1][y1],board.board[x2][y2]))
                                {
                                    promotionCheck(m);
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
                                if(validCapture(board.board[x1][y1],board.board[x2][y2]))
                                {
                                    promotionCheck(m);
                                    return true;
                                }
                            }
                            else if(yabs==1) {
                                if(validCapture(board.board[x1][y1], board.board[x2][y2])){
                                    promotionCheck(m);
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

    /* Call after a move, to see if the move put a player in check. Probably can use a player.state for tracking it? */
    public boolean inCheck(int userID)
    {
        /* Get player colour
         * Grab King position
         * check if any opposing coloured pieces have any valid moves onto the king....This could get inefficient fast.
         *
         *
         * */

        return evaluateCheck(board.board);
    }

    public boolean evaluateCheck(int[][] b)
    {
        //This is where we will do the heavy lifting of this. Both inCheck and checkAfterMove should call with an array.
        return false;
    }

    /* We'll call you before a move, checking for pins & blocking checks, breaking checks. */
    public boolean checkAfterMove(int userid, int x1, int x2, int y1, int y2)
    {
        /* We'll create a "fake" array, but do it in a ghetto way so we don't constantly reinitialise the array
         * Then make the move and see if player is in check?
         */



        return false;
    }


    public void tryMove(MovePacket m)
    {
        /*
         * If move isValid(m) returns true then
         *      * updateBoard()
         *      * Change turn.
         * Else do nothing.
         */
        //TODO check if it's your move && check if it's a valid move.
        //Check turn order clientside. This will allow us to set up things like bughouse chess in the future.
        //How about we check the fucking game id....that's a good start @.o
        if(m.gameID==gameID) {
            if (currentTurnID == m.playerID)
                if (isValid(m)) {
                    updateBoard(m);
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
