import communication.communicator.Communicator;
import communication.communicator.socket.SocketCommunicator;
import controllers.Controller;
import controllers.keyboard.KeyboardController;

public class App {
    public static void main(String[] args) throws Exception {
        Communicator communicator = new SocketCommunicator();
        Controller controller = new KeyboardController(communicator);

        controller.start();
    }
}
