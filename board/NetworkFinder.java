package board;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class NetworkFinder {

    public static void main(String[] args) {
    }

    /**
     * Returns a list of Positions connected to the tile at Position p on Board
     * b. Helper method for our DFS of longest tile chains or networks.
     * 1. Must change directions when moving to next tile
     * 2. The path from position p to the other tile must not be blocked by the
     * opponent.
     * 3. If position p is in a goal, tiles in the same goal cannot be connected
     * to it.
     */
    public static List<Position> getConnected(Board b, Position p) {
        List<Position> connected = new ArrayList<>(8);
        int color = b.tileAt(p);
        int other = Tile.getOtherColor(color);
        Neighbors neighbors = new Neighbors(p);
        while (!neighbors.isEmpty()) {
            for (Position n : neighbors) {
                if (n == null || b.isEmpty(n) || Position.isSameDirection(p, n)) {
                    // Invalid neighbor position, no tile present, or have not
                    // turned a corner since last tile visited
                    continue;
                }
                if (b.tileAt(n) == other) {
                    // This direction is blocked!
                    neighbors.remove(n.direction);
                    continue;
                }
                if (b.tileAt(n) == color) {
                    if (Position.isSameGoal(p, n)) {
                        continue;
                    }
                    connected.add(new Position(n, neighbors.getRadius()));
                }
            }
            neighbors.advance();
        }
        return connected;
    }

    /**
     * Returns a SearchNode, consisting of the chain of positions, that results
     * in a network
     */
    public static SearchNode getChain(Board b, int color) {
        List<Position> tiles = b.getTiles(color);
        SearchNode result = new SearchNode(null);
        int maxLength = 0;
        for (Position p : tiles) {
            LinkedList<Position> chain = new LinkedList<>();
            chain.add(p);
            SearchNode start = new SearchNode(p, chain, false); // Starting node
            Stack<SearchNode> frontier = new Stack<>();
            frontier.add(start);
            while (!frontier.isEmpty()) {
                SearchNode current = frontier.pop();
                Position currentPos = current.position;
                List<Position> connected = getConnected(b, currentPos);
                LinkedList<Position> link = new LinkedList<>(current.chain);
                for (Position next : connected) {
                    if (!link.contains(next)) {
                        // Do not re-explore already added nodes
                        link = new LinkedList<>(current.chain);
                        link.add(next);
                        SearchNode sn = new SearchNode(next, link, false);
                        if (!next.isGoal()) {
                            // Do not further explore if next is in a goal zone
                            frontier.push(sn);
                        }
                        if (isNetwork(link)) {
                            sn.isNetwork = true;
                            return sn;
                        }
                        if (link.size() > maxLength) {
                            maxLength = link.size();
                            result = sn;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isNetwork(LinkedList<Position> chain) {
        if (chain.size() < 6) {
            return false;
        }
        Position p1 = chain.getFirst();
        Position p2 = chain.getLast();
        return Position.isOppositeGoal(p1, p2);
    }

    static final class SearchNode {

        Position position;
        LinkedList<Position> chain;
        boolean isNetwork;

        SearchNode(Position position, LinkedList<Position> chain, boolean isNetwork) {
            this.position = position;
            this.chain = chain;
            this.isNetwork = isNetwork;
        }

        SearchNode(Position position) {
            this(position, new LinkedList<>(), false);
        }

    }

}
