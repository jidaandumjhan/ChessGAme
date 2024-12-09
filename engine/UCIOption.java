package game.engine;

/**
 * A generic option for a UCI engine.
 * 
 * @param <E> The type of data that is stored by this option.
 */
public abstract class UCIOption<E> {

    /**
     * The engine associated with this option.
     */
    protected UCIEngine engine;

    /**
     * The name of the option.
     */
    protected String name;

    /**
     * The current value of the option.
     */
    protected E value;

    /**
     * The default value of the option.
     */
    protected E def;

    /**
     * Gets the engine.
     * 
     * @return {@link #engine}
     */
    public UCIEngine getEngine() {
        return engine;
    }

    /**
     * Gets the name of the option.
     * 
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this option.
     * 
     * @param name The name.
     * @see #name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value.
     * 
     * @return {@link #value}
     */
    public E getValue() {
        return value;
    }

    /**
     * Sets the value.
     * 
     * @param value The value.
     * @see #value
     */
    public void setValue(E value) {
        this.value = value;
    }

    /**
     * Gets the default.
     * 
     * @return {@link #def}
     */
    public E getDef() {
        return def;
    }

    /**
     * Sets the default.
     * 
     * @param def The default.
     * @see #def
     */
    public void setDef(E def) {
        this.def = def;
    }

    /**
     * Sets the value of the option.
     * 
     * @param value The value to set the option to.
     * @throws Exception If there is an error setting the option with the engine.
     */
    abstract public void set(E value) throws Exception;

    /**
     * Gets the value of the option.
     * 
     * @return The value of the option.
     * @throws Exception If there is an error getting the value from the engine.
     */
    abstract public E get() throws Exception;

}
