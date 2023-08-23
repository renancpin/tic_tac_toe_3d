package game.match;

import game.instructions.Instruction;
import game.move.Move;

public interface Match {
    public static final int BOARD_SIZE = 3;

    public void handleInstruction(Instruction instruction);

    public void handleMove(Move move);

    public void handleMessage(String message);

    public int getCurrentPlayer();

    public boolean getIsRunning();

    public int getThisPlayer();

    public int getCell(int board, int line, int column);
}
