package game.match;

import game.instructions.InstructionConsumerInterface;
import game.move.Move;

public interface MatchInterface {
    public static final int BOARD_SIZE = 3;

    public static final int PLAYER_LIMIT = 2;

    public void sendMove(Move move);

    public void sendMessage(String message, int player);

    public void join(InstructionConsumerInterface listener);

    public void start();

    public boolean getIsRunning();

    public int getCurrentPlayer();

    public int getCell(int board, int line, int column);
}
