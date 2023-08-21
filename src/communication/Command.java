package communication;

import game.Move;

public class Command {
    private String message;
    private Move move;
    private Exception error;

    private CommandType type;

    public Command(String message) {
        this.type = CommandType.MESSAGE;
        this.message = message;
    }

    public Command(Move move) {
        this.type = CommandType.MOVE;
        this.move = move;
    }

    public Command(Exception error) {
        this.type = CommandType.ERROR;
        this.error = error;
    }

    public CommandType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public Move getMove() {
        return this.move;
    }

    public Exception getException() {
        return this.error;
    }
}
