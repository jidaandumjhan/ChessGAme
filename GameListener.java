package game;

/**
 * Interface for listening to {@link GameEvent}s.
 */
@FunctionalInterface
public interface GameListener {

    /**
     * Called when an event pertaining to the game happens.
     * 
     * @param event The event that was fired.
     */
    public void onPlayerEvent(GameEvent event);

}
