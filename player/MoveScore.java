package player;

/**
 * Identical to a Move object, except it stores a value indicative of how good
 * the resulting board from making "this" Move will be.
 */
public class MoveScore{

    public int score;
    public Move m;

    public MoveScore(Move m, int score) {
        this.m = m;
        this.score = score;
    }

    public MoveScore(int score) {
        this.m = null;
        this.score = score;
    }

    public MoveScore(Move m) {
        this(m, 0);
    }

    @Override
    public String toString() {
        if (m == null) {
            return "Null move: " + score;
        }
        return m.toString() + " " + score;
    }
}
