package game;

/**
 * The settings for the {@link Game}.
 */
public class GameSettings {

    /** The default starting position for standard chess. */
    public static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    /** The starting position of the game, in Forsyth-Edwards Notation (FEN). */
    private final String fen;

    /**
     * The time, in seconds, each side has in total. Should be {@code 0} if no
     * time control used.
     */
    private final long timePerSide;

    /**
     * The time, in seconds, each side gains per move made. Should be
     * {@code 0} if no time control is used or if no time is added per move.
     */
    private final long timePerMove;

    /**
     * Whether or not pausing is allowed.
     */
    private final boolean canPause;

    /**
     * Whether or not undo/redo is allowed.
     */
    private final boolean canUndo;

    /**
     * Whether or not white's timer should be automatically flipped/managed.
     */
    private final boolean whiteTimerManaged;

    /**
     * Whether or not black's timer should be automatically flipped/managed.
     */
    private final boolean blackTimerManaged;

    /**
     * Creates a new {@link GameSettings} object from the default starting position.
     * 
     * @param timePerSide       The amount of time each side has in seconds.
     * @param timePerMove       The amount of time each side gains per move in
     *                          seconds.
     * @param canPause          If pausing is allowed.
     * @param canUndo           If undoing and redoing is allowed.
     * @param whiteTimerManaged If white's timer should be automatically started and
     *                          stopped after moves.
     * @param blackTimerManaged If black's timer should be automatically started and
     *                          stopped after moves.
     * @throws Exception If the settings provided are invalid.
     */
    public GameSettings(long timePerSide, long timePerMove, boolean canPause, boolean canUndo,
            boolean whiteTimerManaged, boolean blackTimerManaged) throws Exception {

        this(DEFAULT_FEN, timePerSide, timePerMove, canPause, canUndo,
                whiteTimerManaged, blackTimerManaged);

    }

    /**
     * Creates a new {@link GameSettings} object from the default starting position.
     * 
     * @param FEN               The starting position in FEN notation.
     * @param timePerSide       The amount of time each side has in seconds.
     * @param timePerMove       The amount of time each side gains per move in
     *                          seconds.
     * @param canPause          If pausing is allowed.
     * @param canUndo           If undoing and redoing is allowed.
     * @param whiteTimerManaged If white's timer should be automatically started and
     *                          stopped after moves.
     * @param blackTimerManaged If black's timer should be automatically started and
     *                          stopped after moves.
     * @throws Exception If the settings provided are invalid.
     */
    public GameSettings(String FEN, long timePerSide, long timePerMove, boolean canPause, boolean canUndo,
            boolean whiteTimerManaged, boolean blackTimerManaged)
            throws Exception {

        this.fen = FEN;

        this.timePerSide = timePerSide <= 0 ? -1 : timePerSide;
        this.timePerMove = timePerMove <= 0 ? -1 : timePerMove;
        this.canPause = canPause;
        this.canUndo = canUndo;
        this.whiteTimerManaged = whiteTimerManaged;
        this.blackTimerManaged = blackTimerManaged;

        if ((!whiteTimerManaged || !blackTimerManaged) && (canPause || canUndo))
            throw new Exception("Invalid settings.");

    }

    /**
     * Gets the starting FEN.
     * 
     * @return {@link #fen}
     */
    public String getFen() {
        return fen;
    }

    /**
     * Gets the time each side has.
     * 
     * @return {@link #timePerSide}
     */
    public long getTimePerSide() {
        return timePerSide;
    }

    /**
     * Gets the time each side gains per each move.
     * 
     * @return {@link #timePerMove}
     */
    public long getTimePerMove() {
        return timePerMove;
    }

    /**
     * Gets if pausing is permitted.
     * 
     * @return {@link #canPause}
     */
    public boolean canPause() {
        return canPause;
    }

    /**
     * Gets if undoing is permitted.
     * 
     * @return {@link #canUndo}
     */
    public boolean canUndo() {
        return canUndo;
    }

    /**
     * Gets if white's timer will be managed.
     * 
     * @return {@link #whiteTimerManaged}
     */
    public boolean isWhiteTimerManged() {
        return whiteTimerManaged;
    }

    /**
     * Gets if black's timer will be managed.
     * 
     * @return {@link #blackTimerManaged}
     */
    public boolean isBlackTimerManaged() {
        return blackTimerManaged;
    }

}
