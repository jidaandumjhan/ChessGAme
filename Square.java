package game;

/**
 * A representation of a square on a chess board.
 */
public class Square {

    /** The file of the {@link Square}. */
    private int file;

    /** The rank of the {@link Square}. */
    private int rank;

    /**
     * Creates a new {@link Square} object.
     * 
     * @param file The file (column).
     * @param rank The rank (row).
     */
    public Square(int file, int rank) {

        this.file = file;
        this.rank = rank;

    }

    /**
     * Creates a {@link Square} object from a string representation of the square.
     * 
     * <p>
     * Square examples: a1, h8, e4, f7, etc.
     * 
     * @param square The square as a string.
     * @throws Exception If {@code square} is in an invalid format.
     */
    public Square(String square) throws Exception {

        square = square.toLowerCase().trim();

        if (!square.matches("[a-h][1-8]"))
            throw new Exception("Square format invalid.");

        this.file = (int) (square.toLowerCase().charAt(0)) - 96;
        this.rank = (int) (square.toLowerCase().charAt(1)) - 48;

        if (!isValid())
            throw new Exception("Invalid square.");

    }

    /**
     * Gets the file (column) of the square.
     * 
     * @return The file.
     */
    public int getFile() {
        return file;
    }

    /**
     * Sets the file (column) of the square.
     * 
     * @param file The numeric file to set the square to.
     */
    public void setFile(int file) {
        this.file = file;
    }

    /**
     * Gets the rank (row) of the square.
     * 
     * @return The rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Sets the rank (row) of the square.
     * 
     * @param rank The numeric rank to set the square to.
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Gets if the square is a valid board square (rank and file within one and
     * eight inclusive.)
     * 
     * @return If square is a valid board square.
     */
    public boolean isValid() {

        return file >= 1 && file <= 8 && rank >= 1 && rank <= 8;

    }

    /**
     * Gets the if the square is a light square.
     * 
     * @return If the square is a light square.
     */
    public boolean isLightSquare() {

        return ((rank % 2 == 0) && (file % 2 != 0)
                ||
                (rank % 2 != 0) && (file % 2 == 0));

    }

    /**
     * A string representation of the square, with the file as a lowercase
     * letter and rank as the number.
     * 
     * <p>
     * Ex: "h4"
     */
    @Override
    public String toString() {

        return "" + (char) (96 + file) + rank;

    }

    /**
     * Compares two Square objects.
     * 
     * @param compare The square to compare to.
     * @return Whether or not the file and rank match.
     */
    @Override
    public boolean equals(Object compare) {

        if (!(compare instanceof Square))
            return false;

        Square casted = (Square) (compare);

        return (file == casted.getFile()) && (rank == casted.getRank());

    }

}
