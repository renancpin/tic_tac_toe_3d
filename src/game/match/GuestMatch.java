package game.match;

import communication.CommunicatorInterface;
import game.instructions.*;
import game.move.Move;

public class GuestMatch implements MatchInterface, InstructionConsumerInterface {
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private boolean isGameRunning = false;
    private int currentPlayer = 1;
    private InstructionConsumerInterface listener;
    private CommunicatorInterface communicator;

    public GuestMatch(CommunicatorInterface communicator) {
        this.communicator = communicator;

        try {
            communicator.listen(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void join(InstructionConsumerInterface listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
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

    private void registerMove(Move move) {
        final int player = move.getPlayer();
        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        cells[board][line][column] = player;
    }

    @Override
    public void sendMessage(String message, int player) {
        try {
            communicator.sendMessage(message, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMove(Move move) {
        try {
            communicator.sendMove(move);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleInstruction(Instruction instruction) {
        InstructionType instructionType = instruction.getType();

        switch (instructionType) {
            case SET_GUEST:
                this.isGameRunning = true;
                break;
            case SET_TURN:
                this.currentPlayer = instruction.getPlayer();
                break;
            case END_MATCH:
                this.currentPlayer = 0;
                this.isGameRunning = false;
                break;
            case SET_CELL:
                registerMove(instruction.getMove());
                break;
            case INVALID:
                try {
                    listener.handleInstruction(Instruction.message("Instrucao invalida", 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            default:
                break;
        }

        try {
            listener.handleInstruction(instruction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
