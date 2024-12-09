package game.engine;

import java.io.IOException;

/**
 * A UCI option that can be pressed.
 * 
 * @see UCIOption
 */
public class UCIButton extends UCIOption<Void> {

    /**
     * Creates a UCI button.
     * 
     * @param engine The engine.
     * @param name   The name of the option.
     */
    public UCIButton(UCIEngine engine, String name) {
        this.engine = engine;
        this.name = name;
    }

    public void set(Void value) throws IOException {
        engine.setOption(name, "");
        this.value = value;

    }

    public Void get() throws IOException {
        return value;
    }

}
