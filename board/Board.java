package board;

import player.Move;

import java.io.*;
import java.util.*;

/**
 * This class represents the state of the network game board. It is a 8x8 board
 * where the corners are invalid spots, and the borders are goals. The methods
 * in this class are responsible for changing the board's state.
 */
public class Board {

    public static LinkedList<Integer> stateSeq = new LinkedList<>();
    public static HashMap<Integer, Double> stateWeights;


    public static final int SIZE = 8;
    public static final int GOAL1 = 1; // Left (WHITE) or Top (BLACK) goal
    public static final int GOAL2 = 2; // Right (WHITE) or Bottom (BLACK) goal
    public static final int MAX = 10000; // Score received for winning
    public static final int MIN = -10000; // Score received for losing

    private int[][] board;
    private int numBlacks, numWhites; // Keep track of number of tiles each player placed
    private int turn;

    /**
     * Initializes the Board object. The game board is represented
     * as a SIZE x SIZE matrix, where the coordinates (x, y) corresponds
     * to gameBoard[x][y].
     */
    public Board() {
        board = new int[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = Tile.EMPTY;
            }
        }
        turn = Tile.WHITE; // White goes first
        numBlacks = 0;
        numWhites = 0;
        loadWeights();
    }

    // Copy constructor
    public Board(Board b) {
        board = new int[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = b.board[x][y];
            }
        }
        turn = b.turn;
        numBlacks = b.numBlacks;
        numWhites = b.numWhites;
    }

    // Copies the board and then executes the Move m
    public Board(Board b, Move m) {
        this(b);
        executeMove(m);
    }

    /*********************************************************************
     * BELOW ARE METHODS TO OBTAIN INFORMATION REGARDING THE STATE OF    *
     * BOARD                                                             *
     *********************************************************************/

    /**
     * Returns the tile at (x, y).
     */
    public int tileAt(int x, int y) {
        return board[x][y];
    }

    /**
     * Returns the tile at position p.
     */
    public int tileAt(Position p) {
        return tileAt(p.x, p.y);
    }

    /**
     * Returns the number of tiles the current player has on the board.
     */
    public int getCurrentPlayerTileCount() {
        if (turn == Tile.WHITE) {
            return numWhites;
        }
        else return numBlacks;
    }

    public int getTileCount(int color) {
        if (color == Tile.WHITE) {
            return numWhites;
        }
        return numBlacks;
    }

    /**
     * Converts (x,y) to the integer representation of a goal, if it lies in a
     * goal region
     */
    public static int toGoal(int x, int y) {
        if (x == 0 || y == 0) {
            return GOAL1;
        }
        if (x == SIZE - 1 || y == SIZE - 1) {
            return GOAL2;
        }
        return 0;
    }

    public List<Position> getTiles(int color) {
        List<Position> tiles = new ArrayList<>(10);
        for (int x = 0; x < SIZE; x++ ){
            for (int y = 0; y < SIZE; y++) {
                if (isEmpty(x, y)) {
                    continue;
                }
                if (tileAt(x, y) == color) {
                    tiles.add(new Position(x, y));
                }
            }
        }
        return tiles;
    }

    public List<Board> getSuccessors() {
        List<Board> successors = new ArrayList<>();
        for (Move m : getValidMoves()) {
            successors.add(new Board(this, m));
        }
        return successors;
    }

    /**
     * Returns a list of valid moves for the current player.
     */
    public List<Move> getValidMoves() {
        List<Move> moves = new ArrayList<>(64);
        if (getCurrentPlayerTileCount() < 10) {
            // ADD MOVES
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if (isValidTilePlacement(x, y, turn)) {
                        moves.add(new Move(x, y));
                    }
                }
            }
        }
        else {
            // STEP MOVES
            for (int x2 = 0; x2 < SIZE; x2++) {
                for (int y2 = 0; y2 < SIZE; y2++) {
                    if (tileAt(x2, y2) == turn) {
                        removeTile(x2, y2);
                        for (int x1 = 0; x1 < SIZE; x1++) {
                            for (int y1 = 0; y1 < SIZE; y1++) {
                                if (x1 == x2 && y1 == y2) {
                                    continue;
                                }
                                if (isValidTilePlacement(x1, y1, turn)) {
                                    moves.add(new Move(x1, y1, x2, y2));
                                }
                            }
                        }
                        setTile(x2, y2, turn);
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Parses through a player's longest chain and returns a score based off
     * of it's characteristics.
     * 1. Does it start in a goal? Is it at a preferable region in the goal?
     * 2. Are there a lot of big gaps? If so, penalize it
     * 3. Is it a long network? Do not intentionally give bonus points to
     * longer chains, limit the score obtained from length to 6.
     */
    private int scoreChain(LinkedList<Position> chain) {
        int score = 0;
        int size = chain.size();
        if (size > 4) {
            Position p1 = chain.getFirst();
            Position p2 = chain.getLast();
            if (!p1.equals(p2) && Position.isOppositeGoal(p1, p2)) {
                score = 5;
            }
        }
        if (size > 6) {
            score = score + 18; // Maximum 18 points for length
        }
        else score = score + 3 * size;
        // Penalize gaps
        if (size > 3) {
            for (Position p : chain) {
                score = score - p.space;
            }
        }
        return score;
    }

    public int scoreTiles(List<Position> tiles, int tileCount) {
        int score = 0;
        int numPairs = 0;
        int goal1Count = 0;
        int goal2Count = 0;
        for (Position p : tiles) {
            numPairs = numPairs + NetworkFinder.getConnected(this, p).size();
            if (p.goal == GOAL1) {
                goal1Count ++;
                if (Position.isCentral(p)) {
                    score = score + 1;
                }
            }
            if (p.goal == GOAL2) {
                goal2Count ++;
                if (Position.isCentral(p)) {
                    score = score + 1;
                }
            }
        }
        score = score + numPairs / 2;
        if (tileCount < 3) {
            if ((goal1Count == 1 && goal2Count == 0) ||
                    (goal1Count == 0 && goal2Count == 1)) {
                score = score + 2;
            }
            if (goal1Count > 1 || goal2Count > 1) {
                score = score - 2;
            }
        }
        if (tileCount > 4) {
            if ((goal1Count > 0 && goal2Count > 0)) {
                score = score + 2;
            }
        }
        if (score > 0) {
            return score;
        }
        return 0;
    }

    /**
     * Evaluates the game board from the specified player's perspective. A
     * positive value for the player means the player is winning, while a
     * negative value means the player is losing. Calling evaluate(player)
     * should return the same value with the opposite sign as evaluate(other).
     */
    public int evaluate(int player) {
        NetworkFinder.SearchNode self =
                NetworkFinder.getChain(this, player);
        int opp = Tile.getOtherColor(player);
        NetworkFinder.SearchNode other =
                NetworkFinder.getChain(this, opp);
        // Check if someone won
        if (self.isNetwork && other.isNetwork) {
            // Both players wound up with networks, player who did not make the
            // move, wins
            if (turn == player) {
                // If other player moved, we win
                return MAX;
            }
            return MIN;
        }
        if (self.isNetwork) {
            return MAX;
        }
        if (other.isNetwork) {
            return MIN;
        }
        List<Position> selfTiles = getTiles(player);
        List<Position> otherTiles = getTiles(opp);
        // Score the current board from player's perspective
        return scoreChain(self.chain) + scoreTiles(selfTiles, getTileCount(player))
                - scoreChain(other.chain) - scoreTiles(otherTiles, getTileCount(opp));
    }

    public int evaluateWithWeight(int player) {
        return (int) (stateWeights.getOrDefault(board.hashCode(), 1.0) *
                evaluate(player));
    }

    public int turn() {
        return turn;
    }

    public int other() {
        return Tile.getOtherColor(turn);
    }


    /*********************************************************************
     * BELOW ARE METHODS TO MODIFY THE GAME BOARD                        *
     *********************************************************************/

    /**
     * Adds a Tile to the board at (x,y).
     */
    public void setTile(int x, int y, int color) {
        board[x][y] = color;
    }

    /**
     * Adds a Tile to the board at Position p
     */
    public void setTile(Position p, int color) {
        setTile(p.x, p.y, color);
    }

    /**
     * Removes and returns the tile on the board at (x,y)
     */
    public int removeTile(int x, int y) {
        int tile = board[x][y];
        board[x][y] = Tile.EMPTY;
        return tile;
    }

    /**
     * Removes and returns the tile on the board at Position p
     */
    public int removeTile(Position p) {
        return removeTile(p.x, p.y);
    }

    /**
     * Changes turns, ensures only the appropriate player can make a move.
     */
    public void endTurn() {
        if (turn == Tile.WHITE) {
            turn = Tile.BLACK;
        }
        else {
            turn = Tile.WHITE;
        }
    }

    /**
     * If the Move is valid, execute and return true, otherwise return false.
     */
    public boolean executeMove(Move m) {
        return (m.moveKind == Move.ADD && executeAddMove(m)) ||
                (m.moveKind == Move.STEP && executeStepMove(m));
    }

    /**
     * Parses the add Move m and modifies the board appropriately. Returns true
     * if the move is executed.
     */
    private boolean executeAddMove(Move m) {
        if (isValidAddMove(m)) {
            board[m.x1][m.y1] = turn;
            if (turn == Tile.WHITE) {
                numWhites ++;
            }
            else numBlacks ++;
            endTurn();
            return true;
        }
        return false;
    }

    /**
     * Parses the step Move m and modifies the board appropriately. Returns true
     * if the move is executed.
     */
    private boolean executeStepMove(Move m) {
        if (isValidStepMove(m)) {
            removeTile(m.x2, m.y2);
            board[m.x1][m.y1] = turn;
            endTurn();
            return true;
        }
        return false;
    }

    /**
     * Undo a move, for game tree search.
     */
    public void undoMove(Move m) {
        endTurn();
        if (m.moveKind == Move.ADD) {
            setTile(m.x1, m.y1, Tile.EMPTY);
            if (turn == Tile.WHITE) {
                numWhites --;
            }
            else numBlacks --;
        }
        if (m.moveKind == Move.STEP) {
            setTile(m.x1, m.y1, Tile.EMPTY);
            setTile(m.x2, m.y2, turn);
        }
    }

    /*********************************************************************
     * BELOW ARE THE METHODS PERTAINING TO THE RULES OF THE GAME         *
     *********************************************************************/

    /**
     * Returns true if the Move m is a valid move.
     */
    public boolean isValidMove(Move m) {
        return (m.moveKind == Move.ADD && isValidAddMove(m)) ||
                (m.moveKind == Move.STEP && isValidStepMove(m));
    }

    /**
     * Returns true if the ADD Move m can be applied to the Board b.
     * (m.x1,m.y1) must be a valid place for b.getTurn() to place a tile.
     */
    public boolean isValidAddMove(Move m) {
        if (getCurrentPlayerTileCount() == 10) {
            return false;
        }
        return isValidTilePlacement(m.x1, m.y1, turn);
    }

    /**
     * Returns true if the STEP Move m can be applied to the Board b. After
     * (m.x2,m.y2) is removed from the board, (m.x1,m.y1) must be a valid place
     * for b.getTurn() to place a tile.
     */
    public boolean isValidStepMove(Move m) {
        if (getCurrentPlayerTileCount() < 10) {
            return false;
        }
        if (m.x1 == m.x2 && m.y1 == m.y2) {
            return false;
        }
        if (tileAt(m.x2, m.y2) != turn) {
            return false;
        }
        int tile = removeTile(m.x2, m.y2);
        boolean valid = isValidTilePlacement(m.x1, m.y1, turn);
        setTile(m.x2, m.y2, tile);
        return valid;
    }

    /**
     * (x,y) is a valid tile placement for a tile of some color on Board b if
     * it satisfies all four rules.
     */
    public boolean isValidTilePlacement(int x, int y, int color) {
        if (isCorner(x, y)) {
            return false;
        }
        if (isOpponentGoal(x, y, color)) {
            return false;
        }
        if (!isEmpty(x, y)) {
            return false;
        }
        if (formsCluster(x, y, color)) {
            return false;
        }
        return true;
    }

    /**
     * Rule 1: No chip may be placed in any of the four corners.
     */
    private static boolean isCorner(int x, int y) {
        return (x == 0 || x == Board.SIZE - 1) && (y == 0 || y == Board.SIZE);
    }

    /**
     * Rule 2: No chip may be placed in a goal of the opposite color. BLACK goal
     * is top and bottom, WHITE goal is left and right
     */
    private static boolean isOpponentGoal(int x, int y, int color) {
        if (color == Tile.BLACK) {
            return x == 0 || x == Board.SIZE - 1;
        }
        else {
            return y == 0 || y == Board.SIZE - 1;
        }
    }

    /**
     * Rule 3: No chip may be placed in a square that is already occupied.
     */
    private boolean isEmpty(int x, int y) {
        return tileAt(x, y) == Tile.EMPTY;
    }

    boolean isEmpty(Position p) {
        return isEmpty(p.x, p.y);
    }

    /**
     * Rule 4: A player may not have more than two chips in a connected group,
     * whether connected orthogonally or diagonally.
     */
    private boolean formsCluster(int x, int y, int color) {
        Set<Position> visited = new HashSet<>();
        Neighbors n1 = new Neighbors(x, y);
        int count = 0;
        for (Position p1 : n1) {
            if (p1 == null || visited.contains(p1)) {
                continue;
            }
            visited.add(p1);
            if (tileAt(p1.x, p1.y) == color) {
                if (count == 1) {
                    return true;
                }
                count = count + 1;
                Neighbors n2 = new Neighbors(p1);
                for (Position p2 : n2) {
                    if (p2 == null || visited.contains(p2)) {
                        continue;
                    }
                    if (tileAt(p2.x, p2.y) == color) {
                        if (count == 1) {
                            return true;
                        }
                        count = count + 1;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if (x,y) refers to an actual location on the board.
     */
    public static boolean isOnBoard(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    /**
     * Returns true if Position p refers to an actual location on the board.
     */
    public static boolean isOnBoard(Position p) {
        return isOnBoard(p.x, p.y);
    }

    public static void updateWeights(int winner) {
        int count = 1;
        for (int state : stateSeq) {
            double weight = stateWeights.getOrDefault(state, 1.0);
            System.out.println("Current hash: " + state + " - " + weight);
            if (count % 2 == winner) {
                // white won
                stateWeights.put(state, weight * 2);
                System.out.println("New hash: " + state + " - " + stateWeights.get(state));
            }
            else {
                stateWeights.put(state, weight / 2);
                System.out.println("New hash: " + state + " - " + stateWeights.get(state));
            }
            count ++;
        }
    }

    public static void loadWeights() {
        try {
            FileInputStream fis =
                    new FileInputStream("/Users/Johnny/IdeaProjects/DataStructures/Network/board/weight.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            stateWeights = (HashMap) obj;
            System.out.println("Successfully loaded!");

        }
        catch (IOException e) {
            stateWeights = new HashMap<>();
            saveWeights();
            System.out.println("New weights made and saved");
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe);
        }
    }

    public static void saveWeights() {
        try (
            FileOutputStream fos =
                    new FileOutputStream("/Users/Johnny/IdeaProjects/DataStructures/Network/board/weight.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            oos.writeObject(stateWeights);
        }
        catch (IOException e) {
            System.out.println(e);
        }

    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (tileAt(x, y) == Tile.BLACK) {
                    hash += hash(hash, "B" + x + y);
                }
                if (tileAt(x, y) == Tile.WHITE) {
                    hash += hash(hash, "W" + x + y);
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
                if (board[x][y] == Tile.EMPTY) {
                    sb.append("E ");
                }
                if (board[x][y] == Tile.WHITE) {
                    sb.append("W ");
                }
                if (board[x][y] == Tile.BLACK) {
                    sb.append("B ");
                }
            }
            sb.append(count);
            sb.append("\n");
            count = count + 1;
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        Board b = new Board();
        System.out.println(b.hashCode());
        b.executeMove(new Move(5, 5));
        System.out.println(b.hashCode());
        for (int hash : Board.stateWeights.keySet()) {
            System.out.println(hash + " - " + Board.stateWeights.get(hash));
        }
    }

}
