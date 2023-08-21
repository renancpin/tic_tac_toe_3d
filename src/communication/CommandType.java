package communication;

public enum CommandType {
    MESSAGE(0),
    MOVE(1),
    ERROR(2);

    CommandType(int i) {
    }

    public static CommandType fromInt(int integer) {
        switch (integer) {
            case (0):
                return CommandType.MESSAGE;
            case (1):
                return CommandType.MOVE;
            case (2):
                return CommandType.ERROR;
            default:
                return CommandType.MESSAGE;
        }
    }
}
