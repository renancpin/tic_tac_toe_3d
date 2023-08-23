package game.instructions;

public enum InstructionType {
    INVALID(-1),
    SET_TURN(0),
    SET_GUEST(1),
    END_MATCH(2),
    SET_CELL(3);

    private final int num;

    InstructionType(int instruction) {
        this.num = instruction;
    };

    public int toInt() {
        return this.num;
    }

    public static InstructionType fromInt(int num) {
        switch (num) {
            case (0):
                return InstructionType.SET_TURN;
            case (1):
                return InstructionType.SET_GUEST;
            case (2):
                return InstructionType.END_MATCH;
            case (3):
                return InstructionType.SET_CELL;
        }

        return InstructionType.INVALID;
    }

    public static InstructionType fromChar(char ch) {
        return InstructionType.fromInt(Character.getNumericValue(ch));
    }
}
