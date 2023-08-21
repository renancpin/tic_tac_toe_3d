package communication;

import java.io.IOException;

public interface Communicator {
    public void connect(String host, int port);

    public void host(int port);

    public void sendCommand(Command command) throws IOException;

    public Command receiveCommand() throws IOException;
}
