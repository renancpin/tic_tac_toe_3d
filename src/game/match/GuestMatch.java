package game.match;

import communication.command.Command;
import communication.communicator.Communicator;
import game.instructions.Instruction;
import game.instructions.InstructionType;
import game.move.Move;

public class GuestMatch implements Match {
    private int currentPlayer = 1;
    private boolean isGameRunning = true;
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];

    private int thisPlayer;
    private final Communicator communicator;

    public GuestMatch(Communicator communicator, int port) {
        this.communicator = communicator;

        communicator.connect("", port);
    }

    public void handleMessage(String message) {
        Command command = new Command("[GUEST]" + message);

        try {
            this.communicator.sendCommand(command);
        } catch (Exception e) {
        }
    }

    public void handleMove(Move move) {
        Command command = new Command(move);

        try {
            this.communicator.sendCommand(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleInstruction(Instruction instruction) {
        final InstructionType instructionType = instruction.getType();

        switch (instructionType) {
            case SET_GUEST:
                this.thisPlayer = instruction.getPlayer();
                break;
            case SET_TURN:
                this.currentPlayer = instruction.getPlayer();
                break;
            case END_MATCH:
                this.currentPlayer = instruction.getPlayer();
                this.isGameRunning = false;
                break;
            case SET_CELL:
                this.registerMove(instruction.getMove());
                break;
            case INVALID:
                System.out.println("Instrucao invalida");
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

    private void registerMove(Move move) {
        final int player = move.getPlayer();
        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        this.cells[board][line][column] = player;
    }
}
