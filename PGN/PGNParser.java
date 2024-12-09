package game.PGN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.Game;
import game.Position;
import game.Game.Result;

/**
 * Class to parse .pgn files following this specification:
 * http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm
 */
public class PGNParser {

    /**
     * Matches the various tokens described in the PGN standard.
     */
    private static final String TOKEN_REGEX = "(?<termination>1-0|0-1|1/2-1/2|\\*)|(?<period>\\.)|\\[|\\]|\\(|\\)|\\<|\\>|(?<nag>\\$[\\d]+)|(?<symbol>[A-Za-z0-9][A-Za-z0-9_+#=:\\-]*)|(?<num>[\\d]+)|(?<str>\"[^\"]*\")|(?<comment>\\{[^}]*\\})|(?<eol>\\;([^\\n]*))|(?<suffix>[?!]{1,2})";

    /**
     * Converts a time in milliseconds to the format used in the %clk tag.
     * 
     * @param time The time to convert.
     * @return A string representing the time.
     */
    public static String millisToOutputFormat(long time) {

        long hours = (time / 1000 / 60 / 60);
        long minutes = (time / 1000 / 60 % 60);
        long seconds = (time / 1000 % 60 % 60 % 60);

        String s = "";

        s += hours + ":";

        if (minutes < 10) {
            s += "0";
        }
        s += minutes + ":";

        if (seconds < 10) {
            s += "0";
        }
        s += seconds;

        return s;

    }

    /** The original content of the PGN file. */
    private String text;

    /** The tags contained in this PGN. */
    private Map<String, String> tags;

    /** Any comments that are before the first move. */
    private ArrayList<String> comments;

    /** The moves. */
    private ArrayList<PGNMove> moves;

    /**
     * Parses a PGN game based on the input text.
     * 
     * @param text The content of the {@code .pgn} file.
     * @throws Exception If there is an error parsing the game.
     */
    public PGNParser(String text) throws Exception {

        this.text = text.trim();
        tags = new HashMap<String, String>();
        moves = new ArrayList<PGNMove>();
        comments = new ArrayList<>();

        parse();

    }

    /**
     * Parses a PGN game based on a {@link Game} object..
     * 
     * @param game         The game to parse.
     * @param tags         The tags to include in the PGN.
     * @param includeClock Whether or not the time that the timer ends should be
     *                     included in the PGN.
     * @throws Exception If there is an error parsing the game.
     */
    public PGNParser(Game game, Map<String, String> tags, boolean includeClock) throws Exception {

        text = "";
        this.tags = tags;
        moves = new ArrayList<>();
        comments = new ArrayList<>();

        for (int i = 1; i < game.getPositions().size(); i++) {

            Position p = game.getPositions().get(i);

            String comment = null;

            if (includeClock && game.getSettings().getTimePerSide() > 0) {

                final boolean isTurn = p.isWhite() && i == game.getPositions().size() - 1;
                int moveCount = (int) Math.ceil(p.getMoveNumber() / 2.0);

                if (isTurn && game.getPositions().get(0).isWhite() != p.isWhite())
                    --moveCount;

                long time = game.getPositions().get(i - 1).getTimerEnd() + game.calcTimerDelta(moveCount);

                if (time > -1) {
                    comment = "[%clk "
                            + millisToOutputFormat(time)
                            + "]";
                }

            }

            PGNMove m = new PGNMove(p.getMoveString(), p.getMoveNumber() - 1);
            if (comment != null)
                m.getComments().add(comment);

            moves.add(m);

        }

    }

    /**
     * Gets the text.
     * 
     * @return {@link #text}
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the tags.
     * 
     * @return {@link #tags}
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Gets the moves.
     * 
     * @return {@link #moves}
     */
    public ArrayList<PGNMove> getMoves() {
        return moves;
    }

