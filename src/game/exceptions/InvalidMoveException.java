package game.exceptions;

import game.Move;

public class InvalidMoveException extends Exception {
    private String move;
    private String motive;

    public InvalidMoveException(InvalidMoveExceptionMotive motive, Move move) {
        switch (motive) {
            case OUT_OF_BOUNDS:
                this.motive = "Move coordinates out of bounds";
                break;
            case NON_EMPTY:
                this.motive = "Specified cell is already filled";
                break;
            case GAME_OVER:
                this.motive = "The game is already over";
                break;
            default:
                this.motive = "Unspecified error";
        }

        this.move = move.toString();
    }

    public String toString() {
        return motive + ". " + move;
    }
}
