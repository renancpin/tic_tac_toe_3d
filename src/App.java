import communication.communicator.Communicator;
import communication.communicator.socket.SocketCommunicator;
import controllers.Controller;
import controllers.gui.GUIController;
import game.match.Game;
import game.match.MatchBuilder;

public class App {
    public static void main(String[] args) throws Exception {
        Communicator communicator = new SocketCommunicator();
        MatchBuilder game = new Game(communicator);
        Controller controller = new GUIController(game);

        controller.start();
    }
}
