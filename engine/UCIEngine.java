package game.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Interfaces with a UCI-compatible engine following the standard found here:
 * <a href=
 * "https://backscattering.de/chess/uci/">https://backscattering.de/chess/uci/
 * </a>
 */
public class UCIEngine {

    /**
     * The name of the engine.
     */
    private String name;

    /**
     * The author of the engine.
     */
    private String author;

    /**
     * The buffer of data received from the engine.
     */
    protected BufferedReader input;

    /**
     * The buffer of data that is sent to the engine.
     */
    protected BufferedWriter output;

    /**
     * The options associated with the engine.
     */
    protected ArrayList<UCIOption<?>> opts;

    /**
     * Finds and initializes the engine at the given path.
     * 
     * @param enginePath The location of the engine.
     * @throws IOException If there is an error outputting to the engine.
     */
    public UCIEngine(File enginePath) throws IOException {

        opts = new ArrayList<>();

        ProcessBuilder pb = null;

        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            pb = new ProcessBuilder("cmd", "/c", enginePath.getName());
        } else {
            pb = new ProcessBuilder("./" + enginePath.getName());
        }

        pb.directory(new File(enginePath.getParent()));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        input = p.inputReader();
        output = p.outputWriter();

        String rec = input.readLine();
        output.write("uci\n");
        output.flush();

        while (!rec.equals("uciok")) {
            System.out.println(rec);

            if (rec.startsWith("id name ")) {
                this.name = rec.substring(8);
            } else if (rec.startsWith("id author ")) {
                this.author = rec.substring(10);
            } else if (rec.startsWith("option name ")) {

                String[] a = rec.split(" ");

                String n = a[2];
                int i = 0;
                for (i = 3; i < a.length; i++) {

                    if (a[i].equals("type"))
                        break;

                    n += " " + a[i];

                }
                ++i;

                switch (a[i]) {
                    case "check":
                        boolean def = a[i + 2].equals("true");
                        opts.add(new UCICheck(this, n, def, def));
                        break;
                    case "spin":
                        opts.add(new UCISpin(this, n, Integer.parseInt(a[i + 2]), Integer.parseInt(a[i + 2]),
                                Integer.parseInt(a[i + 4]), Integer.parseInt(a[i + 6])));
                        break;
                    case "combo":
                        String cdef = i + 2 >= a.length ? "" : a[i + 2];

                        ArrayList<String> carr = new ArrayList<>();
                        carr.add("");
                        i += 4;
                        for (int z = 0; i < a.length; i++) {
                            if (a[i].equals("var")) {
                                ++z;
                                continue;
                            }
                            if (carr.size() - 1 == z)
                                carr.set(z, carr.get(z) + " " + a[i]);
                            else
                                carr.add(a[i]);

                        }
                        opts.add(new UCICombo(this, n, cdef, cdef, carr.toArray(new String[carr.size()])));
                        break;
                    case "button":
                        opts.add(new UCIButton(this, n));
                        break;
                    case "string":
                        String sdef = i + 2 >= a.length ? "" : a[i + 2];
                        opts.add(new UCIString(this, n, sdef, sdef));
                        break;

                }

            }

            rec = input.readLine();
        }

        for (int i = 0; i < opts.size(); i++) {

            UCIOption<?> o = opts.get(i);
            if (o.getName().equals("UCI_Chess960") && o instanceof UCICheck) {
                UCICheck c = (UCICheck) o;
                c.set(true);
                break;
            }

        }

        println("ucinewgame");
        waitReady();

    }

    /**
     * Gets the options.
     * 
     * @return {@link #opts}
     */
    public ArrayList<UCIOption<?>> getOpts() {
        return opts;
    }

    /**
     * Gets the name.
     * 
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name The name.
     * @see #name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the author.
     * 
     * @return {@link #author}
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     * 
     * @param author The author.
     * @see #author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Waits for the engine to be ready. This method is blocking. Used to confirm an
     * engine is ready after performing an operation.
     * 
     * @throws IOException If there is an error outputting to the engine.
     */
    public void waitReady() throws IOException {

        println("isready");

        String rec = input.readLine();
        while (!rec.equals("readyok")) {
            System.out.println(rec);
            rec = input.readLine();
        }
        System.out.println(rec);

    }

    /**
     * Tells the engine the current position of the game.
     * 
     * @param fen   The starting position in FEN notation.
     * @param moves The moves that led up to the current position.
     * @throws IOException If there is an error outputting to the engine.
     */
    public void setPosition(String fen, String... moves) throws IOException {

        println("position fen " + fen + " moves " + String.join(" ", moves));

    }

    /**
     * Gets the best move from the currently set position.
     * 
     * @param depth The depth to search for.
     * @param wtime The amount of time white has left.
     * @param btime The amount of time black has left.
     * @param winc  The amount of time white gains per move.
     * @param binc  The amount of time black gains per move.
     * @return The best move in long algebraic notation.
     * @throws IOException If there is an error outputting to the engine or
     *                     receiving its response.
     * 
     * @see #setPosition(String, String...)
     */
    public String getBestMove(int depth, long wtime, long btime, long winc, long binc) throws IOException {

        String d = " depth " + depth;
        if (depth <= 0)
            d = "";

        String wt = " wtime " + wtime;
        if (wtime <= 0)
            wt = "";

        String bt = " btime " + btime;
        if (btime <= 0)
            bt = "";

        String wi = " winc " + winc;
        if (winc <= 0)
            wi = "";

        String bi = " binc " + binc;
        if (binc <= 0)
            bi = "";

        println("go" + d + wt + bt + wi + bi);

        String rec = input.readLine();
        while (!rec.startsWith("bestmove ")) {
            System.out.println(rec);
            rec = input.readLine();
        }

        System.out.println(rec);
        String[] sp = rec.split(" ");
        return sp[1];

    }

    /**
     * Sets an option of the engine to the given value.
     * 
     * @param name  The name of the option to set.
     * @param value The value to set the option to.
     * @throws IOException If there is an error outputting to the engine.
     * @see #waitReady()
     */
    public void setOption(String name, String value) throws IOException {

        println("setoption name " + name + " value " + value);

    }

    /**
     * Outputs a message to the engine.
     * 
     * @param command The command to output to the engine.
     * @throws IOException If there is an error outputting to the engine.
     */
    private void println(String command) throws IOException {
        System.out.println("Outputting: " + command);
        output.write(command + "\n");
        output.flush();
    }

}
