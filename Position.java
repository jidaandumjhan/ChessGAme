package game;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import game.pieces.Bishop;
import game.pieces.King;
import game.pieces.Knight;
import game.pieces.Pawn;
import game.pieces.Piece;
import game.pieces.Queen;
import game.pieces.Rook;

/**
 * A board position of a {@link Game}. Contains the pieces and their positions,
 * as well as the moves that can be made from this position.
 */
public class Position {

    /**
     * Generates the given Chess960 starting position using the algorithm found
     * here:
     * <a href=
     * "https://en.wikipedia.org/wiki/Fischer_random_chess_numbering_scheme#Direct_derivation">https://en.wikipedia.org/wiki/Fischer_random_chess_numbering_scheme#Direct_derivation</a>
     * 
     * <p>
     * A list of all start positions can be found here:
     * <a href=
     * "https://www.mark-weeks.com/cfaa/chess960/c960strt.htm">https://www.mark-weeks.com/cfaa/chess960/c960strt.htm</a>
     * 
     * @param startId the ID (from 0-959) of the Chess960 starting position.
     * @return A Chess960 starting position in Forsyth-Edwards Notation (FEN) based
     *         on the given id.
     * @throws RuntimeException If startId is not between 0-959 (inclusive).
     */
    public static String generate960Start(int startId) throws RuntimeException {

        if (startId < 0 || startId > 959)
            throw new RuntimeException("Invalid startId.");

        char[] pcs = new char[8];

        final int n2 = startId / 4;
        final int b1 = startId % 4;

        // Place light-square bishop
        switch (b1) {
            case 0:
                pcs[1] = 'B';
                break;
            case 1:
                pcs[3] = 'B';
                break;
            case 2:
                pcs[5] = 'B';
                break;
            case 3:
                pcs[7] = 'B';
                break;
        }

        final int n3 = n2 / 4;
        final int b2 = n2 % 4;

        // Place dark-square bishop
        switch (b2) {
            case 0:
                pcs[0] = 'B';
                break;
            case 1:
                pcs[2] = 'B';
                break;
            case 2:
                pcs[4] = 'B';
                break;
            case 3:
                pcs[6] = 'B';
                break;
        }

        final int n4 = n3 / 6;
        final int q = n3 % 6;

        // Place queen in the [q]th open square
        int iq = 0;
        int zq = 0;
        while (iq <= q) {

            if (pcs[zq] == '\u0000')
                iq++;

            if (iq <= q)
                zq++;

        }

        pcs[zq] = 'Q';

        // Place Knights based on the N5N table
        final int[] n5nTable1 = { 0, 0, 0, 0, 1, 1, 1, 2, 2, 3 };
        final int[] n5nTable2 = { 1, 2, 3, 4, 2, 3, 4, 3, 4, 4 };

        final int knight1 = n5nTable1[n4];
        final int knight2 = n5nTable2[n4];

        // Place first knight in the [knight1]th open square
        int in1 = 0;
        int zn1 = 0;
        while (in1 <= knight1) {

            if (pcs[zn1] == '\u0000')
                in1++;

            if (in1 <= knight1)
                zn1++;

        }

        // Place second knight in the [knight2]th open square
        int in2 = 0;
        int zn2 = 0;
        while (in2 <= knight2) {

            if (pcs[zn2] == '\u0000')
                in2++;

            if (in2 <= knight2)
                zn2++;

        }

        pcs[zn1] = 'N';
        pcs[zn2] = 'N';

        int x = 0;

        // Place rook in first open square
        while (x < 8 && pcs[x] != '\u0000') {
            ++x;
        }

        pcs[x] = 'R';
        char rookAFile = (char) (x + 97);

        ++x;

        // Place king in middle open square
        while (x < 8 && pcs[x] != '\u0000') {
            ++x;
        }

        pcs[x] = 'K';
        ++x;

        // Place rook in last open square
        while (x < 8 && pcs[x] != '\u0000') {
            ++x;
        }

        pcs[x] = 'R';

        char rookHFile = (char) (x + 97);

        String castleRights = "" + rookAFile
                + rookHFile;

        if (castleRights.equals("AHah"))
            castleRights = "KQkq";

        String fenRow = new String(pcs);

        String fen = fenRow.toLowerCase() + "/pppppppp/8/8/8/8/PPPPPPPP/" + fenRow + " w " + castleRights.toUpperCase()
                + castleRights + " - 0 1";

        return fen;

    }

    /**
     * The number of moves made in this game (including the move that led to
     * this position.) Starts at {@code 0} for the default position as no moves have
     * been made.
     */
    private int moveNumber;

    /**
     * A 2D array that matches the board and stores the pieces.
     */
    private Piece[][] pieces;

    /**
     * All of the moves that can be made. If {@link #mateChecked} is {@code true}
     * when constructor is called, moves that lead to check will not be included.
     */
    private ArrayList<Move> moves;

    /** The move that led to this position. */
    private Move move;

    /** The position of the white king. */
    private Square whiteKing;

    /** The position of the black king. */
    private Square blackKing;

    /**
     * Whether or not white can a-side castle.
     */
    private boolean whiteASide;

    /**
     * Whether or not white can h-side castle.
     */
    private boolean whiteHSide;

    /**
     * Whether or not black can a-side castle.
     */
    private boolean blackASide;

    /**
     * Whether or not black can h-side castle.
     */
    private boolean blackHSide;

    /**
     * The starting file of the a-side rooks. In a normal game, typically would be 1
     * (a). In Chess960, however, this may differ.
     */
    private int aSideRookFile;

    /**
     * The starting file of the h-side rooks. In a normal game, typically would be 8
     * (h). In Chess960, however, this may differ.
     */
    private int hSideRookFile;

    /**
     * Whether or not it is currently white's turn, not whether white made the move
     * that led to this position. Will be the opposite of {@link Move#isWhite()}.
     */
    private boolean white;

    /** If {@link #isWhite()} is currently giving check. */
    private boolean givingCheck;

