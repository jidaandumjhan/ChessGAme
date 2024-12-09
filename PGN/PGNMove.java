package game.PGN;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.Game.Result;

/**
 * A move that was parsed from PGN.
 */
public class PGNMove {

    /**
     * A collection, in order, of the predetermined meanings of the numeric
     * annotation glyphs (NAGs) from 0 to 255.
     * 
     * <p>
     * From the PGN specification:
     * 
     * {@snippet :
     * An NAG (Numeric Annotation Glyph) is a movetext element that is used to
     * indicate a simple annotation in a language independent manner. An NAG is
     * formed from a dollar sign ("$") with a non-negative decimal integer suffix.
     * The non-negative integer must be from zero to 255 in value.
     * }
     */
    public static final String[] NAGs = {
            "null annotation", "good move (traditional !)", "poor move (traditional ?)",
            "very good move (traditional !!)", "very poor move (traditional ??)", "speculative move (traditional !?)",
            "questionable move (traditional ?!)", "forced move (all others lose quickly)",
            "singular move (no reasonable alternatives)", "worst move", "drawish position",
            "equal chances, quiet position", "equal chances, active position", "unclear position",
            "White has a slight advantage", "Black has a slight advantage", "White has a moderate advantage",
            "Black has a moderate advantage", "White has a decisive advantage", "Black has a decisive advantage",
            "White has a crushing advantage (Black should resign)",
            "Black has a crushing advantage (White should resign)", "White is in zugzwang", "Black is in zugzwang",
            "White has a slight space advantage", "Black has a slight space advantage",
            "White has a moderate space advantage", "Black has a moderate space advantage",
            "White has a decisive space advantage", "Black has a decisive space advantage",
            "White has a slight time (development) advantage", "Black has a slight time (development) advantage",
            "White has a moderate time (development) advantage", "Black has a moderate time (development) advantage",
            "White has a decisive time (development) advantage", "Black has a decisive time (development) advantage",
            "White has the initiative", "Black has the initiative", "White has a lasting initiative",
            "Black has a lasting initiative", "White has the attack", "Black has the attack",
            "White has insufficient compensation for material deficit",
            "Black has insufficient compensation for material deficit",
            "White has sufficient compensation for material deficit",
            "Black has sufficient compensation for material deficit",
            "White has more than adequate compensation for material deficit",
            "Black has more than adequate compensation for material deficit",
            "White has a slight center control advantage", "Black has a slight center control advantage",
            "White has a moderate center control advantage", "Black has a moderate center control advantage",
            "White has a decisive center control advantage", "Black has a decisive center control advantage",
            "White has a slight kingside control advantage", "Black has a slight kingside control advantage",
            "White has a moderate kingside control advantage", "Black has a moderate kingside control advantage",
            "White has a decisive kingside control advantage", "Black has a decisive kingside control advantage",
            "White has a slight queenside control advantage", "Black has a slight queenside control advantage",
            "White has a moderate queenside control advantage", "Black has a moderate queenside control advantage",
            "White has a decisive queenside control advantage", "Black has a decisive queenside control advantage",
            "White has a vulnerable first rank", "Black has a vulnerable first rank",
            "White has a well protected first rank", "Black has a well protected first rank",
            "White has a poorly protected king", "Black has a poorly protected king", "White has a well protected king",
            "Black has a well protected king", "White has a poorly placed king", "Black has a poorly placed king",
            "White has a well placed king", "Black has a well placed king", "White has a very weak pawn structure",
            "Black has a very weak pawn structure", "White has a moderately weak pawn structure",
            "Black has a moderately weak pawn structure", "White has a moderately strong pawn structure",
            "Black has a moderately strong pawn structure", "White has a very strong pawn structure",
            "Black has a very strong pawn structure", "White has poor knight placement",
            "Black has poor knight placement", "White has good knight placement", "Black has good knight placement",
            "White has poor bishop placement", "Black has poor bishop placement", "White has good bishop placement",
            "Black has good bishop placement", "White has poor rook placement", "Black has poor rook placement",
            "White has good rook placement", "Black has good rook placement", "White has poor queen placement",
            "Black has poor queen placement", "White has good queen placement", "Black has good queen placement",
            "White has poor piece coordination", "Black has poor piece coordination",
            "White has good piece coordination", "Black has good piece coordination",
            "White has played the opening very poorly", "Black has played the opening very poorly",
            "White has played the opening poorly", "Black has played the opening poorly",
            "White has played the opening well", "Black has played the opening well",
            "White has played the opening very well", "Black has played the opening very well",
            "White has played the middlegame very poorly", "Black has played the middlegame very poorly",
            "White has played the middlegame poorly", "Black has played the middlegame poorly",
            "White has played the middlegame well", "Black has played the middlegame well",
            "White has played the middlegame very well", "Black has played the middlegame very well",
            "White has played the ending very poorly", "Black has played the ending very poorly",
            "White has played the ending poorly", "Black has played the ending poorly",
            "White has played the ending well", "Black has played the ending well",
            "White has played the ending very well", "Black has played the ending very well",
            "White has slight counterplay", "Black has slight counterplay", "White has moderate counterplay",
            "Black has moderate counterplay", "White has decisive counterplay", "Black has decisive counterplay",
            "White has moderate time control pressure", "Black has moderate time control pressure",
            "White has severe time control pressure", "Black has severe time control pressure"
    };

    /**
     * The PGN notation text of the move, not including commentary, etc.
     * 
     * <p>
     * <b>Ex:</b> {@code Nf3}
     */
    private String moveText;

