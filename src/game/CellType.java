package game;

public enum CellType {
    VERTEX(3),
    EDGE(2),
    FACE_CENTER(1),
    CUBE_CENTER(0),
    INVALID(-1);

    CellType(int extremities) {
    }

    public static CellType from(int extremities) {
        switch (extremities) {
            case (0):
                return CellType.CUBE_CENTER;
            case (1):
                return CellType.FACE_CENTER;
            case (2):
                return CellType.EDGE;
            case (3):
                return CellType.VERTEX;
            default:
                return CellType.INVALID;
        }
    }
}
