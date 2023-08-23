package game.move;

public class Move {
    private int board = -1;
    private int line = -1;
    private int column = -1;

    private int player = -1;

    private MoveType moveType = MoveType.INVALID;

    private Move(int player) {
        this.player = player;
    }

    public Move(int player, int board, int line, int column) {
        this(player);

        this.board = board;
        this.line = line;
        this.column = column;
    }

    public Move(int player, int board, int line, int column, int boardSize) {
        this(player, board, line, column);
        this.setMoveType(boardSize);
    }

    private Move(int player, MoveType moveType) {
        this(player);
        this.moveType = moveType;
    }

    public static Move Surrender(int player) {
        return new Move(player, MoveType.SURRENDER);
    }

    public static Move SkipTurn(int player) {
        return new Move(player, MoveType.SKIP_TURN);
    }

    public int getBoard() {
        return this.board;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public int getPlayer() {
        return this.player;
    }

    public MoveType getMoveType() {
        return this.moveType;
    }

    public void setMoveType(int boardSize) {
        if (this.moveType != MoveType.INVALID) {
            return;
        }

        int extremities = 0;

        final int board = this.getBoard();
        final int line = this.getLine();
        final int column = this.getColumn();

        if (board == -1 || line == -1 || column == -1) {
            this.moveType = MoveType.INVALID;
            return;
        }

        if (board == 0 || board == boardSize - 1) {
            extremities++;
        }

        if (line == 0 || line == boardSize - 1) {
            extremities++;
        }

        if (column == 0 || column == boardSize - 1) {
            extremities++;
        }

        this.moveType = MoveType.fromInt(extremities);
    }

    public String toString() {
        return "[Player " + this.player + "] Board " + this.board + " Row " + this.line + " Column " + this.column;
    }
}