package Packets;

import java.lang.reflect.Array;

/**
 *
 */
public class BoardPosition {
    public boolean request;
    public int gameID;
    public int boardID; //Prep for bughouse chess.
    public int[][] board;
    public BoardPosition(){}
    public BoardPosition(int id, boolean request)
    {
        this.gameID=id;
        this.request = request;
    }
    /* The assumed constructor for single board games */
    public BoardPosition(int gameID) {
        //We are assuming that this is an 8x8 board map.
        this.gameID = gameID;
        this.board = new int[8][8];
        this.boardID = -1;
        /* Send standard board state */
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 0) //x=0 aka top?
                {
                    switch (i) {
                        case 0: //White Rook
                            this.board[i][j] = 8;
                            break;
                        case 1: //White Knight
                            this.board[i][j] = 9;
                            break;
                        case 2: //White Bishop
                            this.board[i][j] = 10;
                            break;
                        case 3: //White Queen
                            this.board[i][j] = 7;
                            break;
                        case 4: //White King
                            this.board[i][j] = 6;
                            break;
                        case 5:  //White Bishop
                            this.board[i][j] = 10;
                            break;
                        case 6: //White Knight
                            this.board[i][j] = 9;
                            break;
                        case 7: //White Rook
                            this.board[i][j] = 8;
                            break;
                    }
                } else if (j == 6)
                    this.board[i][j] = 5; //Black pawn
                else if (j == 1)
                    this.board[i][j] = 11; //White pawn
                else if (j == 7) {
                    switch (i) {
                        case 0: //Black Rook
                            this.board[i][j] = 2;
                            break;
                        case 1: //Black Knight
                            this.board[i][j] = 3;
                            break;
                        case 2: //Black Bishop
                            this.board[i][j] = 4;
                            break;
                        case 4: //Black King
                            this.board[i][j] = 0;
                            break;
                        case 3: //Black Queen
                            this.board[i][j] = 1;
                            break;
                        case 5:  //Black Bishop
                            this.board[i][j] = 4;
                            break;
                        case 6: //Black Knight
                            this.board[i][j] = 3;
                            break;
                        case 7: //Black Rook
                            this.board[i][j] = 2;
                            break;
                    }
                } else
                    this.board[i][j] = 12; //Empty space
            }
        }
    }
}
