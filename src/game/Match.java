package game;

import game.exceptions.InvalidMoveException;

public interface Match {
    public static final int BOARD_SIZE = 3;

    public void makeMove(Move move) throws InvalidMoveException;

    public void skipTurn(int player) throws InvalidMoveException;

    public void surrender(int player) throws InvalidMoveException;

    public int getCurrentPlayer();

    public boolean getIsRunning();
}
