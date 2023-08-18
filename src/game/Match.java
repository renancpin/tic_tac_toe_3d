package game;

import game.exceptions.*;

public class Match {
    public static final int SIZE = 3;

    private int[][][] cells = new int[SIZE][SIZE][SIZE];
    private boolean isGameRunning = true;
    private int currentPlayer = 1;

    public int makeMove(Move move) {
        try {
            assertLegalMove(move);

            final int board = move.getBoard();
            final int line = move.getLine();
            final int column = move.getColumn();
            final int player = move.getPlayer();

            this.cells[board][line][column] = player;

            boolean isVictory = checkWinConditionStraightLines(move)
                    || checkWinCondition2dDiagonals(move)
                    || checkWinCondition3dDiagonals(move);

            if (isVictory) {
                endMatch();

                return -1;
            }

            endTurn();
        } catch (Exception e) {
            System.out.println(e);
        }

        return this.currentPlayer;
    }

    private void assertLegalMove(Move move) throws InvalidMoveException {
        if (!isGameRunning) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.GAME_OVER, move);
        }

        final int player = move.getPlayer();

        if (player == 0 || player != this.currentPlayer) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.INVALID_TURN, move);
        }

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        final int[] coords = { board, line, column };

        for (int coord : coords) {
            if (coord < 0 || coord >= SIZE) {
                throw new InvalidMoveException(InvalidMoveExceptionMotive.OUT_OF_BOUNDS, move);
            }
        }

        if (this.cells[board][line][column] != 0) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.NON_EMPTY, move);
        }
    }

    private boolean checkWinConditionStraightLines(Move move) {
        final int player = move.getPlayer();

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        boolean lineVictory = true;
        boolean columnVictory = true;
        boolean perpendicularVictory = true;

        for (int i = 0; i < SIZE; i++) {
            if (this.cells[i][line][column] != player) {
                perpendicularVictory = false;
            }

            if (this.cells[board][i][column] != player) {
                lineVictory = false;
            }

            if (this.cells[board][line][i] != player) {
                columnVictory = false;
            }
        }

        return (lineVictory || columnVictory || perpendicularVictory);
    }

    private boolean checkWinCondition2dDiagonals(Move move) {
        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();
        final int player = move.getPlayer();

        final CellType cellType = getCellType(move);

        final int lookForCoordinate = 1;
        boolean isVictory = false;

        if (cellType == CellType.EDGE) {
            if (board == lookForCoordinate) {
                isVictory = checkBoardPlane(board, player);
            } else if (line == lookForCoordinate) {
                isVictory = checkLinePlane(line, player);
            } else if (column == lookForCoordinate) {
                isVictory = checkColumnPlane(column, player);
            }

        } else if (cellType == CellType.FACE_CENTER) {
            if (board != lookForCoordinate) {
                isVictory = checkBoardPlane(board, player);
            } else if (line != lookForCoordinate) {
                isVictory = checkLinePlane(line, player);
            } else if (column != lookForCoordinate) {
                isVictory = checkColumnPlane(column, player);
            }
        } else if (cellType == CellType.VERTEX) {
            isVictory = checkBoardPlane(board, player) ||
                    checkLinePlane(line, player) ||
                    checkColumnPlane(column, player);
        }

        return isVictory;
    }

    private boolean checkBoardPlane(int board, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < SIZE; i++) {
            if (this.cells[board][i][i] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[board][i][SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkLinePlane(int line, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < SIZE; i++) {
            if (this.cells[i][line][i] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[i][line][SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkColumnPlane(int column, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < SIZE; i++) {
            if (this.cells[i][i][column] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[i][SIZE - 1 - i][column] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkWinCondition3dDiagonals(Move move) {
        final CellType cellType = getCellType(move);

        if (cellType != CellType.CUBE_CENTER && cellType != CellType.VERTEX) {
            return false;
        }

        final int player = move.getPlayer();

        final int LAST = SIZE - 1;
        boolean diagonal1Victory = false;
        boolean diagonal2Victory = false;
        boolean diagonal3Victory = false;
        boolean diagonal4Victory = false;

        for (int i = 0; i < SIZE; i++) {
            // [0][0][0] to [LAST][LAST][LAST]
            if (this.cells[i][i][i] != player) {
                diagonal1Victory = false;
            }

            // [0][0][LAST] to [LAST][LAST][0]
            if (this.cells[i][i][LAST - i] != player) {
                diagonal2Victory = false;
            }

            // [0][LAST][0] to [LAST][0][LAST]
            if (this.cells[i][LAST - i][i] != player) {
                diagonal3Victory = false;
            }

            // [0][LAST][LAST] to [LAST][0][0]
            if (this.cells[i][LAST - i][LAST - i] != player) {
                diagonal4Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory || diagonal3Victory || diagonal4Victory);
    }

    private void endTurn() {
        final int currentPlayer = this.currentPlayer;
        this.currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    private void endMatch() {
        this.isGameRunning = false;
    }

    private static CellType getCellType(Move move) {
        int extremities = 0;

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        if (board == 0 || board == SIZE - 1) {
            extremities++;
        }

        if (line == 0 || line == SIZE - 1) {
            extremities++;
        }

        if (column == 0 || column == SIZE - 1) {
            extremities++;
        }

        return CellType.from(extremities);
    }
}
