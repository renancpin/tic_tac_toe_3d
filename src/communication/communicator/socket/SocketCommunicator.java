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
import game.move.Move;

public class SocketCommunicator implements Communicator {
    private PrintWriter outputStream = null;
    private BufferedReader inputStream = null;

    private void setIO(Socket socket) throws IOException {
        this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outputStream = new PrintWriter(socket.getOutputStream(), true);
    }

    public void connect(String host, int port) {
        try {
            final Socket socket = new Socket(host, port);

            setIO(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void host(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
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

        try {
            String serializedCommandType = Integer.toString(commandType.toInt());
            outputStream.println(serializedCommandType + serializedCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Command receiveCommand() throws IOException {
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
            e.printStackTrace();
        }

        throw new IOException("Erro não tratado");
    }

    private static String serializeMove(Move move) {
        final int player = move.getPlayer();
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
