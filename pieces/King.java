package game.pieces;

import java.util.ArrayList;

import game.Move;
import game.Position;

/**
 * A representation of a king.
 */
public class King extends Piece {

    /**
     * Creates a new King object.
     * 
     * @param file  The file (column) the piece is on.
     * @param rank  The rank (row) the piece is on.
     * @param white Whether the piece is white or not. (True if white, false if
     *              black)
     */
    public King(int file, int rank, boolean white) {
        super(file, rank, white);
    }

    public char getCode() {
        return 'K';
    }

    public int getPoints() {
        return 0;
    }

    public ArrayList<Move> getMoves(Position p) {

        ArrayList<Move> moves = new ArrayList<Move>();

        moves.addAll(getVerticalMoves(1, p, true));
        moves.addAll(getHorizontalMoves(1, p));
        moves.addAll(getDiagonalMoves(1, p));



        return moves;

    }

}
