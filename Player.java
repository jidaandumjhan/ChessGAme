package game;

/**
 * A representation of a player in a {@link Game}.
 */
public class Player {

    /**
     * An enumeration of the types of players.
     */
    public enum Type {

        /**
         * A player that is a human.
         */
        HUMAN("human"),

        /**
         * A player that is a computer program/engine/bot.
         */
        PROGRAM("program");

        /**
         * The string representation of the type of player.
         */
        private final String string;

        /**
         * Creates a new type.
         * 
         * @param string The string representation of the type.
         */
        Type(String string) {
            this.string = string;
        }

        /**
         * Gets the string representation of the type.
         * 
         * @return {@link #string}
         */
        public String getString() {
            return string;
        }

    }

    /**
     * The maximum length of a name.
     */
    public static final int MAX_NAME_LENGTH = 100;

    /**
     * A regular expression that matches a valid name.
     */
    public static final String NAME_REGEX = "[A-Za-z0-9!@#$%^&*()_\\-\\+=\"',. ?:\\/\\[\\]\\{\\}]{1," + MAX_NAME_LENGTH
            + "}";

    /** The name of the player. */
    private String name;

    /** The player's type. */
    private Type type;

    /** Whether or not the player is white. */
    private boolean white;

    /**
     * Creates a new Player object.
     * 
     * @param name The name of the player.
     * @throws Exception If the name is invalid.
     */
    public Player(String name) throws Exception {

        if (!name.matches(NAME_REGEX))
            throw new Exception("Invalid name.");

        this.name = name;

    }

    /**
     * Creates a new Player object.
     * 
     * @param name  The name of the player.
     * @param type  The type of player.
     * @param white Whether or not the player is white.
     * @throws Exception If the name is invalid.
     */
    public Player(String name, Type type, boolean white) throws Exception {

        if (!name.matches(NAME_REGEX))
            throw new Exception("Invalid name.");

        this.name = name;
        this.type = type;
        this.white = white;

    }

    /**
     * Gets the name.
     * 
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     * 
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type.
     * 
     * @return {@link #type}
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the player.
     * 
     * @param type The type.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets whether or not the player is white.
     * 
     * @return {@link #white}
     */
    public boolean isWhite() {
        return white;
    }

    /**
     * Sets whether the player is white.
     * 
     * @param white If the player is white.
     */
    public void setWhite(boolean white) {
        this.white = white;
    }

    /**
     * @return The player's name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Compares two {@link Player} objects.
     * 
     * @param o The {@link Player} object to compare to.
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Player))
            return false;

        Player casted = (Player) o;

        return name.equals(casted.getName()) && white == casted.isWhite();

    }

}
