package game.LAN;

import java.util.ArrayList;

/**
 * A message that is sent over LAN.
 */
public class Message {

    /**
     * A predefined messasge for sending a draw offer.
     */
    public static final Message DRAW_OFFER = new Message("draw");

    /**
     * A predefined messasge for accepting a draw offer.
     */
    public static final Message DRAW_ACCEPT = new Message("drawaccept");

    /**
     * A predefined messasge for declining a draw offer.
     */
    public static final Message DRAW_DECLINE = new Message("drawdecline");

    /**
     * A predefined messasge for a resigning.
     */
    public static final Message RESIGN = new Message("resign");

    /**
     * A predefined messasge for starting the game.
     */
    public static final Message START = new Message("start");

    /**
     * A predefined message for confirming the game is started.
     */
    public static final Message STARTED = new Message("started");

    /**
     * The arguments included in this message. Will be separated by a semicolon when
     * output.
     */
    protected ArrayList<String> args;

    /**
     * Creates a new message.
     * 
     * @param text The text included in the message.
     */
    public Message(String text) {

        this.args = new ArrayList<String>();

        String[] split = text.split("(?<!\\\\);");

        for (int i = 0; i < split.length; i++) {
            args.add(split[i].replaceAll("\\\\;", ";"));
        }

    }

    /**
     * Parses a new message with arguments.
     * 
     * @param split The arguments included in the message.
     */
    public Message(String... split) {

        this.args = new ArrayList<String>();

        for (int i = 0; i < split.length; i++) {
            args.add(split[i].replaceAll("\\\\;", ";"));
        }

    }

    /**
     * Gets the arguments associated with the message.
     * 
     * @return {@link #args}
     */
    public ArrayList<String> getArgs() {
        return args;
    }

    /**
     * Checks whether two messages are equal to each other.
     */
    @Override
    public boolean equals(Object compare) {

        if (!(compare instanceof Message))
            return false;

        Message casted = (Message) (compare);

        boolean same = args.size() == casted.getArgs().size();

        for (int i = 0; same && i < args.size(); i++) {

            if (!args.get(i).equals(casted.getArgs().get(i)))
                same = false;

        }

        return same;

    }

    /**
     * Outputs the message in a format that can be sent to other clients.
     */
    @Override
    public String toString() {

        String text = "";

        for (int i = 0; i < args.size(); i++) {
            text += args.get(i).replaceAll(";", "\\\\;") + ";";
        }

        return text;

    }

}
