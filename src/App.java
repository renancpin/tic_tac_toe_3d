import communication.Communicator;
import communication.socket.SocketCommunicator;
import controllers.keyboard.KeyboardController;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Bem vindo");
        Communicator comm = new SocketCommunicator();
        new KeyboardController(comm);
    }
}
