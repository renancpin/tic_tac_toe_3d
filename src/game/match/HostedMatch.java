package game.match;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import game.exceptions.*;
import game.instructions.*;
import game.move.*;

public class HostedMatch implements MatchInterface {
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private boolean isGameRunning = false;
    private int currentPlayer = 1;
    private int playCount = BOARD_SIZE * BOARD_SIZE * BOARD_SIZE;
    private Map<Integer, InstructionConsumerInterface> players = new HashMap<>();

    public HostedMatch() {
        currentPlayer = (new Random()).nextInt(1, 3);
    }

    private void updateAllExcept(int exceptPlayer, Instruction instruction) {
        for (Entry<Integer, InstructionConsumerInterface> entry : players.entrySet()) {
            InstructionConsumerInterface listener = entry.getValue();

            if (exceptPlayer == 0 || entry.getKey() != exceptPlayer) {
                try {
                    listener.handleInstruction(instruction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateAll(Instruction instruction) {
        updateAllExcept(0, instruction);
    }

    private int getNextPlayer() {
        return currentPlayer == PLAYER_LIMIT ? 1 : currentPlayer + 1;
    }

    @Override
    public void join(InstructionConsumerInterface listener) {
        if (!isGameRunning && players.size() < PLAYER_LIMIT) {
            int thisPlayer = currentPlayer;

            players.put(thisPlayer, listener);

            currentPlayer = getNextPlayer();
        }
    }

    @Override
    public void start() {
        while (players.size() < PLAYER_LIMIT) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Entry<Integer, InstructionConsumerInterface> player : players.entrySet()) {
            InstructionConsumerInterface listener = player.getValue();
            int id = player.getKey();

            try {
                listener.handleInstruction(Instruction.setGuest(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentPlayer = 1;
        isGameRunning = true;
    }

    @Override
    public boolean getIsRunning() {
        return this.isGameRunning;
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
    public void sendMessage(String message, int player) {
        Instruction instruction = Instruction.message(message, player);

        updateAllExcept(player, instruction);
    }

    @Override
    public void sendMove(Move move) {
        try {
            assertLegalMove(move);
        } catch (InvalidMoveException exception) {
            sendMessage("[ERROR] " + exception.toString(), 0);
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

        if (playCount <= 0) {
            currentPlayer = 0;
            isVictory = true;
        }

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

        MoveType moveType = move.getMoveType();

        if (moveType == MoveType.SKIP_TURN || moveType == MoveType.SURRENDER) {
            return;
        }

        move.setMoveType(BOARD_SIZE);

        moveType = move.getMoveType();

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

        playCount--;

        updateAll(Instruction.setCell(move));
    }

    private void endTurn() {
        currentPlayer = getNextPlayer();

        updateAll(Instruction.setTurn(currentPlayer));
    }

    private void endMatch() {
        isGameRunning = false;

        int winner = currentPlayer;
        currentPlayer = 0;

        updateAll(Instruction.endMatch(winner));
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
        boolean diagonal2Victory = true;
        boolean diagonal3Victory = true;
        boolean diagonal4Victory = true;
        boolean diagonal1Victory = true;

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
