package game.engine;

import java.io.IOException;

/**
 * A UCI option that can be true or false, like a checkbox.
 * @see UCIOption
 */
public class UCICheck extends UCIOption<Boolean> {

    /**
     * Creates a new UCI checkbox.
     * 
     * @param engine The engine.
     * @param name   The name of the option.
     * @param def    The default value of the option.
     * @param value  The current value of the option.
     */
    public UCICheck(UCIEngine engine, String name, boolean def, boolean value) {
        this.engine = engine;
        this.name = name;
        this.def = def;
        this.value = value;
    }

    public void set(Boolean value) throws IOException {
        engine.setOption(name, value ? "true" : "false");
        this.value = value;

    }

    public Boolean get() throws IOException {
        return value;
    }

}
