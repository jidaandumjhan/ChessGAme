package game.LAN;

import game.Player;

/**
 * A message that is sent over LAN when initializing the game. Sent by the
 * person who accepted the challenge.
 */
public class InitMessage extends Message {

    /**
     * The version of the game the client is running on.
     */
    private final String version;

    /**
     * The username of the client sending the message.
     */
    private final String name;

    /**
     * Creates a new init message.
     * 
     * @param version The version of the game this client is running on.
     * @param name    This client's username.
     */
    public InitMessage(String version, String name) {

        super("init", version, name);

        this.version = version;
        this.name = name;

    }

    /**
     * Parses a received init message.
     * 
     * @param msg The message received.
     * @throws Exception If the message is invalid.
     */
    public InitMessage(String msg) throws Exception {

        super(msg);

        if (args.size() != 3)
            throw new Exception("Invalid init message.");

        this.version = args.get(1);
        this.name = args.get(2);

        if (!name.matches(Player.NAME_REGEX))
            throw new Exception("Invalid name.");

    }

    /**
     * Gets the version.
     * 
     * @return {@link #version}
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the name.
     * 
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

}
