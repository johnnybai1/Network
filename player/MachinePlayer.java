/* MachinePlayer.java */

package player;

import board.Board;
import board.Rules;
import board.Tile;
import minimax.BoardEvaluator;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;


/**
 * An implementation of an automatic Network player.  Keeps track of moves
 * made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {

    private int searchDepth;
    private int color;
    private Board board;
    static int movesConsidered = 0;

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

    // Returns a new move by "this" player.  Internally records the move (updates
    // the internal game board) as a move by "this" player.
    public Move chooseMove() {
        movesConsidered = 0;
        MoveScore ms;
        ms = chooseMove(0, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        executeMove(ms.m);
        System.out.println("Moves Considered: " + movesConsidered);
        movesConsidered = 0;
        return ms.m;
    }

    public MoveScore chooseMove(int depth, int alpha, int beta, boolean self) {
        int score = BoardEvaluator.evaluate(board, color);
        if (depth > searchDepth || BoardEvaluator.winner(board) != -1) {
            return new MoveScore(score);
        }
        List<Move> moves = BoardEvaluator.getValidMoves(board);
        MoveScore best;
        MoveScore reply;
        if (self) {
            // Max
            best = new MoveScore(moves.get(0), alpha);
        }
        else best = new MoveScore(moves.get(0), beta);
        for (Move m : moves) {
            movesConsidered++;
            executeMove(m);
            reply = chooseMove(depth + 1, alpha, beta, !self);
            undoMove(m);
            if (self && reply.score > best.score) {
                best = new MoveScore(m, reply.score);
                alpha = reply.score;
//                System.out.println("New best move: " + best);
            }
            else if (!self && reply.score < best.score) {
                best = new MoveScore(m, reply.score);
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
        return executeMove(m);
    }

    // If the Move m is legal, records the move as a move by "this" player
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method is used to help set up "Network problems" for your
    // player to solve.
    public boolean forceMove(Move m) {
        return executeMove(m);
    }

    /**
     * If the move is valid, this method will parse the move object
     * and update the board and return true, otherwise it will do
     * nothing and return false.
     */
    private boolean executeMove(Move move) {
        int player = board.getTurn();
        switch (move.moveKind) {
            case Move.ADD: {
                if (Rules.isValidAddMove(board, move)) {
                    board.setTile(move.x1, move.y1, player);
                    if (player == Tile.BLACK) {
                        board.incrementBlack();
                    }
                    else board.incrementWhite();
                    board.endTurn();
                    return true;
                }
            }
            case Move.STEP: {
                if (Rules.isValidStepMove(board, move)) {
                    board.setTile(move.x2, move.y2, Tile.EMPTY);
                    board.setTile(move.x1, move.y1, player);
                    board.endTurn();
                    return true;
                }
            }
//            case Move.QUIT: {
//                System.out.println("QUIT move executed. The game is over!");
//                System.exit(0);
//            }
        }
        return false;
    }

    /**
     * Reverts the move executed by player, intended to be used after an execute move, so there is no
     * need to check for the validity of moves. This method is intended to be used for searching the game
     * tree for the best move.
     */
    private boolean undoMove(Move move) {
        int turn = board.getTurn();
        switch (move.moveKind) {
            case Move.ADD: {
                board.setTile(move.x1, move.y1, Tile.EMPTY);
                if (turn == Tile.WHITE) {
                    // Currently WHITE's turn, so undo BLACK's move
                    board.decrementBlack();
                }
                else board.decrementWhite();
                board.endTurn();
                return true;
            }
            case Move.STEP: {
                board.setTile(move.x1, move.y1, Tile.EMPTY);
                board.setTile(move.x2, move.y2, Board.getOtherColor(turn));
                board.endTurn();
                return true;
            }
        }
        return false;
    }

    /**
     * Applies a series of moves, where even-indexed moves are made by p1 and
     * odd-indexed moves are made by p2.
     */
    public static void applyMoves(List<Move> moves, Player p1, Player p2) {
        Player curr = p1;
        Player opp = p2;
        for (Move m : moves) {
            curr.forceMove(m);
            opp.opponentMove(m);
            Player temp = curr;
            curr = opp;
            opp = temp;
        }
    }

    public static void main(String[] args) {
        MachinePlayer p1 = new MachinePlayer(Tile.WHITE);
        MachinePlayer p2 = new MachinePlayer(Tile.BLACK);
        int count = 1;
        while (BoardEvaluator.evaluate(p1.board, p1.color) != Integer.MIN_VALUE
                || BoardEvaluator.evaluate(p2.board, p2.color) != Integer.MAX_VALUE) {
            System.out.println("Turn: " + count);
            long startTime = System.nanoTime();
            Move m = p1.chooseMove();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("Move found in " + (int)duration/1000000 + "ms");
            p2.opponentMove(m);
            startTime = System.nanoTime();
            m = p2.chooseMove();
            endTime = System.nanoTime();
            duration = (endTime - startTime);
            System.out.println("Move found in " + (int)duration/1000000 + "ms");
            p1.opponentMove(m);
            count = count + 1;
        }
        p1.board.printBoard();
        System.out.println(p1.board.getTurn());
        System.out.println(BoardEvaluator.winner(p1.board));
    }
}
