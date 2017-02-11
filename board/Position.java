package board;
public final class Position {

    int x; // x coordinate of this Position
    int y; // y coordinate of this Position
    // Used in chains of Positions; direction to reach this Position from
    // the previous position.
    int direction;
    int goal; // integer representation of whether this tile is in a goal or not
    int space; // Amount of space between this Position and previous position.

    public Position(int x, int y, int direction, int space) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.space = space;
        this.goal = Board.toGoal(x, y);
    }

    public Position(int x, int y, int direction) {
        this(x, y, direction, 0);
    }

    public Position(int x, int y) {
        this(x, y, Direction.NONE, 0);
    }

    public Position(Position p, int space) {
        this.x = p.x;
        this.y = p.y;
        this.direction = p.direction;
        this.goal = Board.toGoal(p.x, p.y);
        this.space = space;
    }

    // Copy constructor
    public Position(Position p) {
        this(p, p.space);
    }

    /**
     * Adds position p to this position. (Destructive)
     **/
    public void add(Position p) {
        if (p != null) {
            x = x + p.x;
            y = y + p.y;
        }
    }

    /**
     * Returns true if the direction travelled to reach Position p1 did not
     * require turning a corner to reach Position p2 from p1. For three
     * Positions, A, B and C, A B and C are not considered to be connected
     * if they lie on a single line.
     */
    public static boolean isSameDirection(Position p1, Position p2) {
        if (p1.direction == Direction.NORTH ||
                p1.direction == Direction.SOUTH) {
            return p2.direction == Direction.NORTH ||
                    p2.direction == Direction.SOUTH;
        }
        if (p1.direction == Direction.WEST ||
                p1.direction == Direction.EAST) {
            return p2.direction == Direction.WEST ||
                    p2.direction == Direction.EAST;
        }
        if (p1.direction == Direction.NORTHWEST ||
                p1.direction == Direction.SOUTHEAST) {
            return p2.direction == Direction.NORTHWEST ||
                    p2.direction == Direction.SOUTHEAST;
        }
        if (p1.direction == Direction.NORTHEAST ||
                p1.direction == Direction.SOUTHWEST) {
            return p2.direction == Direction.NORTHEAST ||
                    p2.direction == Direction.SOUTHWEST;
        }
        return false;
    }

    public boolean isGoal() {
        return goal != 0;
    }

    public static boolean isSameGoal(Position p1, Position p2) {
        return p1.goal == p2.goal && p1.goal != 0;
    }

    public static boolean isOppositeGoal(Position p1, Position p2) {
        return p1.goal != p2.goal && p1.goal != 0 && p2.goal != 0;
    }

    public static boolean isCentral(Position p) {
        return p.x == 3 || p.x == 4 || p.y == 3 || p.y == 4;
    }

    @Override
    public String toString() {
        String d = "";
        switch (direction) {
            case 8:
                d = "NONE";
                break;
            case 0:
                d = "NORTHWEST";
                break;
            case 1:
                d = "WEST";
                break;
            case 2:
                d = "SOUTHWEST";
                break;
            case 3:
                d = "NORTH";
                break;
            case 4:
                d = "SOUTH";
                break;
            case 5:
                d = "NORTHEAST";
                break;
            case 6:
                d = "EAST";
                break;
            case 7:
                d = "SOUTHEAST";
                break;
        }
        return "[(" + x + "," + y + ") " + d + ", " + space + "]";
    }

    @Override
    public boolean equals(Object o) {
        Position other = (Position) o;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 10 * x + y;
    }

}
