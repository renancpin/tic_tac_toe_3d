package game.move;

public enum MoveType {
    INVALID(-1),
    CUBE_CENTER(0),
    FACE_CENTER(1),
    EDGE(2),
    VERTEX(3),
    SKIP_TURN(-2),
    SURRENDER(-3);

    MoveType(int extremities) {
    }

    public static MoveType fromInt(int extremities) {
        switch (extremities) {
            case (0):
                return MoveType.CUBE_CENTER;
            case (1):
                return MoveType.FACE_CENTER;
            case (2):
                return MoveType.EDGE;
            case (3):
                return MoveType.VERTEX;
        }

        return MoveType.INVALID;
    }

    public static MoveType fromChar(char ch) {
        return MoveType.fromInt(Character.getNumericValue(ch));
    }
}
