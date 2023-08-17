package game;

import game.exceptions.*;

public class Match {
    public static final int SIZE = 3;

    private int[][][] cells = new int[SIZE][SIZE][SIZE];
    private boolean isGameRunning = true;

    private void assertLegalMove(Move move) throws InvalidMoveException {
        if (!isGameRunning) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.GAME_OVER, move);
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

    private boolean checkWinCondition2dDiagonals(Move move, CellType cellType) {
        final int player = move.getPlayer();

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

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

    private boolean checkWinCondition3dDiagonals(Move move, CellType cellType) {
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

    private static CellType getCellType(Move move) throws InvalidCellTypeException {
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

    public boolean makeMove(Move move, int player) {
        try {
            assertLegalMove(move);

            final int board = move.getBoard();
            final int line = move.getLine();
            final int column = move.getColumn();

            this.cells[board][line][column] = player;

            CellType cellType = getCellType(move);

            boolean isVictory = checkWinConditionStraightLines(move);

            if (!isVictory) {
                isVictory = checkWinCondition2dDiagonals(move, cellType);
            }

            if (!isVictory && (cellType == CellType.CUBE_CENTER || cellType == CellType.VERTEX)) {
                isVictory = checkWinCondition3dDiagonals(move, cellType);
            }

            if (isVictory) {
                isGameRunning = false;
            }

            return isVictory;
        } catch (InvalidMoveException e) {
            System.out.println(e);
        } catch (InvalidCellTypeException e) {
            System.out.println(e);
        }

        return false;
    }
}
