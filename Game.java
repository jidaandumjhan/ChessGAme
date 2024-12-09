package game;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import game.GameEvent.Type;
import game.PGN.PGNMove;
import game.PGN.PGNParser;

/**
 * A game of chess.
 */
public class Game {

    /**
     * An enumeration of the result states of the game.
     * 
     * @see Reason
     */
    public enum Result {

        /**
         * The game has not started yet.
         */
        NOT_STARTED,

        /**
         * The game is currently in progress.
         */
        IN_PROGRESS,

        /**
         * White has won the game.
         */
        WHITE_WIN,

        /**
         * Black has won the game.
         */
        BLACK_WIN,

        /**
         * The game has concluded in a draw.
         */
        DRAW,

        /**
         * The game was terminated.
         */
        TERMINATED

    }

    /**
     * An enumeration of all the reasons for the {@link Result}.
     * 
     * @see Result
     */
    public enum Reason {

        /**
         * The game is in progress.
         */
        IN_PROGRESS,

        /**
         * The game has concluded due to a checkmate by either side.
         */
        CHECKMATE,

        /**
         * The game has concluded due to one side running out of time (flagfall.)
         */
        FLAGFALL,

        /**
         * The game has concluded because black accepted a draw that white offered.
         */
        WHITE_OFFERED_DRAW,

        /**
         * The game has concluded because white accepted a draw that black offered.
         */
        BLACK_OFFERED_DRAW,

        /**
         * The game has concluded in a draw due to stalemate.
         */
        STALEMATE,

        /**
         * The game has concluded in a draw due to a dead position because there are
         * insufficient pieces for either side to perform a checkmate.
         */
        DEAD_INSUFFICIENT_MATERIAL,

        /**
         * The game has concluded in a draw due to a dead position because there is no
         * way a checkmate can be achieved.
         */
        DEAD_NO_POSSIBLE_MATE,

        /**
         * The game has concluded in a draw because the same position was repeated three
         * times over the course of the game.
         */
        REPETITION,

        /**
         * The game has concluded in a draw because there has been no capture or pawn
         * move in the last fifty moves (one hundred half-moves.)
         */
        FIFTY_MOVE,

        /**
         * The game has concluded because either color has resigned.
         */
        RESIGNATION,

        /**
         * The game has concluded for another reason.
         */
        OTHER;

    }

    /**
     * The version of the game.
     */
    public static final String VERSION = Game.class.getPackage().getImplementationVersion() != null
            ? Game.class.getPackage().getImplementationVersion()
            : "DEV";

    /**
     * The settings of the game.
     */
    private GameSettings settings;

    /**
     * The white player.
     */
    private Player white;

    /**
     * The black player.
     */
    private Player black;

    /**
     * A timestamp of when the game started.
     */
    private Date start;

    /**
     * A list of the chat messages sent throughout the game.
     */
    private LinkedList<Chat> messages;

    /**
     * A list of the positions in this game, in order.
     */
    private ArrayList<Position> positions;

    /**
     * A list of the registered listeners listening for game events.
     * 
     * @see #addListener(GameListener)
     */
    private LinkedList<GameListener> listeners;

    /**
     * <p>
     * The result of the game.
     */
    private Result result;

    /**
     * <p>
     * The reason for the result of the game.
     */
    private Reason resultReason;

    /**
     * The system time that the active timer was started.
     */
    private long timerStart;

    /**
     * Whether or not the game is paused.
     */
    private boolean paused;

    /**
     * The player that offered a draw. {@code null} if no draw has been offered or
     * the previous draw offer has been declined.
     */
    private Player drawOfferer;

    /**
     * The service that checks for flagfall in the background.
     */
    private ScheduledExecutorService flagfallChecker;

    /**
     * The flagfall checker task.
     */
    private Runnable flagfall = () -> {

        if (getTimerTime(true) <= 0)
            markGameOver(Game.Result.BLACK_WIN, Game.Reason.FLAGFALL);

        if (getTimerTime(false) <= 0)
            markGameOver(Game.Result.WHITE_WIN, Game.Reason.FLAGFALL);

    };

