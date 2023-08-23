package communication.command;

public enum CommandType {
    MESSAGE(0),
    MOVE(1),
    INSTRUCTION(2);

    private final int num;

    CommandType(int num) {
        this.num = num;
    }

    public int toInt() {
        return this.num;
    }

    public static CommandType fromInt(int integer) {
        switch (integer) {
            case (0):
                return CommandType.MESSAGE;
            case (1):
                return CommandType.MOVE;
            case (2):
                return CommandType.INSTRUCTION;
        }

        return CommandType.MESSAGE;
    }

    public static CommandType fromChar(char ch) {
        return CommandType.fromInt(Character.getNumericValue(ch));
    }
}
