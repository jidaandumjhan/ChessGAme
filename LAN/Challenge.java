package game.LAN;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import game.Game;
import game.Player;

/**
 * A challenge which contains the details of a game offered to other players.
 */
public class Challenge {

    /**
     * The color of the client will be randomly selected before the game starts.
     */
    public static final int CHALLENGE_RANDOM = 0;

    /**
     * The color of the client will be white.
     */
    public static final int CHALLENGE_WHITE = 1;

    /**
     * The color of the client will be black.
     */
    public static final int CHALLENGE_BLACK = 2;

    /** The version of the game the challenge is for. */
    private final String version;

    /** The name of the user making the challenge. */
    private final String name;

    /** The FEN of the starting position of the challenge. */
    private final String fen;

    /**
     * The color of the user who created the challenge, correlating to
     * {@link #CHALLENGE_RANDOM}, {@link #CHALLENGE_WHITE}, and
     * {@link #CHALLENGE_BLACK}.
     */
    private final int color;

    /** The time each side will have per the challenge. */
    private final long timePerSide;

    /** The time each side will gain per each move. */
    private final long timePerMove;

    /** The address of the user who created the challenge. */
    private final InetAddress address;

    /**
     * Creates a new challenge for a LAN game.
     * 
     * @param name        The user's name.
     * @param fen         The starting position, in FEN notation.
     * @param color       The color of the user creating the challenge.
     * @param timePerSide The time each side has in total.
     * @param timePerMove The time each side gains per move.
     * @param address     The address of the user creating the challenge.
     * @throws Exception If the challenge parameters are not valid.
     */
    public Challenge(final String name, final String fen, final int color, final long timePerSide,
            final long timePerMove, final InetAddress address)
            throws Exception {

        this.version = Game.VERSION;
        this.name = name;
        this.fen = fen;
        this.color = color;
        this.timePerSide = timePerSide;
        this.timePerMove = timePerMove;
        this.address = address;

        checkIfValid();

    }

    /**
     * Creates a challenge that was received from a packet.
     * 
     * @param packet The packet that contains the challenge data.
     * @throws Exception If the challenge data is not valid.
     */
    public Challenge(final DatagramPacket packet) throws Exception {

        final String str = new String(packet.getData()).trim();

        final Message msg = new Message(str);

        final ArrayList<String> a = msg.getArgs();

        if (a.size() != 6) {
            throw new Exception("Invalid challenge.");
        }

        version = a.get(0);

        name = a.get(1);

        fen = a.get(2);

        try {
            color = Integer.parseInt(a.get(3));
        } catch (final Exception e) {
            throw new Exception("Invalid color.");
        }

        try {
            timePerSide = Integer.parseInt(a.get(4));
        } catch (final Exception e) {
            throw new Exception("Invalid time per side.");
        }

        try {
            timePerMove = Integer.parseInt(a.get(5));
        } catch (final Exception e) {
            throw new Exception("Invalid time per move.");
        }

        address = packet.getAddress();

        checkIfValid();

    }

    /**
     * Gets the starting FEN of the challenge.
     * 
     * @return {@link #fen}
     */
    public String getFen() {
        return fen;
    }

    /**
     * Gets the version of the challenging client.
     * 
     * @return {@link #version}
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the name of the challenger.
     * 
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color of the challenger.
     * 
     * @return {@link #color}
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets the amount of time each side will have.
     * 
     * @return {@link #timePerSide}
     */
    public long getTimePerSide() {
        return timePerSide;
    }

    /**
     * Gets the amount of time each side will gain after each move is made.
     * 
     * @return {@link #timePerMove}
     */
    public long getTimePerMove() {
        return timePerMove;
    }

    /**
     * Gets the IP address of the other client.
     * 
     * @return {@link #address}
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Outputs this challenge in text format that can be sent to other users.
     */
    public String toString() {

        final String str = version + ";" + name + ";" + fen + ";" + color + ";" + timePerSide + ";" + timePerMove + ";";
        return str;

    }

    /**
     * Checks if two challenges are equal.
     */
    @Override
    public boolean equals(final Object compare) {

        if (address == null && compare == null)
            return true;
        else if (address == null)
            return false;

        if (!(compare instanceof Challenge))
            return false;

        if (compare == this)
            return true;

        final Challenge cast = (Challenge) compare;

        return name.equals(cast.getName()) && address.equals(cast.getAddress()) && fen.equals(cast.getFen())
                && color == cast.getColor();

    }

    /**
     * Checks if this challenge is valid.
     * 
     * @throws Exception If the challenge is not valid.
     */
    private void checkIfValid() throws Exception {

        if (name.length() > Player.MAX_NAME_LENGTH)
            throw new Exception("Your name is too long.");

        if (!name.matches(Player.NAME_REGEX))
            throw new Exception("Invalid name.");

        if (color < Challenge.CHALLENGE_RANDOM || color > CHALLENGE_BLACK)
            throw new Exception("Invalid color.");

    }

}
