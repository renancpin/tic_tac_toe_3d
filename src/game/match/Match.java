package game.match;

import java.util.function.Consumer;

import communication.command.Command;
import game.move.Move;

public abstract class Match {
    public static final int BOARD_SIZE = 3;

    public static int getNextPlayer(int currentPlayer) {
        return currentPlayer == 1 ? 2 : 1;
    }

    public abstract void handleMove(Move move);

    public abstract void handleMessage(String message);

    public abstract void listen(Consumer<Command> listener);

    public abstract void start();

    public abstract boolean getIsRunning();

    public abstract int getThisPlayer();

    public abstract int getCurrentPlayer();

    public abstract int getCell(int board, int line, int column);
}
