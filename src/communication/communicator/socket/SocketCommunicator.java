package communication.communicator.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import communication.command.Command;
import communication.command.CommandType;
import communication.communicator.Communicator;
import game.instructions.Instruction;
import game.instructions.InstructionType;
import game.match.Match;
import game.move.Move;
import game.move.MoveType;

public class SocketCommunicator implements Communicator {
    private PrintWriter outputStream = null;
    private BufferedReader inputStream = null;
    private final int PORT;
    private final String HOST;

    public SocketCommunicator() {
        this.PORT = Communicator.PORT;
        this.HOST = Communicator.HOST;
    }

    public SocketCommunicator(int port) {
        this.HOST = Communicator.HOST;
        this.PORT = port;
    }

    public SocketCommunicator(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    private void setIO(Socket socket) throws IOException {
        this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outputStream = new PrintWriter(socket.getOutputStream(), true);
    }

    public void connect() {
        try {
            final Socket socket = new Socket(this.HOST, this.PORT);

            setIO(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void host() {
        try (final ServerSocket serverSocket = new ServerSocket(this.PORT)) {
            final Socket socket = serverSocket.accept();

            setIO(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(Command command) {
        final CommandType commandType = command.getType();
        final String serializedCommand;

        switch (commandType) {
            case MESSAGE:
                serializedCommand = command.getMessage();
                break;
            case MOVE:
                final Move move = command.getMove();
                serializedCommand = serializeMove(move);
                break;
            case INSTRUCTION:
                final Instruction instruction = command.getInstruction();
                serializedCommand = serializeInstruction(instruction);
                break;
            default:
                serializedCommand = "";
        }

        String serializedCommandType = Integer.toString(commandType.toInt());
        outputStream.println(serializedCommandType + serializedCommand);
    }

    public Command receiveCommand() {
        try {
            final String input = inputStream.readLine();
            final char firstChar = input.charAt(0);
            final CommandType commandType = CommandType.fromChar(firstChar);

            final String message = input.substring(1);

            switch (commandType) {
                case MESSAGE:
                    return new Command(message);
                case MOVE:
                    return new Command(parseMove(message));
                case INSTRUCTION:
                    return new Command(parseInstruction(message));
            }
        } catch (Exception e) {
        }

        return null;
    }

    private static String serializeMove(Move move) {
        final MoveType moveType = move.getMoveType();
        final int player = move.getPlayer();

        if (moveType == MoveType.SKIP_TURN) {
            return "" + player + (Match.BOARD_SIZE + 1);
        } else if (moveType == MoveType.SURRENDER) {
            return "" + player + (Match.BOARD_SIZE + 2);
        }

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        String serialized = "" + player + board + line + column;

        return serialized;
    }

    private static String serializeInstruction(Instruction instruction) {
        final InstructionType instructionType = instruction.getType();
        final int player = instruction.getPlayer();

        String serialized = Integer.toString(instructionType.toInt()) + Integer.toString(player);

        if (instructionType == InstructionType.SET_CELL) {
            Move move = instruction.getMove();

            if (move != null) {
                serialized += serializeMove(move).substring(1);
            }
        }

        return serialized;
    }

    private static Move parseMove(String input) {
        final int player = Character.getNumericValue(input.charAt(0));
        final int board = Character.getNumericValue(input.charAt(1));

        if (board == Match.BOARD_SIZE + 1) {
            return Move.SkipTurn(player);
        } else if (board == Match.BOARD_SIZE + 2) {
            return Move.Surrender(player);
        }

        final int line = Character.getNumericValue(input.charAt(2));
        final int column = Character.getNumericValue(input.charAt(3));

        final Move move = new Move(player, board, line, column);

        return move;
    }

    private static Instruction parseInstruction(String input) {
        final int type = Character.getNumericValue(input.charAt(0));
        final int player = Character.getNumericValue(input.charAt(1));

        final InstructionType instructionType = InstructionType.fromInt(type);

        switch (instructionType) {
            case SET_GUEST:
                return Instruction.setGuest(player);
            case SET_TURN:
                return Instruction.setTurn(player);
            case END_MATCH:
                return Instruction.endMatch(player);
            case SET_CELL:
                return Instruction.setCell(parseMove(input.substring(1)));
            default:
                return Instruction.invalid();
        }
    }
}
