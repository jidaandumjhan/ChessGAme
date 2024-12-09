package game.engine;

import java.io.IOException;

/**
 * A UCI option that can be a string.
 * 
 * @see UCIOption
 */
public class UCIString extends UCIOption<String> {
    
    /**
     * Creates a new UCI string option.
     * 
     * @param engine The engine.
     * @param name   The name of the option.
     * @param def    The default value of the string.
     * @param value  The current value of the string.
     */
    public UCIString(UCIEngine engine, String name, String def, String value) {
        this.engine = engine;
        this.name = name;
        this.def = def;
        this.value = value;
    }

    public void set(String value) throws IOException {
        engine.setOption(name, value);
        this.value = value;

    }

    public String get() throws IOException {
        return value;
    }

}
