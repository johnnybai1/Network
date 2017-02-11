/* MachinePlayer.java */

package player;

import board.Board;
import board.Tile;

import java.util.List;


/**
 * An implementation of an automatic Network player.  Keeps track of moves
 * made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {

    private int searchDepth;
    private int color;
    private Board board;
    private static int moves;

    // Creates a machine player with the given color.  Color is either 0 (black)
    // or 1 (white).  (White has the first move.)
    public MachinePlayer(int color) {
        this(color, 3);
    }

    // Creates a machine player with the given color and search depth.  Color is
    // either 0 (black) or 1 (white).  (White has the first move.)
    public MachinePlayer(int color, int searchDepth) {
        this.myName = "Johnny";
        this.color = color;
        this.searchDepth = searchDepth;
        this.board = new Board();
    }

    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {

        }
    }

    // Returns a new move by "this" player.  Internally records the move (updates
    // the internal game board) as a move by "this" player.
    public Move chooseMove() {
        moves = 0;
        MoveScore ms = minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        System.out.println("MOVES CONSIDERED: " + moves + " SCORE: " + ms.score);
        board.executeMove(ms.m);
        return ms.m;
    }

    private MoveScore minimax(int depth, int alpha, int beta, boolean self) {
        int score = board.evaluateWithWeight(color);
        MoveScore best = new MoveScore(score);
        if (depth == searchDepth) {
            return best;
        }
        if (score >= Board.MAX || score <= Board.MIN) {
            if (self) {
                best.score = best.score - depth;
            }
            else best.score = best.score + depth;
            return best;
        }
        if (self) {
            best.score = alpha;
        }
        else best.score = beta;
        List<Move> legal = board.getValidMoves();
        best.m = legal.get(0);

        for (Move m : legal) {
            board.executeMove(m);
            moves++;
            MoveScore reply = minimax(depth + 1, alpha, beta, !self);
            board.undoMove(m);
            if (self && reply.score > best.score) {
                best.m = m;
                best.score = reply.score;
                alpha = reply.score;
            }
            else if (!self && reply.score < best.score) {
                best.m = m;
                best.score = reply.score;
                beta = reply.score;
            }
            if (alpha >= beta) {
                return best;
            }
        }
        return best;
    }

    // If the Move m is legal, records the move as a move by the opponent
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method allows your opponents to inform you of their moves.
    public boolean opponentMove(Move m) {
        if (board.isValidMove(m)) {
            board.executeMove(m);
            Board.stateSeq.add(board.hashCode());
            return true;
        }
        return false;
    }

    // If the Move m is legal, records the move as a move by "this" player
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method is used to help set up "Network problems" for your
    // player to solve.
    public boolean forceMove(Move m) {
        if (board.isValidAddMove(m)) {
            board.executeMove(m);
            return true;
        }
        return false;
    }

    private static void train(int n) {
        for (int i = 0; i < n; i ++) {
            MachinePlayer white = new MachinePlayer(Tile.WHITE);
            MachinePlayer black = new MachinePlayer(Tile.BLACK);
            while (white.board.evaluate(Tile.WHITE) < Board.MAX && white.board.evaluate(Tile.WHITE) > Board.MIN) {
                Move m;
                if (white.board.turn() == Tile.WHITE) {
                    m = white.chooseMove();
                    System.out.println("White: " + m);
                    black.opponentMove(m);
                } else {
                    m = black.chooseMove();
                    System.out.println("Black: " + m);
                    white.opponentMove(m);
                }
            }
            int score = white.board.evaluate(Tile.WHITE);
            if (score == Board.MAX) {
                // white won
                Board.updateWeights(Tile.WHITE);
            } else {
                // black won
                Board.updateWeights(Tile.BLACK);
            }
            Board.saveWeights();
        }
    }

    public static void main(String[] args) {
        MachinePlayer white = new MachinePlayer(Tile.WHITE);
        MachinePlayer black = new MachinePlayer(Tile.BLACK);
        List<Move> valid = white.board.getValidMoves();
        for (Move v : valid) {
            white.board.executeMove(v);
            System.out.println(v + ": " + white.board.evaluate(white.color) +
                    " " + white.board.evaluateWithWeight(white.color));
            white.board.undoMove(v);
        }
        Move m = new Move(0, 3);
        white.forceMove(m);
        black.opponentMove(m);
        valid = black.board.getValidMoves();
        System.out.println("BLACK MOVES");
        for (Move v : valid) {
            black.board.executeMove(v);
            System.out.println(v + ": " + black.board.evaluate(black.color) +
                    " " + " " + Board.stateWeights.get(black.board.hashCode()) + " " + white.board.evaluateWithWeight(black.color));
            black.board.undoMove(v);
        }
    }

    /**
    public static void main(String[] args) {
        MachinePlayer p1 = new MachinePlayer(Tile.WHITE);
        MachinePlayer p2 = new MachinePlayer(Tile.BLACK);
        Move m = new Move(0, 3);
        p1.forceMove(m);
        p2.opponentMove(m);
        m = new Move(1,2);
        p2.forceMove(m);
        p1.opponentMove(m);
        m = new Move(2,3);
        p1.forceMove(m);
        p2.opponentMove(m);
        m = new Move(1,5);
        p2.forceMove(m);
        p1.opponentMove(m);
        m = new Move(2,5);
        p1.forceMove(m);
        p2.opponentMove(m);
        m = new Move(3,0);
        p2.forceMove(m);
        p1.opponentMove(m);
        m = new Move(4,5);
        p1.forceMove(m);
        p2.opponentMove(m);
        m = new Move(3,7);
        p2.forceMove(m);
        p1.opponentMove(m);
        m = new Move(4,1);
        p1.forceMove(m);
        p2.opponentMove(m);
        p1.board.printBoard();
        List<Move> valid = p2.board.getValidMoves();
        for (Move v : valid) {
            p2.board.executeMove(v);
            System.out.println(v + ": " + p2.board.evaluate(p2.color));
            p2.board.undoMove(v);
        }
        m = new Move(4, 2);
        p2.forceMove(m);
        p1.opponentMove(m);

        p2.board.printBoard();
        p2.board.evaluate(p2.color);
        valid = p1.board.getValidMoves();
        for (Move v : valid) {
            p2.board.executeMove(v);
            System.out.println(v + ": " + p1.board.evaluate(p1.color));
            p1.board.undoMove(v);
        }
        m = new Move(7,1);
        p1.forceMove(m);
        p2.opponentMove(m);
        valid = p2.board.getValidMoves();
        for (Move v : valid) {
            p2.board.executeMove(v);
            System.out.println(v + ": " + p2.board.evaluate(p2.color));
            p2.board.undoMove(v);
        }
    }
     **/

}