    /** If {@link #isWhite()} is currently in check. */
    private boolean inCheck;

    /**
     * If {@link #isWhite()} is checkmated. Will only be set if {@link #mateChecked}
     * is {@code true} when constructor is called.
     */
    private boolean checkMate;

    /**
     * The {@link Position} that was previously after this {@link Position}. May be
     * {@code null} if there is no position to redo.
     */
    private Position redo;

    /**
     * The promote type of the redo move. May be {@code null} if there is no
     * position to
     * redo.
     * 
     * @see #redo
     */
    private char redoPromote;

    /**
     * The timer end of the previous position, saved for if this position is
     * restored by calling {@link Game#redo()}.
     * 
     * @see #redo
     */
    private long redoTimerEnd;

    /**
     * The amount of time on the timer at the end of this position.
     */
    private long timerEnd;

    /**
     * Counter that counts the number of moves made since the last time a pawn was
     * moved or a capture was made. Once 50 moves have been completed (100 turns),
     * the game will be declared a draw. This number includes the move that led to
     * the current position. It will count up to 100.
     */
    private int fiftyMoveCounter;

    /**
     * The square that can be en-passanted from this position.
     */
    private Square enPassantTarget;

    /** Whether or not mate has been checked. */
    private boolean mateChecked;

    /**
     * The opening that led to this position. May be null if this position was
     * created from a custom FEN or is in the starting position.
     */
    private Opening opening;

    /**
     * Creates a new {@link Position} object in the default starting position.
     */
    public Position() {

        white = true;
        this.mateChecked = false;

        this.moveNumber = 0;
        this.timerEnd = -1;
        this.fiftyMoveCounter = 0;

        this.whiteASide = true;
        this.whiteHSide = true;
        this.blackASide = true;
        this.blackHSide = true;

        this.aSideRookFile = 1;
        this.hSideRookFile = 8;

        initDefaultPosition();
        initMoves(true);

    }

