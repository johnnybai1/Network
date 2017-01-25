package board;

import player.Move;

import java.util.HashSet;
import java.util.Set;

public class Rules {

    private Rules() {
    }

    /**
     * Returns true if the ADD Move m can be applied to the Board b.
     * (m.x1,m.y1) must be a valid place for b.getTurn() to place a tile.
     */
    public static boolean isValidAddMove(Board b, Move m) {
        if (b.getCurrentPlayerTileCount() == 10) {
            return false;
        }
        return isValidTilePlacement(b, m.x1, m.y1, b.getTurn());
    }

    /**
     * Returns true if the STEP Move m can be applied to the Board b. After
     * (m.x2,m.y2) is removed from the board, (m.x1,m.y1) must be a valid place
     * for b.getTurn() to place a tile.
     */
    public static boolean isValidStepMove(Board b, Move m) {
        if (b.getCurrentPlayerTileCount() < 10) {
            return false;
        }
        if (m.x1 == m.x2 && m.y1 == m.y2) {
            return false;
        }
        if (b.getTile(m.x2, m.y2) != b.getTurn()) {
            return false;
        }
        b.setTile(m.x2, m.y2, Tile.EMPTY);
        boolean valid = isValidTilePlacement(b, m.x1, m.y1, b.getTurn());
        b.setTile(m.x2, m.y2, b.getTurn());
        return valid;
    }

    /**
     * (x,y) is a valid tile placement for a tile of some color on Board b if
     * it satisfies all four rules.
     */
    public static boolean isValidTilePlacement(Board b, int x, int y, int color) {
        if (isCorner(x, y)) {
            return false;
        }
        if (isOpponentGoal(x, y, color)) {
            return false;
        }
        if (!isEmpty(b, x, y)) {
            return false;
        }
        if (formsCluster(b, x, y, color)) {
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
    private static boolean isEmpty(Board b, int x, int y) {
        return b.getTile(x, y) == Tile.EMPTY;
    }

    /**
     * Rule 4: A player may not have more than two chips in a connected group,
     * whether connected orthogonally or diagonally.
     */
    private static boolean formsCluster(Board b, int x, int y, int color) {
        Set<Position> visited = new HashSet<>();
        Neighbors n1 = new Neighbors(x, y);
        int count = 0;
        for (Position p1 : n1) {
            if (p1 == null || visited.contains(p1)) {
                continue;
            }
            visited.add(p1);
            if (b.getTile(p1.x, p1.y)== color) {
                if (count == 1) {
                    return true;
                }
                count = count + 1;
                Neighbors n2 = new Neighbors(p1);
                for (Position p2 : n2) {
                    if (p2 == null || visited.contains(p2)) {
                        continue;
                    }
                    if (b.getTile(p2.x, p2.y) == color) {
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


}
