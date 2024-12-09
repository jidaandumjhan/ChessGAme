package game.LAN;

import game.Square;

/**
 * A message that is sent over LAN when a move is made.
 */
public class MoveMessage extends Message {

    /**
     * The origin square of the move.
     */
    private final Square origin;

    /**
     * The destination square of the move.
     */
    private final Square destination;

    /**
     * The promotion type associated with this move. Should be {@code 0} if no
     * promotion is applicable.
     */
    private final char promoteType;

    /**
     * The time on the timer once this move was made.
     */
    private final long timerEnd;

    /**
     * Creates a new move message.
     * 
     * @param origin      The origin square of the move.
     * @param destination The destination square of the move.
     * @param promoteType The promotion type of the move.
     * @param timerEnd    The time on the timer after the move is made.
     */
    public MoveMessage(Square origin, Square destination, char promoteType, long timerEnd) {

        super("move", origin.toString(), destination.toString(), promoteType + "", timerEnd + "");

        this.origin = origin;
        this.destination = destination;
        this.promoteType = promoteType;
        this.timerEnd = timerEnd;

    }

    /**
     * Parses a move message that was received.
     * 
     * @param msg The message received.
     * @throws Exception If the message is invalid.
     */
    public MoveMessage(String msg) throws Exception {

        super(msg);

        if (args.size() != 5)
            throw new Exception("Invalid move message.");

        origin = new Square(args.get(1));
        destination = new Square(args.get(2));

        promoteType = args.get(3).charAt(0);
        if (promoteType != '0' && promoteType != 'Q' && promoteType != 'R' && promoteType != 'B' && promoteType != 'N')
            throw new Exception("Invalid promote type.");

        timerEnd = Long.parseLong(args.get(4));

    }

    /**
     * Gets the origin.
     * 
     * @return {@link #origin}
     */
    public Square getOrigin() {
        return origin;
    }

    /**
     * Gets the destination.
     * 
     * @return {@link #destination}
     */
    public Square getDestination() {
        return destination;
    }

    /**
     * Gets the promote type.
     * 
     * @return {@link #promoteType}
     */
    public char getPromoteType() {
        return promoteType;
    }

    /**
     * Gets the timer end.
     * 
     * @return {@link #timerEnd}
     */
    public long getTimerEnd() {
        return timerEnd;
    }

}
