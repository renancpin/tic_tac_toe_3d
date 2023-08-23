package communication.command;

import game.instructions.Instruction;
import game.move.Move;

public class Command {
    private String message;
    private Move move;
    private Instruction instruction;

    private CommandType type;

    public Command(String message) {
        this.type = CommandType.MESSAGE;
        this.message = message;
    }

    public Command(Move move) {
        this.type = CommandType.MOVE;
        this.move = move;
    }

    public Command(Instruction instruction) {
        this.type = CommandType.INSTRUCTION;
        this.instruction = instruction;
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

    public Instruction getInstruction() {
        return this.instruction;
    }
}
