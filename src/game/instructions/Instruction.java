package game.instructions;

import java.io.Serializable;

import game.move.Move;

public class Instruction implements Serializable {
    private InstructionType type;
    private int player;
    private Move move;
    private String message;

    private Instruction(InstructionType type, int player) {
        this.type = type;
        this.player = player;
    }

    public InstructionType getType() {
        return this.type;
    }

    public int getPlayer() {
        return this.player;
    }

    public Move getMove() {
        return this.move;
    }

    public String getMessage() {
        return this.message;
    }

    public static Instruction setGuest(int player) {
        return new Instruction(InstructionType.SET_GUEST, player);
    }

    public static Instruction setTurn(int player) {
        return new Instruction(InstructionType.SET_TURN, player);
    }

    public static Instruction endMatch(int player) {
        return new Instruction(InstructionType.END_MATCH, player);
    }

    public static Instruction invalid() {
        return new Instruction(InstructionType.INVALID, 0);
    }

    public static Instruction setCell(Move move) {
        Instruction instruction = new Instruction(InstructionType.SET_CELL, move.getPlayer());
        instruction.move = move;

        return instruction;
    }

    public static Instruction message(String message, int player) {
        Instruction instruction = new Instruction(InstructionType.MESSAGE, player);
        instruction.message = message;

        return instruction;
    }
}
