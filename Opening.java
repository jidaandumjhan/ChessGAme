package game;

import java.io.InputStream;
import java.util.*;

/**
 * Openings database from <a href=
 * "https://github.com/lichess-org/chess-openings">https://github.com/lichess-org/chess-openings</a>.
 */
public class Opening {

    /**
     * Gets the opening of the position with the given FEN from the openings in the
     * given file.
     * 
     * @param fen      The FEN of the position to check.
     * @param openings A tab-separated values file, formatted
     *                 {@code [ECO code]\t[opening name]\t[opening sequence of
     *                 moves]\t[opening FEN]}
     * @return The opening that matches the position.
     */
    public static Opening getOpening(String fen, InputStream openings) throws RuntimeException {

        Opening found = null;
        try (Scanner s = new Scanner(openings)) {

            while (s.hasNextLine() && found == null) {

                String line = s.nextLine().trim();
                String[] a = line.split("\t");

                if (fen.startsWith(a[3])) {

                    found = new Opening(a[0], a[1], a[2], a[3]);

                }

            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting opening: " + e.getMessage());
        }

        return found;

    }

    /**
     * The Encyclopedia of Chess Openings (ECO) code associated with this opening.
     */
    private final String code;

    /** The name of this opening. */
    private final String name;

    /**
     * The sequence of chess moves that leads to this opening. Note that there are
     * often multiple sequences to reach a certain opening position.
     */
    private final String sequence;

    /**
     * The FEN position that is led to by the sequence.
     * 
     * <b>Note:</b> This only includes the first component of FEN, the board
     * position (up to the first space.) Should not include turn, move numbers,
     * castling rights, etc.
     */
    private final String fen;

    /**
     * Creates a new Opening object with the given ECO code, name, and sequence.
     * 
     * @param code     The ECO code
     * @param name     The name of the opening
     * @param sequence A sequence of moves that leads to it
     * @param fen      The FEN position that this opening leads to.
     */
    public Opening(String code, String name, String sequence, String fen) {
        this.code = code;
        this.name = name;
        this.sequence = sequence;
        this.fen = fen;
    }

    /**
     * Gets the ECO code associated with this opening.
     * 
     * @return {@link #code}
     * @see #code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the name associated with this opening.
     * 
     * @return {@link #name}
     * @see #name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the sequence of moves associated with this opening.
     * 
     * @return {@link #sequence}
     * @see #sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Gets the FEN position associated with this opening.
     * 
     * <b>Note:</b> This only includes the first component of FEN, the board
     * position (up to the first space.) Should not include turn, move numbers,
     * castling rights, etc.
     * 
     * @return {@link #fen}
     * @see #fen
     */
    public String getFen() {
        return fen;
    }

}