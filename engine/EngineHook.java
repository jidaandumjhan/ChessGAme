package game.engine;

import java.util.ArrayList;

import game.Chat;
import game.Game;
import game.GameEvent;
import game.GameListener;
import game.Move;
import game.Square;

/**
 * Class used to bridge the engine and the game.
 */
public class EngineHook implements GameListener {

    /**
     * The game this engine is being used in.
     */
    private final Game game;

    /**
     * The engine being used.
     */
    private final UCIEngine engine;

    /**
     * Whether or not the engine is playing the white side.
     */
    private final boolean white;

    /**
     * The depth the engine should compute for each move.
     */
    private int depth;

    /**
     * Whether or not the engine should tell the opponent what it thinks their best
     * move would have been in the previous position.
     */
    private boolean bestMove;

    /**
     * The depth the engine should compute when searching for the opponents best
     * move in the previous position.
     * 
     * @see #bestMove
     */
    private int bestMoveDepth;

    /**
     * Creates a new engine hook.
     * 
     * @param engine The engine associated with this hook.
     * @param game   The game associated with this hook.
     * @param white  Whether or not the engine is playing the white side.
     */
    public EngineHook(UCIEngine engine, Game game, boolean white) {

        this.game = game;
        this.engine = engine;
        this.white = white;

        bestMove = true;
        bestMoveDepth = 15;
        depth = 10;

        game.addListener(this);

    }

    // Handles the game events and sends them to the engine.
    @Override
    public void onPlayerEvent(GameEvent event) {

        switch (event.getType()) {
            case DRAW_DECLINED:
                break;
            case DRAW_OFFER:
                break;
            case IMPORTED:
                break;
            case MESSAGE:
                break;
            case MOVE:

                if (event.getCurrIndex() < event.getPrevIndex()) {

                    if (!(event.getCurrIndex() == 0 && event.getCurr().isWhite() == white)) {
                        return;
                    }

                }

                if (event.getCurr().isWhite() != white && bestMove) {
                    try {
                        ArrayList<String> moveList = new ArrayList<>();

                        for (int i = 1; i < game.getPositions().size() - 2; i++) {
                            moveList.add(game.getPositions().get(i).getMove().toString());
                        }

                        String[] arr = new String[moveList.size()];
                        engine.setPosition(game.getPositions().get(0).toString(), moveList.toArray(arr));

                        String bm = engine.getBestMove(bestMoveDepth, game.getTimerTime(true), game.getTimerTime(false),
                                game.getSettings().getTimePerMove() * 1000, game.getSettings().getTimePerMove() * 1000);

                        Square origin = new Square(bm.substring(0, 2));
                        Square destination = new Square(bm.substring(2, 4));

                        char promoteType = bm.substring(4).equals("") ? '0'
                                : Character.toUpperCase(bm.substring(4).charAt(0));

                        if (game.getPositions().size() - 3 < 0)
                            return;

                        Move m = game.getPositions().get(game.getPositions().size() - 3).findMove(origin, destination);

                        if (m == null || !bestMove)
                            return;

                        String mn = m.getMoveNotation();

                        if (promoteType != '0')
                            mn = mn.substring(0, mn.length() - 1) + promoteType;

                        game.sendMessage(new Chat(game.getPlayer(white),
                                System.currentTimeMillis(),
                                (game.getPositions().size() > 2
                                        && bm.startsWith(
                                                game.getPositions().get(game.getPositions().size() - 2).getMove()
                                                        .toString()))
                                                                ? "That was the best move."
                                                                : ("Best move was: " + mn)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event.getCurr().isWhite() == white)
                    makeMove(event);

                break;
            case OVER:
                break;
            case PAUSED:
                break;
            case RESUMED:
                break;
            case STARTED:

                if (white)
                    makeMove(event);

                break;
            default:
                break;
        }

    }

    /**
     * Takes the move made in the game and outputs it to the engine, then waits for
     * the engine to make it's move.
     * 
     * @param event The event that was fired because of the move made.
     */
    private void makeMove(GameEvent event) {

        Thread t = new Thread(() -> {

            try {

                Thread.sleep(250);

                ArrayList<String> moveList = new ArrayList<>();

                for (int i = 1; i < game.getPositions().size(); i++) {
                    moveList.add(game.getPositions().get(i).getMove().toString());
                }

                String[] arr = new String[moveList.size()];
                engine.setPosition(game.getPositions().get(0).toString(), moveList.toArray(arr));

                String bm = engine.getBestMove(depth, game.getTimerTime(true), game.getTimerTime(false),
                        game.getSettings().getTimePerMove() * 1000, game.getSettings().getTimePerMove() * 1000);

                Square origin = new Square(bm.substring(0, 2));
                Square destination = new Square(bm.substring(2, 4));
                char promoteType = bm.substring(4).equals("") ? '0' : Character.toUpperCase(bm.substring(4).charAt(0));

                System.out.println(bm);
                engine.waitReady();

                game.makeMove(origin, destination, promoteType);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        t.start();
    }

    /**
     * Gets the game.
     * 
     * @return {@link #game}
     */
    public Game getGame() {
        return game;
    }

    /**
     * Gets the engine.
     * 
     * @return {@link #engine}
     */
    public UCIEngine getEngine() {
        return engine;
    }

    /**
     * Gets if the engine is playing white.
     * 
     * @return {@link #white}
     */
    public boolean isWhite() {
        return white;
    }

    /**
     * Gets the depth.
     * 
     * @return {@link #depth}
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth.
     * 
     * @param depth The depth the engine will search for its own move.
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Gets whether or not the engine will analyze the opponents best move.
     * 
     * @return {@link #bestMove}
     */
    public boolean isBestMove() {
        return bestMove;
    }

    /**
     * Sets whether or not the engine will analyze the opponents best move.
     * 
     * @param bestMove If the best move should be analyzed.
     * @see #bestMove
     */
    public void setBestMove(boolean bestMove) {
        this.bestMove = bestMove;
    }

    /**
     * Gets the depth the engine will search for the opponent's best move.
     * 
     * @return {@link #bestMoveDepth}
     */
    public int getBestMoveDepth() {
        return bestMoveDepth;
    }

    /**
     * Sets the depth the engine will search for the opponent's best move.
     * 
     * @param bestMoveDepth The depth.
     * @see #bestMoveDepth
     */
    public void setBestMoveDepth(int bestMoveDepth) {
        this.bestMoveDepth = bestMoveDepth;
    }

}