    /**
     * Creates a new {@link Position} object from the previous position with the new
     * {@link Move}.
     * 
     * @param prev         The previous position to use as a baseline for this
     *                     position.
     * @param move         The move to be made.
     * @param promoteType  The type of piece to promote to.
     * @param checkForMate Whether or not checkmate should be checked for. Should
     *                     only be {@code true} when the position being created is
     *                     the result of a move that is made.
     * @throws Exception If the {@code promoteType} is incorrect or the opening
     *                   cannot be found.
     */
    public Position(Position prev, Move move, char promoteType, boolean checkForMate) throws Exception {

        this.pieces = new Piece[8][8];
        this.timerEnd = -1;
        this.white = !move.isWhite();
        this.moveNumber = prev.getMoveNumber() + 1;
        this.move = move;

        this.whiteASide = prev.isWhiteASide();
        this.whiteHSide = prev.isWhiteHSide();
        this.blackASide = prev.isBlackASide();
        this.blackHSide = prev.isBlackHSide();

        this.aSideRookFile = prev.getaSideRookFile();
        this.hSideRookFile = prev.gethSideRookFile();

        Piece[][] prevPieces = prev.getPieces();

        for (int r = 0; r < prevPieces.length; r++) {

            for (int f = 0; f < prevPieces[r].length; f++) {

                final Piece old = prevPieces[r][f];

                if (old == null)
                    continue;

                Piece piece = null;
                switch (old.getCode()) {
                    case 'P':
                        piece = new Pawn(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());
                        break;
                    case 'K':
                        piece = new King(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());

                        if (piece.isWhite())
                            whiteKing = piece.getSquare();
                        else
                            blackKing = piece.getSquare();

                        break;
                    case 'N':
                        piece = new Knight(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());
                        break;
                    case 'Q':
                        piece = new Queen(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());
                        break;
                    case 'B':
                        piece = new Bishop(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());
                        break;
                    case 'R':
                        piece = new Rook(old.getSquare().getFile(), old.getSquare().getRank(), old.isWhite());
                        break;
                }

                if (piece != null)
                    pieces[old.getSquare().getRank() - 1][old.getSquare().getFile() - 1] = piece;

            }
        }

        if (move.isCapture()) {

            final Square capSquare = move.getCaptureSquare();

            pieces[capSquare.getRank() - 1][capSquare.getFile() - 1] = null;

        }

        final Piece movePiece = pieces[move.getOrigin().getRank() - 1][move.getOrigin().getFile() - 1];

        if (movePiece.getCode() == 'K') {

            if (movePiece.isWhite()) {

                whiteKing = move.getDestination();
                whiteASide = false;
                whiteHSide = false;

            } else {

                blackKing = move.getDestination();
                blackASide = false;
                blackHSide = false;

            }

        } else if (movePiece.getCode() == 'R') {

            if (movePiece.isWhite()) {

                if (move.getOrigin().getFile() == aSideRookFile)
                    whiteASide = false;
                else if (move.getOrigin().getFile() == hSideRookFile)
                    whiteHSide = false;

            } else {

                if (move.getOrigin().getFile() == aSideRookFile)
                    blackASide = false;
                else if (move.getOrigin().getFile() == hSideRookFile)
                    blackHSide = false;

            }

        }

        if (movePiece.getCode() == 'P' && move.getMoveDistance() == 2)
            enPassantTarget = new Square(move.getDestination().getFile(),
                    move.getDestination().getRank() + (move.isWhite() ? -1 : 1));

        movePiece.setSquare(move.getDestination());

        pieces[move.getOrigin().getRank() - 1][move.getOrigin().getFile() - 1] = null;

        if (move.isCastle()) {

            final Piece rook = getPieceAtSquare(move.getRookOrigin());
            rook.setSquare(move.getRookDestination());

            if (move.isWhite()) {

                whiteASide = false;
                whiteHSide = false;

            } else {

                blackASide = false;
                blackHSide = false;

            }

            pieces[move.getRookOrigin().getRank() - 1][move.getRookOrigin().getFile() - 1] = null;
            pieces[move.getRookDestination().getRank() - 1][move.getRookDestination().getFile() - 1] = rook;

        }

        pieces[move.getDestination().getRank() - 1][move.getDestination().getFile() - 1] = movePiece;

        if (move.getPromoteType() == '?' && checkForMate) {

            if (promoteType != 'Q' && promoteType != 'R' && promoteType != 'B' && promoteType != 'N')
                throw new Exception("Invalid promote type.");

            move.setPromoteType(promoteType);

            final Square moveDest = move.getDestination();

            switch (promoteType) {
                case 'Q':
                    setSquare(moveDest, new Queen(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'R':
                    setSquare(moveDest, new Rook(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'B':
                    setSquare(moveDest, new Bishop(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'N':
                    setSquare(moveDest, new Knight(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
            }

        } else if (move.getPromoteType() != '0' && checkForMate) {

            if (move.getPromoteType() != 'Q'
                    && move.getPromoteType() != 'R'
                    && move.getPromoteType() != 'B'
                    && move.getPromoteType() != 'N')
                throw new Exception("Invalid promote type.");

            final Square moveDest = move.getDestination();

            switch (move.getPromoteType()) {
                case 'Q':
                    setSquare(moveDest, new Queen(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'R':
                    setSquare(moveDest, new Rook(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'B':
                    setSquare(moveDest, new Bishop(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
                case 'N':
                    setSquare(moveDest, new Knight(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                    break;
            }

        }

        move.updateMoveNotation();
        initMoves(checkForMate);

        if (checkForMate) {

            if (move.isCapture() || move.getPiece().getCode() == 'P')
                this.fiftyMoveCounter = 0;
            else
                this.fiftyMoveCounter = prev.getFiftyMoveCounter() + 1;

            try {

                opening = Opening.getOpening(this.toString(), getClass().getResourceAsStream("/tsv/openings.tsv"));

                if (opening == null)
                    opening = prev.getOpening();

            } catch (RuntimeException e) {
                throw new RuntimeException(
                        "Error finding the opening associated with this position: " + e.getMessage());
            }

        }

    }

    /**
     * Creates a new {@link Position} object from the given starting position.
     * 
     * @param fen The Forsyth-Edwards Notation (FEN) of the position to start from.
     */
    public Position(String fen) throws RuntimeException {

        String[] a = fen.split(" ");

        if (a.length != 6)
            throw new RuntimeException("Invalid FEN.");

        this.pieces = new Piece[8][8];
        this.mateChecked = false;

        this.timerEnd = -1;

        String[] ranks = a[0].split("/");

        if (ranks.length != 8)
            throw new RuntimeException("Invalid number of ranks.");

        if (a[1].equals("w"))
            white = true;
        else if (a[1].equals("b"))
            white = false;
        else
            throw new RuntimeException("Invalid color to move.");

        try {
            fiftyMoveCounter = Integer.parseInt(a[4]);
        } catch (Exception e) {
            throw new RuntimeException("Invalid 50 move counter.");
        }

        try {
            moveNumber = ((Integer.parseInt(a[5]) - 1) * 2) + (white ? 0 : 1);
        } catch (Exception e) {
            throw new RuntimeException("Invalid move number.");
        }

        if (fiftyMoveCounter < 0)
            throw new RuntimeException("Fifty move counter cannot be less than 0.");

        if (moveNumber + 1 < 1)
            throw new RuntimeException("Move number cannot be less than 1.");

        for (int r = 0; r < 8; r++) {

            for (int i = 0, f = 0; i < ranks[r].length(); i++) {

                char c = ranks[r].charAt(i);

                // Blank spaces
                if (Character.isDigit(c)) {

                    int blankCount = Integer.parseInt(c + "");
                    f += blankCount;
                    continue;

                } else {
                    // Pieces

                    if (!(c + "").matches("[KQBRNPkqbrnp]"))
                        throw new RuntimeException("Unexpected piece type.");
                    boolean white = Character.isUpperCase(c);
                    switch (Character.toUpperCase(c)) {
                        case 'K':
                            pieces[7 - r][f] = new King(f + 1, 8 - r, white);

                            if (white)
                                whiteKing = new Square(f + 1, 8 - r);
                            else
                                blackKing = new Square(f + 1, 8 - r);

                            break;
                        case 'Q':
                            pieces[7 - r][f] = new Queen(f + 1, 8 - r, white);
                            break;
                        case 'R':
                            pieces[7 - r][f] = new Rook(f + 1, 8 - r, white);
                            break;
                        case 'B':
                            pieces[7 - r][f] = new Bishop(f + 1, 8 - r, white);
                            break;
                        case 'N':
                            pieces[7 - r][f] = new Knight(f + 1, 8 - r, white);
                            break;
                        case 'P':
                            pieces[7 - r][f] = new Pawn(f + 1, 8 - r, white);

                            break;
                        default:
                            throw new RuntimeException("Unexpected piece.");
                    }

                    ++f;

                }

            }

        }

        if (!a[3].equals("-")) {

            try {
                enPassantTarget = new Square(a[3]);
            } catch (Exception e) {
                throw new RuntimeException("Invalid en passant target square.");
            }

        }

        if (!a[2].equals("-")) {
            for (int i = 0; i < a[2].length(); i++) {

                char c = a[2].charAt(i);
                switch (c) {

                    case 'K':
                        whiteASide = true;
                        aSideRookFile = 1;
                        break;
                    case 'Q':
                        whiteHSide = true;
                        hSideRookFile = 8;
                        break;
                    case 'k':
                        blackASide = true;
                        aSideRookFile = 1;
                        break;
                    case 'q':
                        blackHSide = true;
                        hSideRookFile = 8;
                        break;
                    default:

                        boolean w = Character.isUpperCase(c);
                        char lc = Character.toLowerCase(c);
                        int file = lc - 96;
                        if (file < (w ? whiteKing : blackKing).getFile()) {

                            aSideRookFile = file;

                            if (w)
                                whiteASide = true;
                            else
                                blackASide = true;

                        } else if (file > (w ? whiteKing : blackKing).getFile()) {

                            hSideRookFile = file;

                            if (w)
                                whiteHSide = true;
                            else
                                blackHSide = true;

                        }

                }

            }
        }

        initMoves(true);

        try {

            opening = Opening.getOpening(this.toString(), getClass().getResourceAsStream("/tsv/openings.tsv"));

        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Error finding the opening associated with this position: " + e.getMessage());
        }

    }

    public boolean isWhiteASide() {
        return whiteASide;
    }

    public boolean isWhiteHSide() {
        return whiteHSide;
    }

    public boolean isBlackASide() {
        return blackASide;
    }

    public boolean isBlackHSide() {
        return blackHSide;
    }

    public int getaSideRookFile() {
        return aSideRookFile;
    }

    public int gethSideRookFile() {
        return hSideRookFile;
    }

    /**
     * Gets the en passant target square for this position.
     * 
     * @return {@link #enPassantTarget}
     */
    public Square getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * Gets the move number.
     * 
     * @return {@link #moveNumber}
     */
    public int getMoveNumber() {
        return moveNumber;
    }

    /**
     * Gets the two-dimensional array of the pieces.
     * 
     * @return {@link #pieces}
     */
    public Piece[][] getPieces() {
        return pieces;
    }

    /**
     * Gets the amount of time on the timer at the end of this position.
     * 
     * @return {@link #timerEnd}
     */
    public long getTimerEnd() {
        return timerEnd;
    }

    /**
     * Sets the amount of time on the timer at the end of this position.
     * 
     * @param timerEnd The time in milliseconds
     */
    public void setTimerEnd(long timerEnd) {
        this.timerEnd = timerEnd;
    }

    /**
     * Gets the position that will be redone if the player requests it.
     * 
     * @return The {@link Position} that was previously after this {@link Position}.
     */
    public Position getRedo() {
        return redo;
    }

    /**
     * Sets the redo {@link Position}.
     * 
     * @param redo The redo {@link Position}
     */
    public void setRedo(Position redo) {
        this.redo = redo;
    }

    /**
     * Gets the promotion type of the redo move.
     * 
     * @return {@link #redoPromote}
     */
    public char getRedoPromote() {
        return redoPromote;
    }

    /**
     * Sets the promotion type of the redo move.
     * 
     * @param redoPromote {@link #redoPromote}
     */
    public void setRedoPromote(char redoPromote) {
        this.redoPromote = redoPromote;
    }

    /**
     * Gets the timerEnd of the redo move.
     * 
     * @return The time on the timer at the end of the redo move.
     */
    public long getRedoTimerEnd() {
        return redoTimerEnd;
    }

    /**
     * Sets the timerEnd of the redo move.
     * 
     * @param redoTimerEnd The time in milliseconds
     */
    public void setRedoTimerEnd(long redoTimerEnd) {
        this.redoTimerEnd = redoTimerEnd;
    }

    /**
     * Gets the list of moves.
     * 
     * @return A list of the {@link Move} objects possible in this position. If
     *         {@code checkForMate} is {@code true} when constructor is called,
     *         moves that lead to check will not be included.
     */
    public ArrayList<Move> getMoves() {
        return moves;
    }

    /**
     * Gets the move that led to this position.
     * 
     * @return Gets the current {@link Move} that led to this position.
     */
    public Move getMove() {
        return move;
    }

    /**
     * Gets whether or not it is white's turn in this position.
     * 
     * @return Returns {@code true} if it is currently white's turn. Is the opposite
     *         color of the move that led to this position.
     */
    public boolean isWhite() {
        return white;
    }

    /**
     * Gets whether or not the current player is giving check.
     * 
     * @return Returns {@code true} if the value of {@link #isWhite()} is giving
     *         check to the other color.
     */
    public boolean isGivingCheck() {
        return givingCheck;
    }

    /**
     * Gets whether or not the curent player is in check.
     * 
     * @return Returns {@code true} if the value of {@link #isWhite()} is in check
     *         from the other color.
     */
    public boolean isInCheck() {
        return inCheck;
    }

    /**
     * Gets whether or not this position is checkmate.
     * 
     * @return Returns {@code true} if the current position is check mate. May be
     *         {@code null} if {@code checkForMate} was not true when constructor
     *         was called.
     */
    public boolean isCheckmate() {
        return checkMate;
    }

    /**
     * Gets the fifty-move counter as it is after the move that led to this position
     * is made.
     * 
     * @return {@link #fiftyMoveCounter}
     */
    public int getFiftyMoveCounter() {
        return fiftyMoveCounter;
    }

    /**
     * Gets the opening that led to this position.
     * 
     * @return {@link #opening}
     */
    public Opening getOpening() {
        return opening;
    }

    /**
     * Returns a string representation of the move that led to this position.
     * 
     * <p>
     * Should be used over {@link Move#getMoveNotation()} when possible, as it
     * includes whether or not this move was a check (+) or checkmate (#).
     * 
     * @return A {@link String} containing the move text.
     */
    public String getMoveString() {

        String str = move.getMoveNotation();

        if (isCheckmate())
            str += "#";
        else if (isInCheck())
            str += "+";

        return str;

    }

    /**
     * Checks if the board positions are exactly equal, including castling
     * privilege, en passant availability, turn, move number, and fifty move
     * counter.
     */
    @Override
    public boolean equals(Object compare) {

        if (!(compare instanceof Position))
            return false;

        Position casted = (Position) (compare);

        return toString().equals(casted.toString());

    }

    /**
     * Gets the rook on the side specified and of the given color. Will only return
     * the rook that is on the home rank of that color. Will not return a rook that
     * has already moved.
     * 
     * @param aRook Whether or not to search for the a-side rook or the h-side rook.
     * @param white Whether or not the rook is white.
     * @return The rook.
     */
    public Piece getRook(boolean aRook, boolean white) {

        if (aRook) {

            return getPieceAtSquare(new Square(aSideRookFile, white ? 1 : 8));

        } else {

            return getPieceAtSquare(new Square(hSideRookFile, white ? 1 : 8));

        }

    }

    /**
     * Constructs an array of all of the pieces that are not on the board in this
     * position of a given color.
     * 
     * @param white The color of the captured pieces.
     * @return The array of captured pieces.
     */
    public ArrayList<Piece> getCapturedPieces(boolean white) {

        ArrayList<Piece> cap = new ArrayList<>();

        int k = 1, q = 1, r = 2, b = 2, n = 2, p = 8;

        for (int rk = 0; rk < 8; rk++) {

            for (int f = 0; f < 8; f++) {

                Piece a = pieces[rk][f];

                if (a != null && a.isWhite() == white) {

                    switch (a.getCode()) {
                        case 'K':
                            --k;
                            break;
                        case 'Q':
                            --q;
                            break;
                        case 'R':
                            --r;
                            break;
                        case 'B':
                            --b;
                            break;
                        case 'N':
                            --n;
                            break;
                        case 'P':
                            --p;
                            break;
                    }
                }

            }

        }

        while (k > 0) {
            cap.add(new King(0, 0, white));
            --k;
        }

        while (q > 0) {
            cap.add(new Queen(0, 0, white));
            --q;
        }

        while (r > 0) {
            cap.add(new Rook(0, 0, white));
            --r;
        }

        while (b > 0) {
            cap.add(new Bishop(0, 0, white));
            --b;
        }

        while (n > 0) {
            cap.add(new Knight(0, 0, white));
            --n;
        }

        while (p > 0) {
            cap.add(new Pawn(0, 0, white));
            --p;
        }

        return cap;

    }

    /**
     * Calculates the difference in material for each side by taking the point total
     * of white pieces minus the point total of black pieces.
     * 
     * <p>
     * If the delta is negative, black has that amount more material. If it is
     * positive, white has that amount more material.
     * 
     * @return The point delta (white - black.)
     */
    public int calculatePieceDelta() {

        int delta = 0;

        for (int r = 0; r < 8; r++) {

            for (int f = 0; f < 8; f++) {

                Piece p = pieces[r][f];

                if (p != null)
                    delta += (p.isWhite() ? 1 : -1) * p.getPoints();

            }

        }

        return delta;

    }

    /**
     * Finds a move based on the given origin and destination.
     * 
     * <p>
     * Castle moves can be represented as either the king capturing the respective
     * rook, or as the king moving to the castle destination square. However, in
     * variants like Chess960, the king may be able to move to the same square it
     * can castle to. In that case, the normal move always overrides the castle
     * move.
     * 
     * <p>
     * Castle moves can always be represented unambiguously as the king moving to
     * the rook's square.
     * 
     * @param origin      The origin square of the move to search for
     * @param destination The destination square of the move to search for
     * @return The move found, or {@code null} if none found.
     */
    public Move findMove(Square origin, Square destination) {

        if (origin == null || destination == null || !origin.isValid() || !destination.isValid())
            return null;

        // Finding the move based on the origin and destination.
        // Castle moves should be king moving to rook's square.
        Move move = null, maybe = null;

        for (int i = 0; move == null && i < moves.size(); i++) {

            Move a = moves.get(i);

            if (!a.isCastle() && a.getOrigin().equals(origin) && a.getDestination().equals(destination))
                move = a;
            else if (a.isCastle() && getPieceAtSquare(destination) != null
                    && getPieceAtSquare(destination).equals(a.getRook())) {
                move = a;
            } else if (a.isCastle() && a.getOrigin().equals(origin) && a.getDestination().equals(destination)) {
                // will mark a castle move that matches the origin and destination, but doesn't
                // set it as the move in case there is a non castle move with the same origin
                // and destination
                maybe = a;
            }

        }

        if (move == null && maybe != null)
            move = maybe;

        return move;

    }

    /**
     * Sets the promote type and updates the moves accordingly.
     * 
     * @param promo The type of piece to promote to.
     * @throws Exception If the promotion is invalid.
     */
    public void setPromote(char promo) throws Exception {

        if (promo != '?' && promo != 'Q' && promo != 'R' && promo != 'B' && promo != 'N')
            throw new Exception("Invalid promote type.");

        move.setPromoteType(promo);

        final Piece movePiece = move.getPiece();
        final Square moveDest = move.getDestination();

        switch (promo) {
            case 'Q':
                setSquare(moveDest, new Queen(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                break;
            case 'R':
                setSquare(moveDest, new Rook(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                break;
            case 'B':
                setSquare(moveDest, new Bishop(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                break;
            case 'N':
                setSquare(moveDest, new Knight(moveDest.getFile(), moveDest.getRank(), movePiece.isWhite()));
                break;
            case '?':
                setSquare(moveDest, movePiece);
                break;
        }

        if (!move.getPiece().equals(getPieceAtSquare(moveDest))) {

            initMoves(true);
            move.updateMoveNotation();

        }

    }

    /**
     * Gets the point total of the pieces that {@code white} has.
     * 
     * @param white The color
     * @return A point total of the pieces.
     * @see #calculatePieceDelta()
     */
    public int getPoints(boolean white) {

        int points = 0;

        for (int r = 0; r < 8; r++) {

            for (int f = 0; f < 8; f++) {

                Piece a = pieces[r][f];

                if (a.isWhite() == white)
                    points += a.getPoints();

            }

        }

        return points;

    }

    /**
     * Sets the square of a piece.
     * 
     * @param square The square to set the piece to.
     * @param piece  The piece to set.
     */
    public void setSquare(Square square, Piece piece) {

        pieces[square.getRank() - 1][square.getFile() - 1] = piece;

    }

    /**
     * Gets the piece at the given square.
     * 
     * @param square The square to search for a piece at
     * @return The {@link Piece} object. Will be {@code null} if no piece is at the
     *         square.
     */
    public Piece getPieceAtSquare(Square square) {
        return pieces[square.getRank() - 1][square.getFile() - 1];
    }

    /**
     * Gets a list of the pieces that are attacking the given square (able to
     * capture the piece occupying it.)
     * 
     * @param square The square to search for attacking pieces at.
     * @return An {@link ArrayList} of {@link Piece} objects
     * @see #getPiecesByCanMoveTo(Square)
     */
    public ArrayList<Piece> getPiecesByAttacking(Square square) {

        ArrayList<Piece> pieces = new ArrayList<Piece>();

        for (int i = 0; i < moves.size(); i++) {

            Move m = moves.get(i);
            if (m.isCapture() && m.getCaptureSquare().equals(square))
                pieces.add(m.getPiece());

        }

        return pieces;

    }

    /**
     * Gets a list of the pieces that are able to move to a given square.
     * 
     * @param square The square to search for moves to.
     * @return An {@link ArrayList} of {@link Piece} objects
     * @see #getPiecesByAttacking(Square)
     */
    public ArrayList<Piece> getPiecesByCanMoveTo(Square square) {

        ArrayList<Piece> pieces = new ArrayList<Piece>();

        for (int i = 0; i < moves.size(); i++) {

            Move m = moves.get(i);
            if (m.getDestination().equals(square))
                pieces.add(m.getPiece());

        }

        return pieces;

    }

    /**
     * Gets a list of the pieces that are able to move to a given square.
     * 
     * @param square The square to search for moves to.
     * @param white  Whether or not to check if white pieces can move to the square.
     * @return An {@link ArrayList} of {@link Piece} objects
     * @see #getPiecesByAttacking(Square)
     */
    public ArrayList<Piece> getPiecesByCanMoveToColor(Square square, boolean white) {

        ArrayList<Piece> pieces = new ArrayList<Piece>();

        for (int i = 0; i < moves.size(); i++) {

            Move m = moves.get(i);
            if (m.isWhite() == white && m.getDestination().equals(square))
                pieces.add(m.getPiece());

        }

        return pieces;

    }

    /**
     * Checks if a piece can move to the given square.
     * 
     * @param piece  The piece to check
     * @param square The square to check for
     * @return Whether or not the location can be moved to
     */
    public boolean canPieceMoveToSquare(Piece piece, Square square) {

        ArrayList<Piece> attackers = getPiecesByCanMoveTo(square);

        for (int i = 0; i < attackers.size(); i++) {

            if (attackers.get(i).equals(piece))
                return true;

        }

        return false;

    }

    /**
     * Gets the moves the given piece can make.
     * 
     * @param piece The piece to get the moves of.
     * @return A list of the moves the piece can make.
     */
    public ArrayList<Move> getPieceMoves(Piece piece) {

        ArrayList<Move> pieceMoves = new ArrayList<Move>();

        for (int i = 0; i < moves.size(); i++) {

            if (moves.get(i).getPiece().equals(piece))
                pieceMoves.add(moves.get(i));

        }

        return pieceMoves;

    }

    /**
     * Gets the square that the king resides on.
     * 
     * @param white The color of the piece to search for. True if white.
     * @return A {@link Square} object
     */
    public Square getKingSquare(boolean white) {

        return white ? whiteKing : blackKing;

    }

    /**
     * Checks if this position has insufficient material to reach a checkmate.
     * 
     * <p>
     * See:
     * <a
     * href=
     * "https://en.wikipedia.org/wiki/Glossary_of_chess#insufficient_material">https://en.wikipedia.org/wiki/Glossary_of_chess#insufficient_material</a>
     * 
     * @return If the position has insufficient pieces on both sides to reach
     *         checkmate.
     */
    public boolean isInsufficientMaterial() {

        ArrayList<Piece> list = getPiecesAsArrayList();

        if (list.size() > 4)
            return false;

        // King and king
        if (list.size() == 2)
            return true;

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getCode() == 'K') {
                list.remove(i);
                --i;
            }

        }

        // King against king and bishop / king against king and knight
        if (list.size() == 1 && (list.get(0).getCode() == 'B' || list.get(0).getCode() == 'N'))
            return true;
        else if (list.size() == 2) {

            Piece one = list.get(0);
            Piece two = list.get(1);

            // King and bishop against king and bishop, with both being on squares of same
            // color
            if (one.isWhite() != two.isWhite()
                    && one.getSquare().isLightSquare() == two.getSquare().isLightSquare())
                return true;

        }

        return false;

    }

    /**
     * Checks if this position is a stalemate.
     * 
     * <p>
     * See:
     * <a
     * href=
     * "https://en.wikipedia.org/wiki/Glossary_of_chess#stalemate">https://en.wikipedia.org/wiki/Glossary_of_chess#stalemate</a>
     * 
     * @return If the position is stalemate.
     */
    public boolean isStalemate() {

        return moves.size() == 0;

    }

    /**
     * Gets the pieces as an {@link ArrayList}.
     * 
     * @return The pieces as an {@link ArrayList}.
     */
    public ArrayList<Piece> getPiecesAsArrayList() {

        ArrayList<Piece> list = new ArrayList<Piece>();

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {

                Piece p = pieces[r][f];
                if (p != null) {
                    list.add(p);
                }

            }

        }

        return list;
    }

    /**
     * Outputs the Forsyth–Edwards Notation (FEN) for this position.
     */
    @Override
    public String toString() {

        String fen = "";

        // Pieces
        for (int r = 7; r >= 0; r--) {

            int noPieceCount = 0;

            for (int f = 0; f < 8; f++) {

                Piece p = pieces[r][f];

                if (p == null)
                    ++noPieceCount;
                else {

                    if (noPieceCount > 0) {

                        fen += noPieceCount;
                        noPieceCount = 0;

                    }

                    fen += p.isWhite() ? (p.getCode() + "") : (p.getCode() + "").toLowerCase();

                }

            }

            if (noPieceCount > 0) {

                fen += noPieceCount;
                noPieceCount = 0;

            }

            if (r > 0)
                fen += "/";

        }

        // Active Color
        fen += " " + (isWhite() ? "w" : "b");

        // Castle availability

        boolean normalCastleIndicators = aSideRookFile == 1 && hSideRookFile == 8;

        fen += " "
                + (whiteASide ? (normalCastleIndicators ? "K" : Character.toUpperCase((char) (aSideRookFile + 96)))
                        : "")
                + (whiteHSide ? (normalCastleIndicators ? "Q" : Character.toUpperCase((char) (hSideRookFile + 96)))
                        : "")
                + (blackASide ? (normalCastleIndicators ? "k" : ((char) (aSideRookFile + 96)))
                        : "")
                + (blackHSide ? (normalCastleIndicators ? "q" : ((char) (hSideRookFile + 96)))
                        : "");

        if (fen.charAt(fen.length() - 1) == ' ')
            fen += '-';

        fen += " " + (enPassantTarget == null ? "-" : enPassantTarget.toString());

        // Fifty-move rule clock
        fen += " " + fiftyMoveCounter;

        // Fullmove number
        fen += " " + ((int) Math.ceil(moveNumber / 2.0) + 1);

        return fen;

    }

    /**
     * Checks the castling availability in the current position. Based on whether or
     * not the king and rook can move, and whether or not there is a king or rook on
     * the given side. Does not mean that castling can occur during the current
     * turn, as temporary blocks like check may still be present.
     * 
     * @param white The color to check if can castle.
     * @param aSide Whether to check castling a side or h side.
     * @return Whether or not the given castle is possible.
     */
    public boolean canCastle(boolean white, boolean aSide) {

        if (white)
            return aSide ? whiteASide : whiteHSide;
        else
            return aSide ? blackASide : blackHSide;

    }

    /**
     * Finds an available move by its short algebraic notation (SAN).
     * 
     * @param move The SAN of the move to find.
     * @return The move found from the SAN.
     * @throws Exception        If the move notation is invalid.
     * @throws RuntimeException If {@link #mateChecked} is not {@code true}.
     */
    public Move getMoveBySAN(String move) throws Exception {

        if (!mateChecked)
            throw new RuntimeException(
                    "Cannot find move by SAN if position has not been initialized with checkForMate as true.");

        move = move.trim();

        Square o = null;
        Square d = null;
        char piece = '0';
        int oFile = -1;
        int oRank = -1;

        if (move.startsWith("0-0-0") || move.startsWith("O-O-O")) {

            o = new Square((white ? whiteKing : blackKing).getFile(), (white ? whiteKing : blackKing).getRank());
            d = new Square(3, white ? 1 : 8);
            piece = 'K';

        } else if (move.startsWith("0-0") || move.startsWith("O-O")) {

            o = new Square((white ? whiteKing : blackKing).getFile(), (white ? whiteKing : blackKing).getRank());
            d = new Square(7, white ? 1 : 8);
            piece = 'K';

        } else {

            Pattern pat = Pattern.compile("[a-h][1-8]");
            Matcher m = pat.matcher(move);

            int lastSquare = -1;
            while (m.find()) {
                lastSquare = m.start();
            }

            if (lastSquare <= -1)
                throw new Exception("No destination square.");

            try {
                d = new Square(move.substring(lastSquare, lastSquare + 2));
            } catch (Exception e) {
                throw new Exception("Invalid destination square.");
            }

            String first = move.substring(0, 1);
            int start = 0;

            if (first.matches("[KQRBNP]")) {
                piece = first.charAt(0);
                start = 1;
            } else if (first.matches("[a-h]"))
                piece = 'P';
            else
                throw new Exception("Invalid piece type.");

            if (move.length() > 3) {
                String modifier = move.substring(start, lastSquare);

                if (modifier.matches("[a-h][1-8]x?")) {
                    o = new Square(modifier);
                } else if (modifier.matches("[a-h]x?")) {
                    oFile = (int) (modifier.charAt(0)) - 96;
                } else if (modifier.matches("[1-8]x?")) {
                    oRank = (int) (modifier.charAt(0)) - 48;
                }
            }

        }

        ArrayList<Move> possibleMoves = new ArrayList<Move>();

        for (Move mo : moves) {

            if (mo.getPiece().getCode() != piece || mo.isWhite() != isWhite())
                continue;

            if (mo.getDestination().equals(d)) {

                if (o != null && mo.getOrigin().equals(o))
                    possibleMoves.add(mo);
                else if (oFile > -1 && oRank > -1) {
                    if (mo.getOrigin().getFile() == oFile && mo.getOrigin().getRank() == oRank)
                        possibleMoves.add(mo);
                } else if (oFile > -1) {
                    if (mo.getOrigin().getFile() == oFile)
                        possibleMoves.add(mo);
                } else if (oRank > -1) {
                    if (mo.getOrigin().getRank() == oRank)
                        possibleMoves.add(mo);
                } else {
                    possibleMoves.add(mo);
                }

            }

        }

        if (possibleMoves.size() == 0)
            throw new Exception("Move not found.");

        if (possibleMoves.size() > 1)
            throw new Exception("Multiple possible moves.");

        Move found = possibleMoves.get(0);

        Matcher findPromo = Pattern.compile("=(?<promo>[QRBN])").matcher(move);
        if (findPromo.find()) {

            char promo = findPromo.group("promo").charAt(0);

            if (promo != 'Q' && promo != 'R' && promo != 'B' && promo != 'N')
                throw new Exception("Invalid promote type supplied.");

            found.setPromoteType(promo);

        }

        return found;

    }

    /**
     * Initializes the list of moves.
     * 
     * @param checkForMate If checkmate should be checked for.
     */
    private void initMoves(boolean checkForMate) {

        this.moves = new ArrayList<Move>();

        for (int r = 0; r < pieces.length; r++) {

            for (int c = 0; c < pieces[r].length; c++) {

                final Piece p = pieces[r][c];

                if (p == null)
                    continue;

                moves.addAll(p.getMoves(this));

            }

        }

        ArrayList<Piece> ownPieces = getPiecesByAttacking(white ? whiteKing : blackKing);
        if (ownPieces.size() >= 1)
            inCheck = true;

        ArrayList<Piece> oppPieces = getPiecesByAttacking(!white ? whiteKing : blackKing);
        if (oppPieces.size() >= 1)
            givingCheck = true;

        ArrayList<Move> castleMoves = new ArrayList<>(2);

        // Castling
        Square kingSquare = white ? whiteKing : blackKing;

        Piece hRook = getRook(false, white), aRook = getRook(true, white);

        if (hRook != null && canCastle(white, false)) {

            boolean canReach = true;

            // If king can reach
            boolean left = kingSquare.getFile() > 7;

            for (int inc = (left ? -1 : 1), i = kingSquare.getFile() + inc; canReach
                    && ((left && i >= 7) || (!left && i <= 7)); i += inc) {

                Square lookingSquare = new Square(i, kingSquare.getRank());

                ArrayList<Piece> attackers = getPiecesByCanMoveToColor(lookingSquare, !white);

                if (attackers.size() > 0)
                    canReach = false;

                if (canReach == false || i == gethSideRookFile())
                    continue;

                Piece find = getPieceAtSquare(lookingSquare);
                if (find != null)
                    canReach = false;

            }

            // If rook can reach
            left = hRook.getSquare().getFile() > 6;

            for (int inc = (left ? -1 : 1), i = hRook.getSquare().getFile() + inc; canReach
                    && ((left && i >= 6) || (!left && i <= 6)); i += inc) {

                if (i == kingSquare.getFile())
                    continue;

                Piece find = getPieceAtSquare(new Square(i, kingSquare.getRank()));
                if (find != null)
                    canReach = false;

            }

            if (canReach) {

                try {
                    castleMoves.add(new Move(kingSquare, new Square(7, white ? 1 : 8), this,
                            true));
                } catch (Exception e) {
                }

            }

        }

        if (aRook != null && canCastle(white, true)) {

            boolean canReach = true;

            // If king can reach
            boolean left = kingSquare.getFile() > 3;

            for (int inc = (left ? -1 : 1), i = kingSquare.getFile() + inc; canReach
                    && ((left && i >= 3) || (!left && i <= 3)); i += inc) {

                Square lookingSquare = new Square(i, kingSquare.getRank());

                ArrayList<Piece> attackers = getPiecesByCanMoveToColor(lookingSquare, !white);

                if (attackers.size() > 0)
                    canReach = false;

                if (canReach == false || i == gethSideRookFile())
                    continue;

                Piece find = getPieceAtSquare(lookingSquare);
                if (find != null)
                    canReach = false;

            }

            // If rook can reach
            left = aRook.getSquare().getFile() > 4;

            for (int inc = (left ? -1 : 1), i = aRook.getSquare().getFile() + inc; canReach
                    && ((left && i >= 4) || (!left && i <= 4)); i += inc) {

                if (i == kingSquare.getFile())
                    continue;

                Piece find = getPieceAtSquare(new Square(i, kingSquare.getRank()));
                if (find != null)
                    canReach = false;

            }

            if (canReach) {

                try {
                    castleMoves.add(new Move(kingSquare, new Square(3, white ? 1 : 8), this,
                            true));
                } catch (Exception e) {
                }

            }

        }

        if (checkForMate) {

            // longest delay
            setCheckmate();

            if (this.inCheck == false) {

                moves.addAll(castleMoves);

            }
        }

    }

    /**
     * Sets whether or not the position is check mate. (The color whose turn it is
     * isn't able to make any moves and is in check.) Also will filter out any moves
     * that are not legal.
     */
    private void setCheckmate() {

        this.mateChecked = true;
        this.checkMate = true;

        Collection<Move> temp = Collections.synchronizedList(new ArrayList<>(moves.size()));
        
        ExecutorService pool = Executors.newCachedThreadPool();
        try {

            for (int i = 0; i < moves.size(); i++) {

                Move m = moves.get(i);

                if (m.isWhite() != isWhite())
                    continue;

                try {
                    pool.submit(() -> {

                        try {

                            Position test = new Position(this, m, '0', false);

                            if (!test.isGivingCheck()) {
                                checkMate = false;
                                temp.add(m);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        moves.clear();
        moves.addAll(temp);

    }

    /**
     * Initializes the default board pieces and their positions.
     */
    private void initDefaultPosition() {

        pieces = new Piece[8][8];

        boolean color = true;
        for (int i = 0; i < 2; i++) {

            for (int j = 0; j < 8; j++) {

                setSquare(new Square(j + 1, color ? 2 : 7), new Pawn(j + 1, color ? 2 : 7, color));

            }

            // Kings
            Piece king = new King(5, color ? 1 : 8, color);
            if (color)
                whiteKing = king.getSquare();
            else
                blackKing = king.getSquare();

            setSquare(king.getSquare(), king);

            // Rooks
            setSquare(new Square(1, color ? 1 : 8), new Rook(1, color ? 1 : 8, color));
            setSquare(new Square(8, color ? 1 : 8), new Rook(8, color ? 1 : 8, color));

            // Queen
            setSquare(new Square(4, color ? 1 : 8), new Queen(4, color ? 1 : 8, color));

            // Bishops
            setSquare(new Square(3, color ? 1 : 8), new Bishop(3, color ? 1 : 8, color));
            setSquare(new Square(6, color ? 1 : 8), new Bishop(6, color ? 1 : 8, color));

            // Knight
            setSquare(new Square(2, color ? 1 : 8), new Knight(2, color ? 1 : 8, color));
            setSquare(new Square(7, color ? 1 : 8), new Knight(7, color ? 1 : 8, color));

            color = false;
        }

    }

}