    /**
     * Initializes a new Game with the specified settings.
     * 
     * @param whiteName The name of the white player.
     * @param blackName The name of the black player.
     * @param whiteType The type of the white player.
     * @param blackType The type of the white player.
     * @param settings  The settings used for this game.
     * @throws Exception If there is an error with the starting position or the
     *                   player names are invalid.
     */
    public Game(String whiteName, String blackName, Player.Type whiteType, Player.Type blackType, GameSettings settings)
            throws Exception {

        this.white = new Player(whiteName, whiteType, true);
        this.black = new Player(blackName, blackType, false);

        positions = new ArrayList<Position>();
        listeners = new LinkedList<GameListener>();
        messages = new LinkedList<Chat>();

        this.settings = settings;

        result = Game.Result.NOT_STARTED;
        resultReason = Game.Reason.IN_PROGRESS;

        if (settings.getFen().equals(GameSettings.DEFAULT_FEN))
            positions.add(new Position());
        else
            positions.add(new Position(settings.getFen()));

    }

    /**
     * Initializes a Game based on a parsed PGN.
     * 
     * @param pgn                 The parsed PGN.
     * @param settings            The settings used for this game.
     * @param overridePGNSettings Whether or not the settings specified in
     *                            {@code settings} should override the settings
     *                            specified in the parsed PGN.
     * @throws Exception If there is an error importing the parsed PGN.
     */
    public Game(PGNParser pgn, GameSettings settings, boolean overridePGNSettings) throws Exception {

        positions = new ArrayList<Position>();
        messages = new LinkedList<Chat>();
        listeners = new LinkedList<GameListener>();

        result = Game.Result.NOT_STARTED;
        resultReason = Game.Reason.IN_PROGRESS;

        // White
        final String whiteName = pgn.getTags().getOrDefault("White", "White");
        Player.Type whiteType = Player.Type.HUMAN;

        String whiteTypeString = pgn.getTags().getOrDefault("WhiteType", Player.Type.HUMAN.getString());
        switch (whiteTypeString) {
            case "program":
                whiteType = Player.Type.PROGRAM;
            default:
                whiteType = Player.Type.HUMAN;
        }

        white = new Player(whiteName, whiteType, true);

        // Black
        final String blackName = pgn.getTags().getOrDefault("Black", "Black");
        Player.Type blackType = Player.Type.HUMAN;

        String blackTypeString = pgn.getTags().getOrDefault("BlackType", Player.Type.HUMAN.getString());
        switch (blackTypeString) {
            case "program":
                blackType = Player.Type.PROGRAM;
            default:
                blackType = Player.Type.HUMAN;
        }

        black = new Player(blackName, blackType, false);

        // If custom starting position used
        final String setup = pgn.getTags().getOrDefault("SetUp", "");
        final String fen = pgn.getTags().getOrDefault("FEN", "");

        if (setup.equals("1") && !fen.equals(""))
            positions.add(new Position(fen));
        else
            positions.add(new Position());

        // Game settings
        this.settings = new GameSettings(setup.equals("1") && !fen.equals("") ? fen : GameSettings.DEFAULT_FEN,
                overridePGNSettings ? settings.getTimePerSide() : pgn.getTimePerSide(),
                overridePGNSettings ? settings.getTimePerMove() : pgn.getTimePerMove(),
                settings.canPause(),
                settings.canUndo(),
                settings.isWhiteTimerManged(),
                settings.isBlackTimerManaged());

        // Importing moves
        ArrayList<PGNMove> pMoves = pgn.getMoves();

        for (int i = 0; i < pMoves.size(); i++) {

            final String m = pMoves.get(i).getMoveText();

            try {

                char promote = m.charAt(m.length() - 1);

                if (!(promote + "").matches("[QRBN]"))
                    promote = '0';

                positions.add(new Position(getLastPos(), getLastPos().getMoveBySAN(m),
                        promote, true));

                getPreviousPos().setTimerEnd(
                        pMoves.get(i).getTimerEnd()
                                - calcTimerDelta(calcMovesPerSide(getPreviousPos().isWhite(), positions.size() - 1)));

            } catch (Exception e) {
                throw new Exception("Error importing PGN at move " + i + ", \"" + m + "\". " + e.getMessage());
            }

        }

        fireEvent(new GameEvent(Type.IMPORTED));

        // Setting the result
        final String res = pgn.getTags().getOrDefault("Result", "*");
        switch (res) {
            case "1/2-1/2":
                result = Result.DRAW;
                resultReason = Reason.OTHER;
                break;
            case "1-0":
                result = Result.WHITE_WIN;
                resultReason = Reason.OTHER;
                break;
            case "0-1":
                result = Result.BLACK_WIN;
                resultReason = Reason.OTHER;
                break;
        }

    }

