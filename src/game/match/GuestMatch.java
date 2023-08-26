package game.match;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import communication.command.Command;
import communication.command.CommandType;
import communication.communicator.Communicator;
import game.instructions.*;
import game.move.Move;

public class GuestMatch extends Match implements Runnable {
    private int[][][] cells = new int[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
    private boolean isGameRunning = false;
    private int currentPlayer = 1;
    private int thisPlayer;
    private final Communicator communicator;
    private Set<Consumer<Command>> consumers = new HashSet<>();

    public GuestMatch(Communicator communicator) {
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

    private void registerMove(Move move) {
        final int player = move.getPlayer();
        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        cells[board][line][column] = player;
    }

    private void handleInstruction(Instruction instruction) {
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
                registerMove(instruction.getMove());
                break;
            case INVALID:
                notify(new Command("Instrucao invalida"));
                return;
        }

        notify(new Command(instruction));
    }

    @Override
    public void handleMessage(String message) {
        Command command = new Command(message);

        communicator.sendCommand(command);
    }

    @Override
    public void handleMove(Move move) {
        Command command = new Command(move);

        communicator.sendCommand(command);
    }

    @Override
    public void run() {
        communicator.connect();

        Command command = null;
        CommandType commandType;

        try {
            command = communicator.receiveCommand();
        } catch (Exception e) {
        }

        if (command != null && command.getType() == CommandType.INSTRUCTION) {
            handleInstruction(command.getInstruction());
        }

        isGameRunning = true;

        while (isGameRunning) {
            command = communicator.receiveCommand();

            if (command == null) {
                break;
            }

            commandType = command.getType();

            switch (commandType) {
                case MESSAGE:
                    String message = command.getMessage();
                    notify(new Command("[HOST] " + message));
                    break;
                case INSTRUCTION:
                    Instruction instruction = command.getInstruction();
                    handleInstruction(instruction);
                    break;
                default:
                    break;
            }
        }
    }
}
