package game;

public class GuestMatch implements Match {
    private int currentPlayer = 0;
    private boolean isGameRunning = true;
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];

    public void makeMove(Move move) {
        final int player = move.getPlayer();
        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        this.cells[board][line][column] = player;
    }

    public void skipTurn(int player) {
        this.currentPlayer = player == 1 ? 2 : 1;
    }

    public void surrender(int player) {
        this.skipTurn(player);
        this.isGameRunning = false;
    }

    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    public boolean getIsRunning() {
        return this.isGameRunning;
    }
}
