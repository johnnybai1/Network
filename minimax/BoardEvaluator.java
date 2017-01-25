package minimax;

import board.*;
import player.Move;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class BoardEvaluator {

    private static List<Position> blackTiles;
    private static List<Position> whiteTiles;
    private static int numBlackPairs;
    private static int numWhitePairs;
    private static boolean blackNetwork;
    private static boolean whiteNetwork;

    private BoardEvaluator() {
    }

    /**
     * Initializes our static variables to keep track of the the board's
     * features.
     */
    private static void initFeatures() {
        blackTiles = new ArrayList<>(10);
        whiteTiles = new ArrayList<>(10);
        numBlackPairs = 0;
        numWhitePairs = 0;
        blackNetwork = false;
        whiteNetwork = false;
    }

    /**
     * Parses through the board and creates two lists, one for each tile color,
     * of Positions that have the appropriate colored tile at them on the Board.
     */
    private static void buildTileList(Board b) {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                // (j, i): traverse left to right, top to bottom
                if (b.getTile(j, i) == Tile.BLACK) {
                    Position p = new Position(j, i);
                    blackTiles.add(p);
                }
                // (i, j): traverse top to bottom, left to right
                if (b.getTile(i, j) == Tile.WHITE) {
                    Position p = new Position(i, j);
                    whiteTiles.add(p);
                }
            }
        }
    }

    /**
     * Returns a list of valid moves for the player who is placing a tile.
     */
    public static List<Move> getValidMoves(Board b) {
        int turn = b.getTurn();
        int numTiles = b.getCurrentPlayerTileCount();
        List<Move> moves = new ArrayList<>(64);
        if (numTiles < 10) {
            // ADD moves
            for (int x = 0; x < Board.SIZE; x++) {
                for (int y = 0; y < Board.SIZE; y++) {
                    if (Rules.isValidTilePlacement(b, x, y, turn)) {
                        moves.add(new Move(x, y));
                    }
                }
            }
        } else {
            for (int x2 = 0; x2 < Board.SIZE; x2++) {
                for (int y2 = 0; y2 < Board.SIZE; y2++) {
                    if (b.getTile(x2, y2) == turn) {
                        // We can "remove" this tile (x2, y2)
                        b.setTile(x2, y2, Tile.EMPTY);
                        for (int x1 = 0; x1 < Board.SIZE; x1++) {
                            for (int y1 = 0; y1 < Board.SIZE; y1++) {
                                if (x2 != x1 && y2 != y1 && Rules.isValidTilePlacement(b, x1, y1, turn)) {
                                    // We can place a tile at (x1, y1)
                                    moves.add(new Move(x1, y1, x2, y2));
                                }
                            }
                        }
                        b.setTile(x2, y2, turn);
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Returns a list of tiles connected to the tile at Position p on Board b.
     * 1. Must change directions when moving to next goal
     * 2. The path from position p to the other tile must not be blocked by the
     * opponent.
     * 3. If position p is in a goal, tiles in the same goal cannot be connected
     * to it.
     */
    private static List<Position> getConnected(Board b, Position p) {
        int color = b.getTile(p);
        int other = Board.getOtherColor(color);
        List<Position> connected = new ArrayList<>();
        Neighbors neighbors = new Neighbors(p);
        while (!neighbors.isEmpty()) {
            for (Position n : neighbors) {
                if (n == null || Position.isSameDirection(p, n)) {
                    // Invalid position or have not turned a corner yet
                    continue;
                }
                if (b.getTile(n) == other) {
                    // This direction is blocked by opponent
                    neighbors.remove(n.direction);
                    continue;
                }
                if (b.getTile(n) == color) {
                    if (Board.isSameGoal(p, n)) {
                        // n is not connected to p if they are in the same
                        // goal zone
                        continue;
                    }
                    connected.add(new Position(n));
                }
            }
            neighbors.advance();
        }
        return connected;
    }

    /**
     * Returns a chain that is either a network or the longest chain possible.
     */
    private static LinkedList<Position> getChain(Board b, int color) {
        List<Position> tiles;
        if (color == Tile.BLACK) {
            tiles = blackTiles;
        } else tiles = whiteTiles;
        LinkedList<Position> result = new LinkedList<>();
        for (Position p : tiles) {
            int maxLength = 0;
            LinkedList<Position> chain = new LinkedList<>();
            chain.add(p);
            SearchNode start = new SearchNode(p, chain); // Starting node
            Stack<SearchNode> frontier = new Stack<>();
            frontier.add(start);
            while (!frontier.isEmpty()) {
                SearchNode current = frontier.pop();
                Position currentPos = current.pos;
                List<Position> connected = getConnected(b, currentPos);
                LinkedList<Position> linked = new LinkedList<>(current.linked);
                for (Position next : connected) {
                    if (!linked.contains(next)) {
                        // Do not re-explore already added nodes
                        linked = new LinkedList<>(current.linked);
                        linked.add(next);
                        SearchNode sn = new SearchNode(next, linked);
                        if (!Board.isGoal(next)) {
                            // Do not further explore if next is in a goal zone
                            frontier.push(sn);
                        }
                        if (isNetwork(linked)) {
                            // Return the chain if it is a network
                            if (color == Tile.BLACK) {
                                blackNetwork = true;
                            } else whiteNetwork = true;
                            return linked;
                        }
                        if (linked.size() > maxLength) {
                            maxLength = linked.size();
                            result = linked;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * A network is a series of 6 or more connected tiles that begins in
     * one goal and ends in another.
     */
    private static boolean isNetwork(LinkedList<Position> chain) {
        if (chain.size() > 5) {
            Position first = chain.getFirst();
            Position last = chain.getLast();
            return Board.isOppositeGoal(first, last);
        }
        return false;
    }

    private static void countPairs(Board b) {
        for (Position p : blackTiles) {
            numBlackPairs = numBlackPairs + getConnected(b, p).size();
        }
        numBlackPairs = numBlackPairs / 2;
        for (Position p : whiteTiles) {
            numWhitePairs = numWhitePairs + getConnected(b, p).size();
        }
        numWhitePairs = numWhitePairs / 2;
    }

    private static int getNumPairs(int color) {
        if (color == Tile.BLACK) {
            return numBlackPairs;
        }
        return numWhitePairs;
    }

    /**
     * If we have a tile in a goal, gain 1 point
     * If the goal was established early (< 3 moves), gain 5 points.
     * If that goal piece is towards the middle of the board (3 or 4) gain 2 point.
     * If we have multiple tiles in the same goal, penalize one point for each
     */
    private static int processGoals(int color) {
        int score = 0;
        List<Position> tiles;
        if (color == Tile.BLACK) {
            tiles = blackTiles;
        } else tiles = whiteTiles;
        int goal1Count = 0;
        int goal2Count = 0;
        for (Position p : tiles) {
            if (p.goal == 1) {
                goal1Count++;
            }
            if (p.goal == -1) {
                goal2Count++;
            }
            if (Board.isCenterGoal(p)) {
                score = 2;
            }
        }
        if (goal1Count > 0 || goal2Count > 0) {
            score = score + 1;
            if (tiles.size() < 3) {
                score = score + 5;
            }
            if (goal1Count > 1) {
                score = score - goal1Count + 1;
            }
            if (goal2Count > 1) {
                score = score - goal2Count + 1;
            }
        }
        return score;
    }

    /**
     * Returns 0 if BLACK won, 1 if WHITE won, or -1 if no one winner
     */
    public static int winner(Board b) {
        int turn = b.getTurn(); // Current player's turn
        if (blackNetwork && whiteNetwork) {
            // Both players have a network
            return turn;
        }
        if (blackNetwork) {
            return Tile.BLACK;
        }
        if (whiteNetwork) {
            return Tile.WHITE;
        }
        return -1;
    }

    /**
     * Scores the given Board b.
     * Return Integer.MAX_VALUE if player makes a move that results in him winning
     * Return Integer.MIN_VALUE if player makes a move that results in him losing
     * Scoring strategy
     * 1. My pair count - your pair count
     * 2. My longest chain size ^ 2 - your longest chain size ^ 2
     * 3. Bonus points for having goal zone towards middle of the board.
     */
    public static int evaluate(Board b, int player) {
        int score = 0;
        int other = Board.getOtherColor(player);
        initFeatures();
        buildTileList(b);
        countPairs(b);
        List<Position> listPlayer = getChain(b, player);
        int playerSize = listPlayer.size();
        List<Position> listOther = getChain(b, other);
        int otherSize = listOther.size();
        int winner = winner(b);
        if (winner == player) {
            // We won
            return Integer.MAX_VALUE;
        }
        if (winner == other) {
            // They won
            return Integer.MIN_VALUE;
        }
        score = score + processGoals(player) - processGoals(other);
        score = score + getNumPairs(player) - getNumPairs(other);
        score = score + playerSize * playerSize - otherSize * otherSize;
        return score;
    }

    /**
     * Helper method to print a list of positions
     */
    public static void printPositionList(List<Position> positions) {
        StringBuilder sb = new StringBuilder();
        for (Position p : positions) {
            sb.append(p);
            sb.append(" ");
        }
        System.out.println(sb.toString());
    }

    private static final class SearchNode {

        Position pos;
        LinkedList<Position> linked;
        boolean hasGoal1; // Top or Left
        boolean hasGoal2; // Bottom or Right

        SearchNode(Position pos, LinkedList<Position> linked,
                   boolean hasGoal1, boolean hasGoal2) {
            this.pos = pos;
            this.linked = linked;
            this.hasGoal1 = hasGoal1;
            this.hasGoal2 = hasGoal2;
        }

        SearchNode(Position pos, LinkedList<Position> linked) {
            this(pos, linked, false, false);
        }

        SearchNode(Position pos) {
            this(pos, new LinkedList<>());
        }

    }

}
