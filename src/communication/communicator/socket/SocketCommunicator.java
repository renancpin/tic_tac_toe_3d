package communication.communicator.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import communication.command.Command;
import communication.communicator.Communicator;

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
        final String serializedCommand = Command.serialize(command);

        outputStream.println(serializedCommand);
    }

    public Command receiveCommand() {
        try {
            final String input = inputStream.readLine();
            return Command.deserialize(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
