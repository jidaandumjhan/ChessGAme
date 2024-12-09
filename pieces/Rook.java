package game.pieces;

import java.util.ArrayList;

import game.Move;
import game.Position;

/**
 * A representation of a rook.
 */
public class Rook extends Piece {

    /**
     * Creates a new Rook object.
     * 
     * @param file  The file (column) the piece is on.
     * @param rank  The rank (row) the piece is on.
     * @param white Whether the piece is white or not. (True if white, false if
     *              black)
     */
    public Rook(int file, int rank, boolean white) {
        super(file, rank, white);
    }

    public char getCode() {
        return 'R';
    }

    public int getPoints() {
        return 5;
    }

    public ArrayList<Move> getMoves(Position p) {

        ArrayList<Move> moves = new ArrayList<Move>();

        moves.addAll(getVerticalMoves(0, p, true));
        moves.addAll(getHorizontalMoves(0, p));

        return moves;

    }

}
