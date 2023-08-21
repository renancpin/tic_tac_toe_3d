package game;

public class Move {
    private int board;
    private int line;
    private int column;

    private int player;

    private MoveType moveType = MoveType.INVALID;

    public static Move Rendition(int player) {
        Move move = new Move(player, -1, -1, -1);
        move.setMoveType(MoveType.RENDITION);

        return move;
    }

    public static Move SkipTurn(int player) {
        Move move = new Move(player, -1, -1, -1);
        move.setMoveType(MoveType.INVALID);

        return move;
    }

    public Move(int player, int board, int line, int column) {
        this.board = board;
        this.line = line;
        this.column = column;

        this.player = player;
    }

    public Move(int player, int board, int line, int column, int boardSize) {
        this(board, line, column, player);
        this.setMoveType(boardSize);
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

    private void setMoveType(MoveType type) {
        this.moveType = type;
    }

    public void setMoveType(int boardSize) {
        int extremities = 0;

        final int board = this.getBoard();
        final int line = this.getLine();
        final int column = this.getColumn();

        if (board == -1 && line == -1 && column == -1) {
            this.moveType = MoveType.RENDITION;
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

        this.moveType = MoveType.from(extremities);
    }

    public String toString() {
        return "[Player " + this.player + "] B" + this.board + " R" + this.line + " C" + this.column;
    }
}