    /**
     * Gets the amount of time each side has based on the TimeControl tag.
     * 
     * @return The time in milliseconds
     */
    public long getTimePerSide() {

        final String tc = tags.getOrDefault("TimeControl", "-");
        if (!tc.matches("[\\d]+(\\+[\\d]+)?"))
            return 0;

        final String[] split = tc.split("\\+");

        try {
            return Integer.parseInt(split[0]);
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * Gets the amount of time per move based on the TimeControl tag.
     * 
     * @return The time in milliseconds
     */
    public long getTimePerMove() {

        final String tc = tags.getOrDefault("TimeControl", "-");
        if (!tc.matches("[\\d]+(\\+[\\d]+)?"))
            return 0;

        final String[] split = tc.split("\\+");

        try {
            return Integer.parseInt(split[1]);
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * Outputs the parsed PGN.
     * 
     * @param includeTags Whether or not to include the game info tags. If
     *                    {@code false}, only the movetext will be included.
     * @return The game in PGN format.
     */
    public String outputPGN(boolean includeTags) {

        String str = "";

        if (includeTags) {

            str += "[Event \"" + tags.getOrDefault("Event", "?") + "\"]\n";
            str += "[Site \"" + tags.getOrDefault("Site", "?") + "\"]\n";
            str += "[Date \"" + tags.getOrDefault("Date", "????.??.??") + "\"]\n";
            str += "[Round \"" + tags.getOrDefault("Round", "-") + "\"]\n";
            str += "[White \"" + tags.getOrDefault("White", "?") + "\"]\n";
            str += "[Black \"" + tags.getOrDefault("Black", "?") + "\"]\n";
            str += "[Result \"" + tags.getOrDefault("Result", "*") + "\"]\n";

            for (Map.Entry<String, String> tag : tags.entrySet()) {

                if (tag.getKey().equals("Event") || tag.getKey().equals("Site") || tag.getKey().equals("Date")
                        || tag.getKey().equals("Round") || tag.getKey().equals("White") || tag.getKey().equals("Black")
                        || tag.getKey().equals("Result"))
                    continue;

                str += "[" + tag.getKey() + " \"" + tag.getValue() + "\"]\n";

            }

            str += "\n";

        }

        String moveList = "";

        for (int i = 0; i < comments.size(); i++) {

            moveList += "{" + comments.get(i) + "}\n";

        }

        for (int i = 0; i < moves.size(); i++) {

            PGNMove m = moves.get(i);

            moveList += " " + ((i / 2) + 1) + ".";

            moveList += " " + m;

            if (i + 1 < moves.size()) {

                PGNMove o = moves.get(++i);

                if (moves.get(i).getComments().size() > 0 ||
                        moves.get(i).getNag() != 0)
                    moveList += " " + ((i / 2) + 1) + "...";

                moveList += " " + o;

            }

        }

        while (moveList.length() > 80) {

            int find = moveList.lastIndexOf(" ", 80);

            if (find <= -1)
                find = 79;

            str += moveList.substring(0, find + 1).trim() + "\n";
            moveList = moveList.substring(find + 1);

        }
        str += moveList;
        str += "\n";

        return str;
    }

    /**
     * Parses a PGN file.
     * 
     * @throws Exception If the PGN file is formatted incorrectly or the game is
     *                   invalid.
     */
    private void parse() throws Exception {

        Matcher t = Pattern.compile(TOKEN_REGEX).matcher(text);
        int ravDepth = 0;

        while (t.find()) {

            String tok = t.group();

            if (tok.equals("[")) {

                if (t.find()) {

                    String key = t.group();

                    if (t.find()) {

                        String value = String.join("", t.group().split("\""));

                        if (t.find()) {

                            if (t.group().equals("]")) {

                                tags.put(key, value);

                            } else {
                                throw new Exception("Tag @ " + t.start() + " not closed.");
                            }

                        } else {
                            throw new Exception("Tag @ " + t.start() + " not closed.");
                        }

                    } else {
                        throw new Exception("Tag and key found @ " + t.start() + ", but no value found.");
                    }

                } else {
                    throw new Exception("Tag started @ " + t.start() + ", but no key found.");
                }

            } else if (tok.matches("1-0|0-1|1/2-1/2|\\*")) {

                PGNMove last = getLast(ravDepth).get(getLast(ravDepth).size() - 1);

                switch (tok) {
                    case "1-0":
                        last.setTermination(Result.WHITE_WIN);
                        break;
                    case "0-1":
                        last.setTermination(Result.BLACK_WIN);
                        break;
                    case "1/2-1/2":
                        last.setTermination(Result.DRAW);
                        break;
                    case "*":
                        last.setTermination(Result.IN_PROGRESS);

                }

                // Comments
            } else if (tok.matches("\\{[^}]*\\}|\\;([^\\n]*)")) {

                boolean eol = tok.startsWith(";");
                String comment = eol ? tok.substring(1) : tok.substring(1, tok.length() - 1);

                if (moves.size() == 0)
                    comments.add(comment);
                else
                    getLast(ravDepth).get(getLast(ravDepth).size() - 1).getComments()
                            .add(comment.replaceAll("\n", " "));

                // NAGs
            } else if (tok.matches("\\$[\\d]+")) {

                if (getLast(ravDepth).size() == 0)
                    throw new Exception("Error @ " + t.start() + ". NAG given before a move.");

                try {
                    getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(Integer.parseInt(tok.substring(1)));
                } catch (Exception e) {
                    throw new Exception("Error @ " + t.start() + ". Invalid NAG.");
                }

                // Suffixes
            } else if (tok.matches("[?!]{1,2}")) {

                if (getLast(ravDepth).size() == 0)
                    throw new Exception("Error @ " + t.start() + ". Suffix given before a move.");

                switch (tok) {

                    case "!":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(1);
                        break;
                    case "?":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(2);
                        break;
                    case "!!":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(3);
                        break;
                    case "!?":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(4);
                        break;
                    case "?!":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(5);
                        break;
                    case "??":
                        getLast(ravDepth).get(getLast(ravDepth).size() - 1).setNag(6);
                        break;
                    default:
                        throw new Exception("Error @ " + t.start() + ". Invalid suffix.");
                }

                // RAV
            } else if (tok.matches("\\(")) {

                ++ravDepth;

                if (moves.size() == 0)
                    throw new Exception("Error @ " + t.start() + ". RAV given before a move.");

                ArrayList<ArrayList<PGNMove>> a = moves.get(moves.size() - 1).getRav();
                for (int i = 1; i < ravDepth; i++) {

                    ArrayList<PGNMove> r = a.get(a.size() - 1);
                    a = r.get(r.size() - 1).getRav();

                }

                a.add(new ArrayList<PGNMove>());

                // Move numbers
            } else if (tok.matches("\\)")) {
                --ravDepth;

                if (ravDepth < 0)
                    throw new Exception("Error @ " + t.start() + ". Unmatched closing parentheses.");

            } else if (tok.matches("[\\d]+")) {

                if ((ravDepth == 0 && (int) Math.ceil((moves.size() + 1) / 2.0) != Integer.parseInt(tok))
                        || (ravDepth > 0 && (int) Math
                                .ceil((getLast(ravDepth - 1).get(getLast(ravDepth - 1).size() - 1).getMoveNumber() + 1
                                        + getLast(ravDepth).size()) / 2.0) != Integer.parseInt(tok)))
                    throw new Exception("Error @ " + t.start() + ". Unexpected move number.");

            } else if (tok.matches("[A-Za-z0-9][A-Za-z0-9_+#=:\\-]*")) {

                if (ravDepth == 0)
                    moves.add(new PGNMove(tok, moves.size()));
                else {

                    ArrayList<PGNMove> a = getLast(ravDepth);
                    a.add(new PGNMove(tok,
                            getLast(ravDepth - 1).get(getLast(ravDepth - 1).size() - 1).getMoveNumber() + a.size()));

                }

            }

        }

    }

    /**
     * Gets the list of moves currently being targeted. Used for recursive
     * annotation variations (RAVs). If {@code ravDepth} is {@code 0},
     * {@link #moves} will be returned.
     * 
     * @param ravDepth The depth of parentheses currently being parsed.
     * @return The move list currently being targeted.
     */
    private ArrayList<PGNMove> getLast(int ravDepth) {

        ArrayList<PGNMove> a = moves;
        for (int i = 0; i < ravDepth; i++) {

            ArrayList<ArrayList<PGNMove>> r = a.get(a.size() - 1).getRav();
            a = r.get(r.size() - 1);

        }

        return a;

    }

}