    /**
     * Gets the settings of the game.
     * 
     * @return {@link #settings}
     */
    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Gets the chat messages sent during the game.
     * 
     * @return {@link #messages}
     */
    public LinkedList<Chat> getMessages() {
        return messages;
    }

    /**
     * Gets the positions of the game.
     * 
     * @return {@link #positions}
     */
    public ArrayList<Position> getPositions() {
        return positions;
    }

    /**
     * Gets the result of the game.
     * 
     * @return {@link #result}
     */
    public Result getResult() {
        return result;
    }

    /**
     * Gets the reason for the result of the game.
     * 
     * @return {@link #resultReason}
     */
    public Reason getResultReason() {
        return resultReason;
    }

    /**
     * Gets whether or not the game is paused.
     * 
     * @return {@link #paused}
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Gets the position at the end of the list of positions.
     * 
     * @return the most recent position, the last position in the list of
     *         positions
     */
    public Position getLastPos() {
        return positions.get(positions.size() - 1);
    }

    /**
     * Gets the second to last position in the list of positions.
     * 
     * @return the second to last position in the list of positions.
     */
    public Position getPreviousPos() {

        if (positions.size() == 1)
            return null;

        return positions.get(positions.size() - 2);

    }

    /**
     * Gets the player of the given color.
     * 
     * @param white If the white player should be returned. If {@code false} the
     *              black player will be returned.
     * @return The {@link Player} requested.
     */
    public Player getPlayer(boolean white) {
        return white ? this.white : this.black;
    }

    /**
     * Gets the player that offered the currently active draw offer. May be
     * {@code null} if no draw is currently being offered.
     * 
     * @return {@link #drawOfferer}
     */
    public Player getDrawOfferer() {
        return drawOfferer;
    }

    /**
     * Calculates the number of moves each side has completed from the last
     * position.
     * 
     * @param white Which color to count the moves of.
     * @return The count of moves that the color has completed.
     */
    public int calcMovesPerSide(boolean white) {
        return calcMovesPerSide(white, positions.size() - 1);
    }

    /**
     * Calculates the number of moves each side has completed from the given
     * position.
     * 
     * @param white    Which color to count the moves of.
     * @param position The position to count from.
     * @return The count of moves that the color has completed.
     */
    public int calcMovesPerSide(boolean white, int position) {

        final Position pos = positions.get(position);

        final boolean isTurn = pos.isWhite() == white;
        int moveCount = (int) Math.ceil(pos.getMoveNumber() / 2.0);

        if (isTurn && positions.get(0).isWhite() != white)
            --moveCount;

        return moveCount;

    }

    /**
     * Calculates, with the supplied {@code moveCount}, how many additional seconds
     * should be added to the clock when {@link GameSettings#getTimePerMove()} is
     * greater than {@code 0}.
     * 
     * @param moveCount The amount of moves completed.
     * @return The additional milliseconds that should be added to the timer.
     */
    public long calcTimerDelta(int moveCount) {
        return moveCount * (settings.getTimePerMove() * 1000);
    }

    /**
     * Gets the {@code timerEnd} of the requested color's last completed (or second
     * to last, if the given position has already been completed) turn from the
     * last position.
     * 
     * @param white The color to get the {@code timerEnd} of.
     * @return The previous timer end.
     */
    public long getPrevTimerEnd(boolean white) {
        return getPrevTimerEnd(white, positions.size() - 1);
    }

    /**
     * Gets the {@code timerEnd} of the requested color's last completed (or second
     * to last, if the given position has already been completed) turn from the
     * given position.
     * 
     * @param white    The color to get the {@code timerEnd} of.
     * @param position The position to start searching from.
     * @return The previous timer end.
     */
    public long getPrevTimerEnd(boolean white, int position) {

        final Position pos = positions.get(position);

        final boolean isTurn = pos.isWhite() == white;

        long lastTimerEnd = settings.getTimePerSide() * 1000;

        int index = isTurn ? position - 2 : position - 1;

        if (index >= 0)
            lastTimerEnd = positions.get(index).getTimerEnd();

        return lastTimerEnd;

    }

    /**
     * Gets the current, live time remaining the requested color has at the current
     * position.
     * 
     * @param white Whether or not to get white's timer.
     * @return The remaining time on the timer.
     */
    public long getTimerTime(boolean white) {
        return getTimerTime(white, positions.size() - 1);
    }

    /**
     * Gets the current, live time remaining the requested color has from the given
     * position.
     * 
     * @param white    Whether or not to get white's timer.
     * @param position The position to get the time from.
     * @return The remaining time on the timer.
     */
    public long getTimerTime(boolean white, int position) {

        final Position pos = positions.get(position);

        final boolean isTurn = pos.isWhite() == white;

        long lastTimerEnd = getPrevTimerEnd(white, position);

        final int moveCount = calcMovesPerSide(white, position);

        if (isTurn && pos.getTimerEnd() > -1)
            lastTimerEnd = pos.getTimerEnd();

        lastTimerEnd += calcTimerDelta(Math.max(moveCount, 0));

        if (isTurn && position == positions.size() - 1)
            lastTimerEnd -= getElapsed();

        if (lastTimerEnd < 0)
            return 0;

        return lastTimerEnd;

    }

    /**
     * Starts the timer.
     */
    public void startTimer() {

        timerStart = System.currentTimeMillis();

    }

    /**
     * Stops the current timer and updates the current position's {@code timerEnd}
     * property.
     */
    public void stopTimer() {

        long current = getPrevTimerEnd(getLastPos().isWhite(), positions.size() - 1);

        if (getLastPos().getTimerEnd() > -1)
            current = getLastPos().getTimerEnd();

        getLastPos().setTimerEnd(current - getElapsed());

        timerStart = -1;

    }

    /**
     * Starts the game. Will also mark the game as over after starting if the game
     * already has an outcome.
     */
    public void startGame() {

        if (paused)
            return;

        start = new Date();

        final String res = result.toString();
        final String reas = resultReason.toString();

        result = Result.IN_PROGRESS;
        resultReason = Reason.IN_PROGRESS;

        startTimer();

        if (settings.getTimePerSide() > 0) {

            flagfallChecker = Executors.newScheduledThreadPool(1);
            flagfallChecker.scheduleWithFixedDelay(flagfall, 10, 10, TimeUnit.MILLISECONDS);

        }

        fireEvent(new GameEvent(Type.STARTED));

        checkGameOver();

        if (result == Result.IN_PROGRESS
                && Result.valueOf(res) != Result.IN_PROGRESS
                && Result.valueOf(res) != Result.NOT_STARTED)
            markGameOver(Result.valueOf(res), Reason.valueOf(reas));

    }

    /**
     * Checks if the game is over based on the current position. Checks for
     * checkmate, insufficient material, and stalemate.
     */
    public void checkGameOver() {

        if (getLastPos().isCheckmate())
            markGameOver(getLastPos().isWhite() ? Result.BLACK_WIN : Result.WHITE_WIN, Reason.CHECKMATE);

        else if (getLastPos().isInsufficientMaterial())
            markGameOver(Result.DRAW, Reason.DEAD_INSUFFICIENT_MATERIAL);

        else if (getLastPos().isStalemate())
            markGameOver(Result.DRAW, Reason.STALEMATE);

        else if (getLastPos().getFiftyMoveCounter() >= 100)
            markGameOver(Result.DRAW, Reason.FIFTY_MOVE);

        else {

            String curr = getLastPos().toString().split(" ")[0];

            int sameCount = 1;

            for (int i = 0; i < positions.size() - 1; i++) {

                if (positions.get(i).toString().split(" ")[0].equals(curr))
                    ++sameCount;

            }

            if (sameCount >= 3)
                markGameOver(Result.DRAW, Reason.REPETITION);

        }

    }

    /**
     * Stops the game based on the supplied result and reason.
     * 
     * @param result       The result of the game.
     * @param resultReason The reason for the result of the game.
     */
    public void markGameOver(Result result, Reason resultReason) {

        this.result = result;
        this.resultReason = resultReason;

        if (result == Result.NOT_STARTED || result == Result.IN_PROGRESS)
            return;

        if (flagfallChecker != null)
            flagfallChecker.shutdownNow();

        stopTimer();
        fireEvent(new GameEvent(Type.OVER));

    }

    /**
     * Finds the move with the given {@code origin} and {@code destination} then
     * executes it.
     * 
     * <p>
     * Castle moves should be represented as the king moving to the space of the
     * rook it is castling with.
     * 
     * @param origin      The square the piece being moved originated from.
     * @param destination The destination of the piece being moved.
     * @param promoteType The char code of the piece to promote to. Should be either
     *                    'Q', 'R', 'B', or 'N'. If the move is not a promotion
     *                    move, promoteType should be '0'.
     * @throws Exception If the move is invalid or a move is unable to be made at
     *                   this time (e.g. game is paused.)
     */
    public void makeMove(Square origin, Square destination, char promoteType) throws Exception {

        if (paused)
            throw new Exception("Game is paused.");

        if (result != Game.Result.IN_PROGRESS)
            throw new Exception("Game is not in progress.");

        Move move = getLastPos().findMove(origin, destination);

        if (move == null)
            throw new Exception("Invalid move.");

        if (move.getPromoteType() == '?'
                && (promoteType != 'Q' && promoteType != 'R' && promoteType != 'B' && promoteType != 'N'))
            throw new Exception("Invalid promotion type.");

        // The position after the move is made.
        // biggest delay
        Position movePosition = new Position(getLastPos(), move, promoteType, true);

        if (movePosition.isGivingCheck())
            throw new Exception("Cannot move into check.");

        if (movePosition.getMove().isCapture() && movePosition.getMove().getCapturePiece().getCode() == 'K')
            throw new Exception("Cannot capture a king.");

        stopTimer();

        positions.add(movePosition);

        fireEvent(new GameEvent(
                Type.MOVE,
                positions.size() - 2,
                positions.size() - 1,
                getPreviousPos(),
                getLastPos(),
                move,
                move.isWhite()));

        checkGameOver();

        if (result == Game.Result.IN_PROGRESS)
            startTimer();

    }

    /**
     * Gets if the game can be paused right now.
     * 
     * @return If the game can be paused right now.
     */
    public boolean canPause() {

        return result == Game.Result.IN_PROGRESS && settings.canPause() && !isPaused();

    }

    /**
     * Pauses the game, stopping the clock until {@link #resume()} is called.
     * 
     * @throws Exception If the game is already paused or pausing is not allowed in
     *                   the {@link GameSettings}.
     * 
     * @see #canPause()
     */
    public void pause() throws Exception {

        if (paused)
            throw new Exception("Game is already paused.");

        if (!canPause())
            throw new Exception("Pausing not allowed!");

        paused = true;

        stopTimer();

        fireEvent(new GameEvent(Type.PAUSED));

    }

    /**
     * Gets if the game can be resumed right now.
     * 
     * @return If the game can be resumed right now.
     */
    public boolean canResume() {

        return result == Game.Result.IN_PROGRESS && settings.canPause() && isPaused();

    }

    /**
     * Resumes the game, restarting the clock after it has been stopped.
     * 
     * @throws Exception If the game is not paused or pausing is not allowed in the
     *                   {@link GameSettings}.
     * 
     * @see #canResume()
     */
    public void resume() throws Exception {

        if (!paused)
            throw new Exception("Game is not paused.");

        if (!canResume())
            throw new Exception("Resuming not allowed!");

        paused = false;

        startTimer();

        fireEvent(new GameEvent(Type.RESUMED));

    }

    /**
     * Gets if an undo can occur.
     * 
     * @return If undoing is allowed, and there is a position to undo to.
     */
    public boolean canUndo() {

        return settings.canUndo() && positions.size() > 1;

    }

    /**
     * Undoes the last move. Will also set the timer back to the time before the
     * move was made.
     * 
     * @throws Exception If undoing is not allowed or there is no move to undo.
     * 
     * @see #canUndo()
     */
    public void undo() throws Exception {

        if (!settings.canUndo())
            throw new Exception("Game settings do not allow undo/redo.");

        if (positions.size() <= 1)
            throw new Exception("No move to undo.");

        stopTimer();

        Position redo = getLastPos();

        positions.remove(positions.size() - 1);

        getLastPos().setRedo(redo);
        redo.setRedoPromote(redo.getMove().getPromoteType());

        if (redo.getMove().getPromoteType() != '0')
            redo.setPromote('?');

        redo.setRedoTimerEnd(getLastPos().getTimerEnd());

        if (result != Game.Result.NOT_STARTED && result != Game.Result.IN_PROGRESS) {
            result = Game.Result.IN_PROGRESS;
            resultReason = Game.Reason.IN_PROGRESS;
        }

        fireEvent(new GameEvent(
                Type.MOVE,
                positions.size(),
                positions.size() - 1,
                redo,
                getLastPos(),
                getLastPos().getMove(),
                !getLastPos().isWhite()));

        startTimer();

    }

    /**
     * Gets if a redo can occur.
     * 
     * @return If redoing is allowed, and there is a position to redo to.
     */
    public boolean canRedo() {

        return settings.canUndo() && getLastPos().getRedo() != null;

    }

    /**
     * Redoes the previously undone move. Will also set the timer back to where it
     * was after the redone move was made.
     * 
     * @throws Exception If undoing is not allowed or there is no move to redo.
     * 
     * @see #canRedo()
     */
    public void redo() throws Exception {

        if (!settings.canUndo())
            throw new Exception("Game settings do not allow undo/redo.");

        Position redo = getLastPos().getRedo();

        if (redo == null)
            throw new Exception("No move to redo.");

        stopTimer();

        getLastPos().setTimerEnd(redo.getRedoTimerEnd());

        positions.add(redo);

        if (redo.getRedoPromote() != '0')
            redo.setPromote(redo.getRedoPromote());

        fireEvent(new GameEvent(
                Type.MOVE,
                positions.size() - 2,
                positions.size() - 1,
                getPreviousPos(),
                getLastPos(),
                getLastPos().getMove(),
                getLastPos().getMove().isWhite()));

        startTimer();

    }

    /**
     * Gets if a draw offer can be sent.
     * 
     * @return If a draw offer can be sent, meaning the game is currently in
     *         progress and there is no current offer.
     */
    public boolean canDrawOffer() {

        return result == Result.IN_PROGRESS && drawOfferer == null;

    }

    /**
     * Sends a draw offer.
     * 
     * @param offererWhite If the offerer of the draw is white.
     * @throws Exception If a draw cannot be currently offered.
     * 
     * @see #canDrawOffer()
     */
    public void sendDrawOffer(boolean offererWhite) throws Exception {

        if (!canDrawOffer())
            throw new Exception("Cannot offer a draw.");

        drawOfferer = getPlayer(offererWhite);
        fireEvent(new GameEvent(Type.DRAW_OFFER));

        sendMessage(new Chat(drawOfferer, new Date().getTime(),
                drawOfferer.getName() + " sent a draw offer.", true));

    }

    /**
     * Accepts the current draw offer.
     * 
     * @throws Exception If there is no draw offer to accept, or the game is not in
     *                   progress.
     */
    public void acceptDrawOffer() throws Exception {

        if (result != Game.Result.IN_PROGRESS)
            throw new Exception("Game is not in progress.");

        if (drawOfferer == null)
            throw new Exception("No draw offer.");

        if (canDrawOffer())
            throw new Exception("Cannot accept draw.");

        markGameOver(Game.Result.DRAW,
                (drawOfferer.isWhite())
                        ? Game.Reason.WHITE_OFFERED_DRAW
                        : Game.Reason.BLACK_OFFERED_DRAW);

        final Player accepter = getPlayer(!drawOfferer.isWhite());
        sendMessage(new Chat(accepter, new Date().getTime(), accepter.getName() + " accepted the draw offer."));

        drawOfferer = null;

    }

    /**
     * Declines the current draw offer.
     * 
     * @throws Exception If there is no draw offer to decline, or the game is not in
     *                   progress.
     */
    public void declineDrawOffer() throws Exception {

        if (canDrawOffer())
            throw new Exception("No draw to decline.");

        final Player decliner = getPlayer(!drawOfferer.isWhite());

        fireEvent(new GameEvent(Type.DRAW_DECLINED, decliner.isWhite()));

        sendMessage(new Chat(decliner, new Date().getTime(),
                decliner.getName() + " declined the draw offer.", true));

        drawOfferer = null;

    }

    /**
     * Sends a chat message.
     * 
     * @param message The message to send.
     */
    public void sendMessage(Chat message) {

        messages.add(message);
        fireEvent(new GameEvent(message));

    }

    /**
     * Exports the game to PGN format.
     * 
     * @param includeTags  Whether or not the tags should be included. If
     *                     {@code false},
     *                     just the moves will be listed.
     * @param includeClock Whether or not the clock timestamps should be included
     *                     after each move.
     * @return The game in PGN format.
     * @throws Exception If there was an error exporting the game.
     * 
     * @see game.PGN.PGNParser
     */
    public String exportPosition(boolean includeTags, boolean includeClock) throws Exception {

        Map<String, String> tags = new LinkedHashMap<>();

        // The start date
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        if (start != null)
            tags.put("Date", df.format(start));

        // The player names
        tags.put("White", getPlayer(true).getName());
        tags.put("Black", getPlayer(false).getName());

        // The result of the game.
        switch (result) {
            case DRAW:
                tags.put("Result", "1/2-1/2");
                break;
            case WHITE_WIN:
                tags.put("Result", "1-0");
                break;
            case BLACK_WIN:
                tags.put("Result", "0-1");
                break;
            default:
                tags.put("Result", "*");
        }

        // The time control used
        if (settings.getTimePerSide() <= -1)
            tags.put("TimeControl", "-");
        else
            tags.put("TimeControl",
                    settings.getTimePerSide() + (settings.getTimePerMove() > 0 ? "+" + settings.getTimePerMove() : ""));

        // White & Black's type
        if (white.getType() != null)
            tags.put("WhiteType", white.getType().getString());

        if (black.getType() != null)
            tags.put("BlackType", black.getType().getString());

        // If the starting position is not the default
        if (!settings.getFen().equals(GameSettings.DEFAULT_FEN)) {
            tags.put("SetUp", "1");
            tags.put("FEN", settings.getFen());
        }

        // Opening
        if (getLastPos().getOpening() != null) {

            tags.put("ECO", getLastPos().getOpening().getCode());
            tags.put("Opening", getLastPos().getOpening().getName());

        }

        return new PGNParser(this, tags, includeClock).outputPGN(includeTags);

    }

    /**
     * Registers a class that implements {@link GameListener} to receive
     * {@link GameEvent}s.
     * 
     * @param listener The listener.
     */
    public void addListener(GameListener listener) {

        listeners.add(listener);

    }

    /**
     * Fires an event to all registered listeners.
     * 
     * @param event The event to fire.
     * 
     * @see #addListener(GameListener)
     */
    public void fireEvent(GameEvent event) {

        for (GameListener listener : listeners) {

            listener.onPlayerEvent(event);

        }

    }

    /**
     * Gets the time elapsed of the currently running timer. Will be {@code 0} if
     * the timer has not been started.
     * 
     * @return The time elapsed.
     */
    private long getElapsed() {
        return timerStart >= 0 ? System.currentTimeMillis() - timerStart : 0;
    }

}
