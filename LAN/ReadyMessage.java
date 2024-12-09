package game.LAN;

import game.Player;

/**
 * A message that is sent over LAN when a client is ready to start the game.
 * Sent by the person who created the challenge.
 */
public class ReadyMessage extends Message {

    /**
     * The color of the opponent.
     */
    private final int oppColor;

    /**
     * The name of the user.
     */
    private final String name;

    /**
     * The starting position in FEN format.
     */
    private final String fen;

    /**
     * The amount of time each side has.
     */
    private final long timePerSide;

    /**
     * The amount of time each side gains per move.
     */
    private final long timePerMove;

    /**
     * Creates a new ready message.
     * 
     * @param oppColor    The color of the opponent.
     * @param name        The name of the player.
     * @param fen         The starting position in FEN format.
     * @param timePerSide The amount of time each side has.
     * @param timePerMove The amount of time each side gains per move.
     */
    public ReadyMessage(int oppColor, String name, String fen, long timePerSide, long timePerMove) {

        super("ready", oppColor + "", name, fen, timePerSide + "", timePerMove + "");

        this.oppColor = oppColor;
        this.name = name;
        this.fen = fen;
        this.timePerSide = timePerSide;
        this.timePerMove = timePerMove;

    }

    /**
     * Parses a received ready message.
     * 
     * @param msg The message received.
     * @throws Exception If the message is not a valid ready message.
     */
    public ReadyMessage(String msg) throws Exception {

        super(msg);

        if (args.size() != 6)
            throw new Exception("Invalid ready message.");

        try {
            this.oppColor = Integer.parseInt(args.get(1));
        } catch (Exception e) {
            throw new Exception("Invalid opponent color.");
        }

        if (oppColor != Challenge.CHALLENGE_WHITE && oppColor != Challenge.CHALLENGE_BLACK)
            throw new Exception("Invalid opponent color.");

        this.name = args.get(2);

        if (!name.matches(Player.NAME_REGEX))
            throw new Exception("Invalid name.");

        this.fen = args.get(3);

        try {
            this.timePerSide = Long.parseLong(args.get(4));
        } catch (Exception e) {
            throw new Exception("Invalid time per side.");
        }

        try {
            this.timePerMove = Long.parseLong(args.get(5));
        } catch (Exception e) {
            throw new Exception("Invalid time per move.");
        }

    }

    /**
     * Gets the starting position in FEN format.
     * 
     * @return The starting position.
     */
    public String getFen() {
        return fen;
    }

    /**
     * Gets the opponent's color.
     * 
     * @return The opponent's color.
     */
    public int getOppColor() {
        return oppColor;
    }

    /**
     * Gets the player's name.
     * 
     * @return The player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the amount of time each side has.
     * 
     * @return The amount of time each side has.
     */
    public long getTimePerSide() {
        return timePerSide;
    }

    /**
     * Gets the amount of time each side gains per move.
     * 
     * @return The amount of time each side gains per move.
     */
    public long getTimePerMove() {
        return timePerMove;
    }

}
