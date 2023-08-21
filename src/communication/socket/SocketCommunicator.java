package communication.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import communication.Command;
import communication.CommandType;
import communication.Communicator;
import game.Move;

public class SocketCommunicator implements Communicator {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private DataOutputStream ostream = null;
    private DataInputStream istream = null;

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            serverSocket = null;

            istream = new DataInputStream(socket.getInputStream());
            ostream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
        }
    }

    public void host(int port) {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();

            istream = new DataInputStream(socket.getInputStream());
            ostream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
        }
    }

    public void sendCommand(Command command) {
        final CommandType type = command.getType();

        try {
            switch (type) {
                case MESSAGE:
                    ostream.writeByte(0);
                    ostream.writeUTF(command.getMessage());
                    break;
                case MOVE:
                    final Move move = command.getMove();

                    final int player = move.getPlayer();
                    final int board = move.getBoard();
                    final int line = move.getLine();
                    final int column = move.getColumn();

                    final String message = String.valueOf(player) + String.valueOf(board) + String.valueOf(line)
                            + String.valueOf(column);

                    ostream.writeByte(1);
                    ostream.writeUTF(message);
                    break;
                case ERROR:
                    final Exception error = command.getException();

                    ostream.writeByte(2);
                    ostream.writeUTF(error.toString());
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Command receiveCommand() throws IOException {
        Command command = null;

        try {
            final int firstByte = istream.readByte();
            final CommandType type = CommandType.fromInt(firstByte);

            if (type == CommandType.MESSAGE) {
                final String message = istream.readUTF();

                command = new Command(message);
            } else if (type == CommandType.MOVE) {
                final int player = Character.getNumericValue(istream.readChar());
                final int board = Character.getNumericValue(istream.readChar());
                final int line = Character.getNumericValue(istream.readChar());
                final int column = Character.getNumericValue(istream.readChar());

                final Move move = new Move(player, board, line, column);

                command = new Command(move);
            } else if (type == CommandType.ERROR) {
                final String message = istream.readUTF();
                final Exception error = new Exception(message);

                command = new Command(error);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        if (command == null) {
            Exception error = new Exception("Erro não tratado");
            return new Command(error);
        }

        return command;
    }
}
