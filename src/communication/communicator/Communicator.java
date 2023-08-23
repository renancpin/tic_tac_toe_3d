package communication.communicator;

import java.io.IOException;

import communication.command.Command;

public interface Communicator {
    public void connect(String host, int port);

    public void host(int port);

    public void sendCommand(Command command) throws IOException;

    public Command receiveCommand() throws IOException;
}
