package game;

public class Move {
    private int board;
    private int line;
    private int column;

    private int player;

    public Move(int board, int line, int column, int player) {
        this.board = board;
        this.line = line;
        this.column = column;

        this.player = player;
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

    public String toString() {
        return "[" + this.board + ' ' + this.column + ' ' + this.line + "]";
    }
}