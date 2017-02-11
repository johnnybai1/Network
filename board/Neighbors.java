package board;

import java.util.Iterator;

/**
 * This class stores a set of Position objects that is connected (orthogonally
 * or diagonally) to the Position (x,y). This class does not determine whether
 * two Tiles are validly connected or not.
 **/
public class Neighbors implements Iterable<Position> {


    public static final Position[] UNIT_LIST =
            {
                    new Position(-1,-1, Direction.NORTHWEST),
                    new Position(-1,0, Direction.WEST),
                    new Position(-1,1, Direction.SOUTHWEST),
                    new Position(0, -1, Direction.NORTH),
                    new Position(0,1, Direction.SOUTH),
                    new Position(1, -1, Direction.NORTHEAST),
                    new Position(1, 0, Direction.EAST),
                    new Position(1, 1, Direction.SOUTHEAST)
            };

    private Position[] neighbors; // Array of positions that are orthogonal or diagonal to (x,y)
    private int size = 0;
    private int radius = 0;

    public Neighbors(int x, int y) {
        neighbors = new Position[8];
        for (int i = 0; i < UNIT_LIST.length; i++) {
            Position unit = UNIT_LIST[i];
            int xx = x + unit.x;
            int yy = y + unit.y;
            if (Board.isOnBoard(xx, yy)) {
                neighbors[i] = new Position(xx, yy, i);
                size = size + 1;
            }
        }
    }

    public Neighbors(Position p) {
        this(p.x, p.y);
    }

    /**
     * For each position in the list, increments the Position one unit further.
     * If the increment causes a Position to reference a position not on the board,
     * the Position will be removed from the checklist.
     */
    public void advance() {
        radius = radius + 1;
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
                neighbors[i].add(UNIT_LIST[i]); // neighbors[i] is modified
                if (!Board.isOnBoard(neighbors[i].x, neighbors[i].y)) {
                    remove(i);
                }
            }
        }
    }

    /**
     * The radius refers to how much space exists between the neighboring
     * Positions and the origin Position. A radius of 0 implies a Position
     * immediately adjacent to the origin Position.
     */
    public int getRadius() {
        return radius;
    }

    public Position get(int direction) {
        if (direction >= 0 && direction <= 7) {
            return neighbors[direction];
        }
        return null;
    }

    /**
     * Removes a Position from the checklist at index i.
     **/
    public void remove(int i) {
        if (neighbors[i] != null) {
            neighbors[i] = null;
            size = size - 1;
        }
    }

    /**
     * Returns true if checklist is empty (all null elements)
     **/
    public boolean isEmpty() {
        return size == 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] == null) {
                continue;
            }
            sb.append(neighbors[i]);
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public Iterator iterator() {
        return new NeighborIterator();
    }

    public class NeighborIterator implements Iterator<Position> {

        private int i;

        public NeighborIterator() {
            i = 0;
        }

        public boolean hasNext() {
            return i != 8;
        }

        public Position next() {
            Position p = neighbors[i];
            i = i + 1;
            return p;
        }

    }

    public static void main(String[] args) {
        Neighbors n = new Neighbors(5, 5);
        System.out.println(n);

    }

}
