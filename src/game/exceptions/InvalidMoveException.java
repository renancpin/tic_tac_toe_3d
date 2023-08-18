package game.exceptions;

import java.util.HashMap;
import java.util.Map;

import game.Move;

public class InvalidMoveException extends Exception {
    private String move;
    private String motive;

    private static Map<InvalidMoveExceptionMotive, String> MOTIVES;

    static {
        MOTIVES = new HashMap<InvalidMoveExceptionMotive, String>();

        MOTIVES.put(InvalidMoveExceptionMotive.OUT_OF_BOUNDS, "Move coordinates out of bounds");
        MOTIVES.put(InvalidMoveExceptionMotive.NON_EMPTY, "Specified cell is already filled");
        MOTIVES.put(InvalidMoveExceptionMotive.GAME_OVER, "The game is already over");
        MOTIVES.put(InvalidMoveExceptionMotive.INVALID_TURN, "Player is not currently in their turn");
    }

    public InvalidMoveException(InvalidMoveExceptionMotive motive, Move move) {
        this.motive = MOTIVES.getOrDefault(motive, "Unspecified reason");

        this.move = move.toString();
    }

    public String toString() {
        return motive + ". " + move;
    }
}
