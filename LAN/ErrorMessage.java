package game.LAN;

/**
 * A message that is sent over LAN when an error message is sent.
 */
public class ErrorMessage extends Message {

    /**
     * An error that is of normal severity. Game does not need to stop.
     */
    public static final int NORMAL = 0;

    /**
     * An error that is fatal. The game must be terminated.
     */
    public static final int FATAL = 1;

    /**
     * A predefined terminate message for when the client disconnects.
     */
    public static final ErrorMessage TERMINATE = new ErrorMessage(FATAL, "Disconnected.");

    /**
     * The severity of this error.
     */
    private final int severity;

    /**
     * The reason for the error.
     */
    private final String reason;

    /**
     * Creates a new error message.
     * 
     * @param severity The severity of the error.
     * @param reason   The reason for the error.
     */
    public ErrorMessage(int severity, String reason) {

        super("error", severity + "", reason);

        this.severity = severity;
        this.reason = reason;

    }

    /**
     * Parses a received error message.
     * 
     * @param msg The message that was received.
     * @throws Exception If the message is in an invalid format.
     */
    public ErrorMessage(String msg) throws Exception {

        super(msg);

        if (args.size() != 3)
            throw new Exception("Invalid error message.");

        try {
            severity = Integer.parseInt(args.get(1));
        } catch (Exception e) {
            throw new Exception("Severity not a number.");
        }

        if (severity != NORMAL && severity != FATAL)
            throw new Exception("Invalid severity.");

        reason = args.get(2);

    }

    /**
     * Gets the severity.
     * 
     * @return {@link #severity}
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Gets the reason for the error.
     * 
     * @return {@link #reason}
     */
    public String getReason() {
        return reason;
    }

}
