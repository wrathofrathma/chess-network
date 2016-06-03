package com.chess.network;

/**
 * An abstracted class to make moves easier to work with.
 */
public class Move {
    enum PIECE {
        KING,
        QUEEN,
        ROOK,
        KNIGHT,
        BISHOP,
        PAWN
    }
    enum COLOUR {
        WHITE,
        BLACK
    }
    COLOUR colour;
    PIECE piece;
    int x1, y1, x2, y2;
    public Move(int x1, int y1, int x2, int y2, int[][] position)
    {
        /* Let's just contain all of the logic here and extract everything */
        int pieceID1 = position[x1][y1];
        int pieceID2 = position[x2][y2];
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        switch(pieceID1){
            case 0:
                colour=COLOUR.BLACK;
                piece=PIECE.KING;
                break;
            case 1:
                colour=COLOUR.BLACK;
                piece=PIECE.QUEEN;
                break;
            case 2:
                colour=COLOUR.BLACK;
                piece=PIECE.ROOK;
                break;
            case 3:
                colour=COLOUR.BLACK;
                piece=PIECE.KNIGHT;
                break;
            case 4:
                colour=COLOUR.BLACK;
                piece=PIECE.BISHOP;
                break;
            case 5:
                colour=COLOUR.BLACK;
                piece=PIECE.PAWN;
                break;
            case 6:
                colour=COLOUR.WHITE;
                piece=PIECE.KING;
                break;
            case 7:
                colour=COLOUR.WHITE;
                piece=PIECE.QUEEN;
                break;
            case 8:
                colour=COLOUR.WHITE;
                piece=PIECE.ROOK;
                break;
            case 9:
                colour=COLOUR.WHITE;
                piece=PIECE.KNIGHT;
                break;
            case 10:
                colour=COLOUR.WHITE;
                piece=PIECE.BISHOP;
                break;
            case 11:
                colour=COLOUR.WHITE;
                piece=PIECE.PAWN;
                break;
            case 12:
                //This is for empty space.
                break;

        }
    }

    @Override
    public String toString() {
        return colour + " " + piece;
    }
}
