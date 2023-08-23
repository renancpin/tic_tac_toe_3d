package game.match;

import java.io.IOException;
import java.util.Random;

import communication.command.Command;
import communication.communicator.Communicator;
import game.exceptions.*;
import game.instructions.*;
import game.move.*;

public class HostedMatch implements Match {
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private boolean isGameRunning = true;
    private int currentPlayer = 1;
    private final int thisPlayer;
    private final Communicator communicator;

    public HostedMatch(Communicator communicator, int port) {
        this.thisPlayer = (new Random()).nextInt(1, 2);
        this.communicator = communicator;

        communicator.host(port);

        final int guest = thisPlayer == 1 ? 2 : 1;
        Instruction instruction = Instruction.setGuest(guest);

        try {
            communicator.sendCommand(new Command(instruction));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getThisPlayer() {
        return this.thisPlayer;
    }

    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    public boolean getIsRunning() {
        return this.isGameRunning;
    }

    public int getCell(int board, int line, int column) {
        return this.cells[board][line][column];
    }

    public void handleMessage(String message) {
        Command command = new Command("[HOST]" + message);

        try {
            this.communicator.sendCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleMove(Move move) {
        try {
            assertLegalMove(move);
        } catch (InvalidMoveException exception) {
            this.handleMessage("[ERROR]" + exception.toString());
            return;
        }

        final MoveType moveType = move.getMoveType();

        if (moveType == MoveType.SKIP_TURN) {
            endTurn();
            return;
        }

        if (moveType == MoveType.SURRENDER) {
            endTurn();
            endMatch();
            return;
        }

        registerMove(move);

        boolean isVictory = checkWinConditionStraightLines(move)
                || checkWinCondition2dDiagonals(move)
                || checkWinCondition3dDiagonals(move);

        if (isVictory) {
            endMatch();
            return;
        }

        endTurn();
    }

    public void handleInstruction(Instruction instruction) {
        System.out.println("HOST nao recebe instrucoes");
    }

    private void assertLegalMove(Move move) throws InvalidMoveException {
        if (!this.isGameRunning) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.GAME_OVER, move);
        }

        final int player = move.getPlayer();

        if (player != this.currentPlayer) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.INVALID_TURN, move);
        }

        move.setMoveType(BOARD_SIZE);

        final MoveType moveType = move.getMoveType();

        if (moveType == MoveType.INVALID) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.OUT_OF_BOUNDS, move);

        }

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();
        if (this.cells[board][line][column] != 0) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.NON_EMPTY, move);
        }
    }

    private void registerMove(Move move) {
        final int player = move.getPlayer();

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        this.cells[board][line][column] = player;

        Instruction instruction = Instruction.setCell(move);

        try {
            this.communicator.sendCommand(new Command(instruction));
        } catch (IOException e) {
            e.printStackTrace();
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

        for (int i = 0; i < BOARD_SIZE; i++) {
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

        final MoveType moveType = move.getMoveType();

        final int lookForCoordinate = 1;
        boolean isVictory = false;

        if (moveType == MoveType.EDGE) {
            if (board == lookForCoordinate) {
                isVictory = checkBoardPlane(board, player);
            } else if (line == lookForCoordinate) {
                isVictory = checkLinePlane(line, player);
            } else if (column == lookForCoordinate) {
                isVictory = checkColumnPlane(column, player);
            }

        } else if (moveType == MoveType.FACE_CENTER) {
            if (board != lookForCoordinate) {
                isVictory = checkBoardPlane(board, player);
            } else if (line != lookForCoordinate) {
                isVictory = checkLinePlane(line, player);
            } else if (column != lookForCoordinate) {
                isVictory = checkColumnPlane(column, player);
            }
        } else if (moveType == MoveType.VERTEX) {
            isVictory = checkBoardPlane(board, player) ||
                    checkLinePlane(line, player) ||
                    checkColumnPlane(column, player);
        }

        return isVictory;
    }

    private boolean checkBoardPlane(int board, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (this.cells[board][i][i] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[board][i][BOARD_SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkLinePlane(int line, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (this.cells[i][line][i] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[i][line][BOARD_SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkColumnPlane(int column, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (this.cells[i][i][column] != player) {
                diagonal1Victory = false;
            }

            if (this.cells[i][BOARD_SIZE - 1 - i][column] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkWinCondition3dDiagonals(Move move) {
        final MoveType moveType = move.getMoveType();

        if (moveType != MoveType.CUBE_CENTER && moveType != MoveType.VERTEX) {
            return false;
        }

        final int player = move.getPlayer();

        final int LAST = BOARD_SIZE - 1;
        boolean diagonal1Victory = false;
        boolean diagonal2Victory = false;
        boolean diagonal3Victory = false;
        boolean diagonal4Victory = false;

        for (int i = 0; i < BOARD_SIZE; i++) {
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
        final Instruction instruction = Instruction.setTurn(this.currentPlayer);

        try {
            this.communicator.sendCommand(new Command(instruction));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void endMatch() {
        this.isGameRunning = false;

        final Instruction instruction = Instruction.endMatch(this.currentPlayer);

        try {
            this.communicator.sendCommand(new Command(instruction));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
