package game.match;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import communication.command.Command;
import communication.command.CommandType;
import communication.communicator.Communicator;
import game.exceptions.*;
import game.instructions.*;
import game.move.*;

public class HostedMatch extends Match implements Runnable {
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private boolean isGameRunning = false;
    private int currentPlayer = 1;
    private final int thisPlayer;
    private final Communicator communicator;
    private final Set<Consumer<Command>> consumers = new HashSet<>();

    public HostedMatch(Communicator communicator) {
        this.thisPlayer = (new Random()).nextInt(1, 2);
        this.communicator = communicator;
    }

    private void notify(Command command) {
        for (Consumer<Command> consumer : consumers) {
            consumer.accept(command);
        }
    }

    @Override
    public void listen(Consumer<Command> listener) {
        consumers.add(listener);
    }

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        communicator.host();

        int guest = getNextPlayer(thisPlayer);
        Command command = new Command(Instruction.setGuest(guest));

        notify(command);
        communicator.sendCommand(command);

        this.isGameRunning = true;

        while (isGameRunning) {
            command = communicator.receiveCommand();

            if (command == null) {
                break;
            }

            CommandType commandType = command.getType();

            switch (commandType) {
                case MESSAGE:
                    String message = command.getMessage();
                    notify(new Command("[GUEST] " + message));
                    break;
                case MOVE:
                    Move move = command.getMove();
                    handleMove(move);
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public boolean getIsRunning() {
        return this.isGameRunning;
    }

    @Override
    public int getThisPlayer() {
        return this.thisPlayer;
    }

    @Override
    public int getCurrentPlayer() {
        return this.currentPlayer;
    }

    @Override
    public int getCell(int board, int line, int column) {
        return this.cells[board][line][column];
    }

    @Override
    public void handleMessage(String message) {
        Command command = new Command(message);

        communicator.sendCommand(command);
    }

    @Override
    public void handleMove(Move move) {
        try {
            assertLegalMove(move);
        } catch (InvalidMoveException exception) {
            handleMessage("[ERROR] " + exception.toString());
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

    private void assertLegalMove(Move move) throws InvalidMoveException {
        if (!isGameRunning) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.GAME_OVER, move);
        }

        final int player = move.getPlayer();

        if (player != currentPlayer) {
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
        if (cells[board][line][column] != 0) {
            throw new InvalidMoveException(InvalidMoveExceptionMotive.NON_EMPTY, move);
        }
    }

    private void registerMove(Move move) {
        final int player = move.getPlayer();

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        cells[board][line][column] = player;

        Instruction instruction = Instruction.setCell(move);

        Command command = new Command(instruction);

        notify(command);
        communicator.sendCommand(command);
    }

    private void endTurn() {
        currentPlayer = getNextPlayer(currentPlayer);
        final Instruction instruction = Instruction.setTurn(currentPlayer);

        Command command = new Command(instruction);

        notify(command);
        communicator.sendCommand(command);
    }

    private void endMatch() {
        isGameRunning = false;

        final Instruction instruction = Instruction.endMatch(currentPlayer);

        Command command = new Command(instruction);

        notify(command);
        communicator.sendCommand(command);
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
            if (cells[i][line][column] != player) {
                perpendicularVictory = false;
            }

            if (cells[board][i][column] != player) {
                lineVictory = false;
            }

            if (cells[board][line][i] != player) {
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
            if (cells[board][i][i] != player) {
                diagonal1Victory = false;
            }

            if (cells[board][i][BOARD_SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkLinePlane(int line, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (cells[i][line][i] != player) {
                diagonal1Victory = false;
            }

            if (cells[i][line][BOARD_SIZE - 1 - i] != player) {
                diagonal2Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory);
    }

    private boolean checkColumnPlane(int column, int player) {
        boolean diagonal1Victory = true;
        boolean diagonal2Victory = true;

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (cells[i][i][column] != player) {
                diagonal1Victory = false;
            }

            if (cells[i][BOARD_SIZE - 1 - i][column] != player) {
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
            if (cells[i][i][i] != player) {
                diagonal1Victory = false;
            }

            // [0][0][LAST] to [LAST][LAST][0]
            if (cells[i][i][LAST - i] != player) {
                diagonal2Victory = false;
            }

            // [0][LAST][0] to [LAST][0][LAST]
            if (cells[i][LAST - i][i] != player) {
                diagonal3Victory = false;
            }

            // [0][LAST][LAST] to [LAST][0][0]
            if (cells[i][LAST - i][LAST - i] != player) {
                diagonal4Victory = false;
            }
        }

        return (diagonal1Victory || diagonal2Victory || diagonal3Victory || diagonal4Victory);
    }
}
