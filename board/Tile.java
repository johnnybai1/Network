package board;



public final class Tile {

    public static final int EMPTY = -1;
    public static final int BLACK = 0;
    public static final int WHITE = 1;

    private Tile() {
    }

    public static int getOtherColor(int color) {
        if (color == Tile.WHITE) {
            return Tile.BLACK;
        }
        return Tile.WHITE;
    }

}
