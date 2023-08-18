package game;

public enum MoveType {
    VERTEX(3),
    EDGE(2),
    FACE_CENTER(1),
    CUBE_CENTER(0),
    RENDITION(-1),
    SKIP_TURN(-2),
    INVALID(-3);

    MoveType(int extremities) {
    }

    public static MoveType from(int extremities) {
        switch (extremities) {
            case (0):
                return MoveType.CUBE_CENTER;
            case (1):
                return MoveType.FACE_CENTER;
            case (2):
                return MoveType.EDGE;
            case (3):
                return MoveType.VERTEX;
            case (-1):
                return MoveType.RENDITION;
            case (-2):
                return MoveType.SKIP_TURN;
            default:
                return MoveType.INVALID;
        }
    }
}