    /**
     * The half-move number this move is in the game.
     */
    private int moveNumber;

    /**
     * The {@code int} corresponding to the numeric annotation glyph. Will be the
     * same as the index in {@link #NAGs}.
     */
    private int nag;

    /**
     * The commentary that is included with this move.
     */
    private ArrayList<String> comments;

    /**
     * The termination marker that is after this move.
     */
    private Result termination;

    /**
     * The recursive annotation variation (RAV) associated with this move.
     * 
     * <p>
     * From the PGN specification:
     * {@snippet : 
     * An RAV (Recursive Annotation Variation) is a sequence of movetext
     * containing one or more moves enclosed in parentheses. An RAV is used to
     * represent an alternative variation. The alternate move sequence given by an
     * RAV is one that may be legally played by first unplaying the move that
     * appears immediately prior to the RAV. Because the RAV is a recursive
     * construct, it may be nested.
     * }
     * 
     */
    private ArrayList<ArrayList<PGNMove>> rav;

    /**
     * Creates a new PGN move.
     * 
     * @param move       The movetext.
     * @param moveNumber The number this move is.
     */
    public PGNMove(String move, int moveNumber) {

        this.moveText = move.trim();
        this.moveNumber = moveNumber;
        this.comments = new ArrayList<>();
        this.rav = new ArrayList<>();

    }

    /**
     * Gets the termination.
     * 
     * @return {@link #termination}
     */
    public Result getTermination() {
        return termination;
    }

    /**
     * Sets the game termination.
     * 
     * @param termination {@link #termination}
     */
    public void setTermination(Result termination) {
        this.termination = termination;
    }

    /**
     * Gets the move text.
     * 
     * @return {@link #moveText}
     */
    public String getMoveText() {
        return moveText;
    }

    /**
     * Gets the NAG.
     * 
     * @return {@link #NAGs}
     */
    public int getNag() {
        return nag;
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
     * Sets the move text.
     * 
     * @param moveText The text to set it to.
     */
    public void setMoveText(String moveText) {
        this.moveText = moveText;
    }

    /**
     * Sets the NAG.
     * 
     * @param nag The NAG.
     * @see #NAGs
     * @see #nag
     */
    public void setNag(int nag) {
        this.nag = nag;
    }

    /**
     * Gets the RAV associated with this move.
     * 
     * @return {@link #rav}
     */
    public ArrayList<ArrayList<PGNMove>> getRav() {
        return rav;
    }

    /**
     * Gets the comments associated with this move.
     * 
     * @return {@link #comments}
     */
    public ArrayList<String> getComments() {
        return comments;
    }

    /**
     * Outputs the move in PGN movetext format.
     */
    @Override
    public String toString() {

        String s = "";

        s += moveText;

        if (nag > 0)
            s += " $" + nag;

        for (int x = 0; x < comments.size(); x++) {

            s += " {" + comments.get(x) + "}";

        }

        if (rav.size() > 0) {

            for (int i = 0; i < rav.size(); i++) {
                s += " (";

                ArrayList<PGNMove> r = rav.get(i);

                for (int x = 0; x < r.size(); x++) {

                    PGNMove m = r.get(x);
                    boolean black = m.getMoveNumber() % 2 != 0;

                    s += (x == 0 ? "" : " ") + (((m.getMoveNumber()) / 2) + 1) + (black ? "..." : ".");

                    s += " " + m;

                    if (!black && x + 1 < r.size()) {

                        PGNMove o = r.get(++x);

                        if (m.getComments().size() > 0 || m.getNag() != 0)
                            s += " " + (((m.getMoveNumber()) / 2) + 1) + "...";

                        s += " " + o;

                    }

                }

                s += ")";
            }

        }

        if (termination != null) {
            switch (termination) {
                case WHITE_WIN:
                    s += " 1-0";
                    break;
                case BLACK_WIN:
                    s += " 0-1";
                    break;
                case DRAW:
                    s += " 1/2-1/2";
                    break;
                case IN_PROGRESS:
                    s += " *";
                    break;
                default:
                    break;
            }
        }

        return s;

    }

    /**
     * Gets a tag that is attached as commentary to the move. Tags are in this
     * format, including the brackets: {@code [%key value]}.
     * 
     * @param key The key to get.
     * @return The value associated with the key, or {@code null} if the key was not
     *         found.
     */
    public String getTag(String key) {

        for (int i = 0; i < comments.size(); i++) {

            Matcher m = Pattern.compile("\\[\\%(?<key>[^\\s]+)(?<value>[^\\]]+)\\]").matcher(comments.get(i));

            while (m.find()) {

                if (m.group("key").equals(key)) {
                    return m.group("value").trim().replaceAll("\n", "");
                }

            }

        }

        return null;

    }

    /**
     * Gets the %clk tag and parses it into a time in milliseconds.
     * 
     * @return The time in milliseconds the move ended.
     */
    public long getTimerEnd() {

        Pattern pat = Pattern.compile("(?<hrs>[\\d]+):(?<mins>[\\d]+):(?<secs>[\\d]+)");
        String clk = getTag("clk");

        if (clk == null || clk.equals(""))
            return -1;

        Matcher matcher = pat.matcher(clk);

        long timerEnd = 0;

        if (matcher.find()) {
            timerEnd += Integer.parseInt(matcher.group("hrs")) * 60 * 60 * 1000;
            timerEnd += Integer.parseInt(matcher.group("mins")) * 60 * 1000;
            timerEnd += Integer.parseInt(matcher.group("secs")) * 1000;
        } else
            timerEnd = -1;

        return timerEnd;

    }

}
