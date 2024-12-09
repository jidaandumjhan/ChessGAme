package game.pieces;

import java.util.ArrayList;

import game.Move;
import game.Position;
import game.Square;

/**
 * An abstract representation of a chess piece.
 */
abstract public class Piece {

    /** The current square of the piece. */
    protected Square square;

    /** The color of the piece. True if white, false if black. */
    protected final boolean white;

    /**
     * Creates a new Piece object.
     * 
     * @param file  The file (column) the piece is on.
     * @param rank  The rank (row) the piece is on.
     * @param white Whether the piece is white or not. (True if white, false if
     *              black)
     */
    public Piece(int file, int rank, boolean white) {

        this.square = new Square(file, rank);
        this.white = white;

    }

    /**
     * Gets the square that the piece is on.
     * 
     * @return The {@link Square} the piece is on.
     */
    public Square getSquare() {
        return square;
    }

    /**
     * Sets the square of the piece.
     * 
     * @param square The square to set the piece to.
     */
    public void setSquare(Square square) {
        this.square = square;
    }

    /**
     * Gets whether or not the piece is white.
     * 
     * @return If the piece is white.
     */
    public boolean isWhite() {
        return white;
    }

    /**
     * Checks if the given {@link Piece} object matches this {@link Piece} object.
     * 
     * @param compare The {@link Piece} object to compare
     * @return Whether or not the pieces are the same
     */
    @Override
    public boolean equals(Object compare) {

        if (!(compare instanceof Piece))
            return false;

        Piece casted = (Piece) (compare);

        return (casted.getCode() == getCode()) && isWhite() == casted.isWhite() && square == casted.getSquare();

    }

    /**
     * Gets the code of the piece.
     * 
     * <ul>
     * <li>K = King
     * <li>Q = Queen
     * <li>R = Rook
     * <li>B = Bishop
     * <li>N = Knight
     * <li>P = Pawn
     * </ul>
     * 
     * @return The piece code.
     */
    abstract public char getCode();

    /**
     * Gets the number of points the piece is worth.
     * 
     * @return The amount of points.
     */
    abstract public int getPoints();

    /**
     * Gets all moves that a given piece can make, regardless of if they are
     * possible in the context of the game.
     * 
     * @param position The position this piece is a part of.
     * @return A list of valid moves. Does not account for check.
     */
    abstract public ArrayList<Move> getMoves(Position position);

    /**
     * Gets the vertical moves possible for the piece.
     * 
     * @param distance         The number of squares the piece may move. If there is
     *                         no limit, enter 0 or 8.
     * @param position         The current {@link Position} the piece is on.
     * @param includeBackwards Should backwards moves be included? (e.g. not for
     *                         pawns)
     * @return A list of valid vertical moves for the piece. Does not account for
     *         check, but
     *         does account for capturing/pieces in the way.
     */
    protected ArrayList<Move> getVerticalMoves(int distance, Position position, boolean includeBackwards) {

        ArrayList<Move> moves = new ArrayList<Move>();

        int file = square.getFile();
        int rank = square.getRank();

        if (distance <= 0)
            distance = 8;

        for (int i = 1; i < distance + 1; i++) {

            final Move move;

            try {

                move = new Move(square, new Square(file, white ? rank + i : rank - i), position);

            } catch (Exception e) {

                if (e.getMessage().equals("Cannot capture your own piece.") || e.getMessage().equals(
                        "Cannot capture going forward."))
                    break;

                continue;

            }

            moves.add(move);

            if (move.isCapture())
                break;

        }

        if (includeBackwards) {

            distance = -distance;
            for (int i = -1; i > distance - 1; i--) {

                final Move move;

                try {

                    move = new Move(square, new Square(file, white ? rank + i : rank - i), position);

                } catch (Exception e) {

                    if (e.getMessage().equals("Cannot capture your own piece."))
                        break;

                    continue;
                }

                moves.add(move);

                if (move.isCapture())
                    break;

            }

        }

        return moves;
    }

    /**
     * Gets the horizontal moves possible for the piece.
     * 
     * @param distance The number of squares the piece may move. If there is
     *                 no limit, enter 0 or 8.
     * @param position The current {@link Position} the piece is on.
     * @return A list of valid horizontal moves for the piece. Does not account for
     *         check, but
     *         does account for capturing/pieces in the way.
     */
    protected ArrayList<Move> getHorizontalMoves(int distance, Position position) {

        ArrayList<Move> moves = new ArrayList<Move>();

        int file = square.getFile();
        int rank = square.getRank();

        if (distance <= 0)
            distance = 8;

        // Right
        for (int i = 1; i < distance + 1; i++) {

            final Move move;

            try {

                move = new Move(square, new Square(white ? file + i : file - i, rank), position);

            } catch (Exception e) {

                if (e.getMessage().equals("Cannot capture your own piece."))
                    break;

                continue;

            }

            moves.add(move);

            if (move.isCapture())
                break;

        }

        // Left
        distance = -distance;
        for (int i = -1; i > distance - 1; i--) {

            Move move;
            try {

                move = new Move(square, new Square(white ? file + i : file - i, rank), position);

            } catch (Exception e) {

                if (e.getMessage().equals("Cannot capture your own piece."))
                    break;

                continue;

            }

            moves.add(move);

            if (move.isCapture())
                break;

        }

        return moves;
    }

    /**
     * Gets the diagonal moves possible for the piece.
     * 
     * @param distance The number of squares the piece may move. If there is
     *                 no limit, enter 0 or 8.
     * @param position The current {@link Position} the piece is on.
     * @return A list of valid diagonal moves for the piece. Does not account for
     *         check, but
     *         does account for capturing/pieces in the way.
     */
    protected ArrayList<Move> getDiagonalMoves(int distance, Position position) {

        ArrayList<Move> moves = new ArrayList<Move>();

        int file = square.getFile();
        int rank = square.getRank();

        if (distance <= 0)
            distance = 8;

        boolean color = white ? true : false;

        for (int l = 0; l < 2; l++, color = !color, distance = -distance) {

            // Up
            for (int i = 1; i < distance + 1; i++) {

                final Move move;
                try {

                    move = new Move(square, new Square(color ? file + i : file - i, color ? rank + i : rank - i),
                            position);

                } catch (Exception e) {

                    if (e.getMessage().equals("Cannot capture your own piece."))
                        break;

                    continue;

                }

                moves.add(move);

                if (move.isCapture())
                    break;

            }

            // Down
            distance = -distance;
            for (int i = -1; i > distance - 1; i--) {

                final Move move;
                try {

                    move = new Move(square, new Square(color ? file + i : file - i, color ? rank - i : rank + i),
                            position);

                } catch (Exception e) {

                    if (e.getMessage().equals("Cannot capture your own piece."))
                        break;

                    continue;

                }

                moves.add(move);

                if (move.isCapture())
                    break;

            }

        }

        return moves;
    }

}
