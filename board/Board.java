package board;

/**
 * This class represents the network game board. It is a 8x8 board where
 * the corners are invalid spots, and borders are goals. While the methods
 * in this class are responsible for changing the board's state, we should
 * never call them directly
 */
public class Board {

    public static final int SIZE = 8;

    private int[][] gameBoard;
    private int numBlacks, numWhites; // Keep track of number of tiles each player placed
    private int turn;

    /**
     * Initializes the Board object. The game board is represented
     * as a SIZE x SIZE matrix, where the coordinates (x, y) corresponds
     * to gameBoard[x][y].
     */
    public Board() {
        gameBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                gameBoard[i][j] = Tile.EMPTY;
            }
        }
        turn = Tile.WHITE; // White goes first
        numBlacks = 0;
        numWhites = 0;
    }

    /**
     * Copy constructor.
     */
    public Board(Board b) {
        gameBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                gameBoard[i][j] = b.gameBoard[i][j];
            }
        }
        turn = b.turn;
        numBlacks = b.numBlacks;
        numWhites = b.numWhites;
    }

    /**
     * Returns the tile at (x, y).
     */
    public int getTile(int x, int y) {
        return gameBoard[x][y];
    }

    /**
     * Returns the tile at position p.
     */
    public int getTile(Position p) {
        return getTile(p.x, p.y);
    }

    /**
     * Sets the game board at (x, y) to tile.
     */
    public void setTile(int x, int y, int tile) {
        gameBoard[x][y] = tile;
    }

    /**
     * Sets the game board at position p to tile.
     */
    public void setTile(Position p, int tile) {
        setTile(p.x, p.y, tile);
    }

    /**
     * Returns true if the board at (x, y) is empty.
     */
    public boolean isEmpty(int x, int y) {
        return gameBoard[x][y] == Tile.EMPTY;
    }

    /**
     * Returns true if the board at position p is empty.
     */
    public boolean isEmpty(Position p) {
        return isEmpty(p.x, p.y);
    }

    public static boolean isOnBoard(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    /**
     * Returns 1 if (x,y) is a top (BLACK) or left (WHITE) goal.
     * Returns -1 if (x,y) is a bottom (BLACK) or right (WHITE) goal.
     * Returns 100 if (x,y) is not a goal.
     */
    public static int goalValue(int x, int y) {
        if (x == 0 || y == 0) {
            return 1;
        }
        if (x == Board.SIZE - 1 || y == Board.SIZE - 1) {
            return -1;
        }
        return 100;
    }

    public static int goalValue(Position p) {
        return goalValue(p.x, p.y);
    }

    /**
     * Goal values are either -1 or 1. (100 if not a goal)
     */
    public static boolean isOppositeGoal(Position p1, Position p2) {
        return p1.goal * -1 == p2.goal;
    }

    /**
     * Goal values are either -1 or 1. (100 if not a goal)
     */
    public static boolean isSameGoal(Position p1, Position p2) {
        return p1.goal == p2.goal && p1.goal != 100;
    }

    public static boolean isGoal(Position p) {
        return p.goal != 100;
    }

    public static boolean isCenterGoal(Position p) {
        if ((p.x == 0 || p.y == Board.SIZE - 1) && (p.y == 3 || p.y == 4)) {
            return true;
        }
        if ((p.y == 0 || p.y == Board.SIZE - 1) && (p.x == 3 || p.x == 4)) {
            return true;
        }
        return false;
    }

    /**
     * Increments number of white tiles registered on the board.
     */
    public void incrementWhite() {
        numWhites = numWhites + 1;
    }

    /**
     * Decrements number of white tiles registered on the board.
     */
    public void decrementWhite() {
        numWhites = numWhites - 1;
    }

    /**
     * Increments number of black tiles registered on the board.
     */
    public void incrementBlack() {
        numBlacks = numBlacks + 1;
    }

    /**
     * Decrements number of black tiles registered on the board
     */
    public void decrementBlack() {
        numBlacks = numBlacks - 1;
    }

    /**
     * Returns the current player's turn (0 for BLACK, 1 for WHITE)
     */
    public int getTurn() {
        return turn;
    }

    /**
     * Returns the other player's turn
     */
    public int getOther() {
        if (turn == Tile.BLACK) {
            return Tile.WHITE;
        }
        return Tile.BLACK;
    }

    /**
     * Returns the color of the opponent of the specified color.
     */
    public static int getOtherColor(int color) {
        if (color == Tile.BLACK) {
            return Tile.WHITE;
        }
        return Tile.BLACK;
    }

    /**
     * Returns the number of tiles the current player has on the board.
     */
    public int getCurrentPlayerTileCount() {
        if (turn == Tile.BLACK) {
            return numBlacks;
        }
        else return numWhites;
    }

    /**
     * Returns the number of tiles the player not moving has on the board.
     */
    public int getOtherPlayerTileCount() {
        if (turn == Tile.BLACK) {
            return numWhites;
        }
        else return numBlacks;
    }


    /**
     * Changes turns, ensures only the appropriate player can make a move.
     */
    public void endTurn() {
        if (turn == Tile.BLACK) {
            turn = Tile.WHITE;
        }
        else {
            turn = Tile.BLACK;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int tile = gameBoard[x][y];
                if (tile == Tile.BLACK) {
                    hash(hash, "B" + x + y);
                }
                if (tile == Tile.WHITE) {
                    hash(hash, "W" + x + y);
                }
            }
        }
        return hash;
    }

    private static int hash(int hash, String s) {
        for (int i = 0; i < s.length(); i++) {
            hash = (127 * hash + s.charAt(i)) % 16908799;
        }
        return hash;
    }

    public void printBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 1 2 3 4 5 6 7\n");
        int count = 0;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                switch (gameBoard[x][y]) {
                    case Tile.BLACK:
                        sb.append("B ");
                        break;
                    case Tile.WHITE:
                        sb.append("W ");
                        break;
                    case Tile.EMPTY:
                        sb.append("E");
                        break;
                }
            }
            sb.append(count);
            sb.append("\n");
            count = count + 1;
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
    }

}
