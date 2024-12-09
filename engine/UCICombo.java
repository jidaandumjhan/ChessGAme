package game.engine;

import java.io.IOException;

/**
 * A UCI option that can be a list of predefined options, like a dropdown menu
 * (combo box.)
 * 
 * @see UCIOption
 */
public class UCICombo extends UCIOption<String> {

    /**
     * The variables (options) of this combo box.
     */
    protected String[] vars;

    /**
     * Gets the predefined options.
     * 
     * @return {@link #vars}
     */
    public String[] getVars() {
        return vars;
    }

    /**
     * Sets the variables.
     * 
     * @param vars A list of the variables.
     * @see #vars
     */
    public void setVars(String[] vars) {
        this.vars = vars;
    }

    /**
     * Creates a new UCI combo option.
     * 
     * @param engine The engine.
     * @param name   The name of the option.
     * @param def    The default value of the option.
     * @param value  The current value of the option.
     * @param vars   The predetermined variables associated with this option.
     */
    public UCICombo(UCIEngine engine, String name, String def, String value, String... vars) {
        this.engine = engine;
        this.name = name;
        this.def = def;
        this.value = value;
        this.vars = vars;

    }

    public void set(String value) throws IOException {
        engine.setOption(name, value);
        this.value = value;

    }

    public String get() throws IOException {
        return value;
    }

}
