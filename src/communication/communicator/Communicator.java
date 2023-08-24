package communication.communicator;

import communication.command.Command;

public interface Communicator {
    public static final int PORT = 3000;
    public static final String HOST = "";

    public void connect();

    public void host();

    public void sendCommand(Command command);

    public Command receiveCommand();
}
