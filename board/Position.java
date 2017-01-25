package board;
/**
 * The sole purpose of this class is to combine x and y
 * coordinates into a single object.
 */
public final class Position {

    public int x;
    public int y;
    public int direction;
    public int goal;

    public Position(Position p) {
        this.x = p.x;
        this.y = p.y;
        this.direction = p.direction;
        goal = Board.goalValue(x, y);
    }

    public Position(int x, int y) {
        this(x, y, -1);
    }

    public Position(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        goal = Board.goalValue(x, y);
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

    public static boolean isSameDirection(Position p1, Position p2) {
        if (p1.direction == Direction.NORTH || p1.direction == Direction.SOUTH) {
            return p2.direction == Direction.NORTH || p2.direction == Direction.SOUTH;
        }
        if (p1.direction == Direction.WEST || p1.direction == Direction.EAST) {
            return p2.direction == Direction.WEST || p2.direction == Direction.EAST;
        }
        if (p1.direction == Direction.NORTHWEST || p1.direction == Direction.SOUTHEAST) {
            return p2.direction == Direction.NORTHWEST || p2.direction == Direction.SOUTHEAST;
        }
        if (p1.direction == Direction.NORTHEAST || p1.direction == Direction.SOUTHWEST) {
            return p2.direction == Direction.NORTHEAST || p2.direction == Direction.SOUTHWEST;
        }
        return false;
    }

    @Override
    public String toString() {
        String d = "";
        switch (direction) {
            case -1:
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
        return "(" + x + "," + y + ") " + d;
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
