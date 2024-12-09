package game.LAN;

import java.util.Date;

/**
 * A message that is sent over LAN when a chat message is sent.
 */
public class ChatMessage extends Message {

    /**
     * The timestamp of when the message was sent.
     */
    private final Date timestamp;

    /**
     * The message sent.
     */
    private final String message;

    /**
     * Creates a new chat message.
     * 
     * @param timestamp The time the message was sent.
     * @param message   The message that was sent.
     */
    public ChatMessage(Date timestamp, String message) {

        super("chat", timestamp.getTime() + "", message);

        this.timestamp = timestamp;
        this.message = message;

    }

    /**
     * Parses a chat message that was received.
     * 
     * @param msg The message received.
     * @throws Exception If the chat message is invalid.
     */
    public ChatMessage(String msg) throws Exception {

        super(msg);

        if (args.size() != 3)
            throw new Exception("Invalid chat message.");

        try {
            this.timestamp = new Date(Long.parseLong(args.get(1)));
        } catch (Exception e) {
            throw new Exception("Invalid timestamp.");
        }

        this.message = args.get(2);

    }

    /**
     * Gets the timestamp of the message.
     * 
     * @return {@link #timestamp}
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the message text.
     * 
     * @return {@link #message}
     */
    public String getMessage() {
        return message;
    }

}
