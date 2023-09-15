package communication.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import communication.CommunicatorInterface;
import game.instructions.Instruction;
import game.instructions.InstructionConsumerInterface;
import game.instructions.InstructionType;
import game.match.MatchInterface;
import game.move.Move;
import game.move.MoveType;

public class SocketCommunicator implements Runnable, CommunicatorInterface {
    private PrintWriter outputStream = null;
    private BufferedReader inputStream = null;
    private MatchInterface match = null;
    private InstructionConsumerInterface listener = null;
    private int PORT = 3000;
    private String HOST = "";

    public SocketCommunicator() {
    }

    public SocketCommunicator(int port) {
        this.PORT = port;
    }

    public SocketCommunicator(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public void connect(boolean asHost, MatchInterface match) {
        this.match = match;

        try {
            Socket socket;

            if (asHost) {
                try (ServerSocket serverSocket = new ServerSocket(this.PORT)) {
                    socket = serverSocket.accept();
                }
            } else {
                socket = new Socket(this.HOST, this.PORT);
            }

            setIO(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIO(Socket socket) throws IOException {
        this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.outputStream = new PrintWriter(socket.getOutputStream(), true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            while (true) {
                String input = inputStream.readLine();
                char firstChar = input.charAt(0);

                if (firstChar == '0' && listener != null) {
                    Instruction instruction = deserializeInstruction(input.substring(1));

                    listener.handleInstruction(instruction);
                } else {
                    char secondChar = input.charAt(1);

                    if (Character.isAlphabetic(secondChar)) {
                        int player = Character.getNumericValue(firstChar);
                        String message = input.substring(1);

                        match.sendMessage(message, player);
                    } else {
                        Move move = deserializeMove(input);

                        match.sendMove(move);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void listen(InstructionConsumerInterface listener) {
        this.listener = listener;
    }

    @Override
    public void sendMove(Move move) {
        String serialized = serializeMove(move);
        outputStream.println(serialized);
    }

    @Override
    public void sendMessage(String message, int player) {
        String serialized = serializeMessage(message, player);
        outputStream.println(serialized);
    }

    @Override
    public void handleInstruction(Instruction instruction) {
        String serialized = serializeInstruction(instruction);
        outputStream.println(serialized);
    }

    private static String serializeMove(Move move) {
        final MoveType moveType = move.getMoveType();
        final int player = move.getPlayer();

        if (moveType == MoveType.SKIP_TURN) {
            return "" + player + (MatchInterface.BOARD_SIZE + 1);
        } else if (moveType == MoveType.SURRENDER) {
            return "" + player + (MatchInterface.BOARD_SIZE + 2);
        }

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        String serialized = "" + player + board + line + column;

        return serialized;
    }

    private String serializeMessage(String message, int player) {
        return "" + player + message;
    }

    private static String serializeInstruction(Instruction instruction) {
        final InstructionType instructionType = instruction.getType();
        final int player = instruction.getPlayer();

        String serialized = "0" + Integer.toString(instructionType.toInt()) + Integer.toString(player);

        if (instructionType == InstructionType.SET_CELL) {
            Move move = instruction.getMove();

            if (move != null) {
                serialized += serializeMove(move).substring(1);
            }
        } else if (instructionType == InstructionType.MESSAGE) {
            serialized += instruction.getMessage();
        }

        return serialized;
    }

    private static Move deserializeMove(String input) {
        final int player = Character.getNumericValue(input.charAt(0));
        final int board = Character.getNumericValue(input.charAt(1));

        if (board == MatchInterface.BOARD_SIZE + 1) {
            return Move.SkipTurn(player);
        } else if (board == MatchInterface.BOARD_SIZE + 2) {
            return Move.Surrender(player);
        }

        final int line = Character.getNumericValue(input.charAt(2));
        final int column = Character.getNumericValue(input.charAt(3));

        final Move move = new Move(player, board, line, column);

        return move;
    }

    private static Instruction deserializeInstruction(String input) {
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
                return Instruction.setCell(deserializeMove(input.substring(1)));
            case MESSAGE:
                return Instruction.message(input.substring(2), player);
            default:
                return Instruction.invalid();
        }
    }
}
