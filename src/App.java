import communication.socket.SocketMatchBuilder;
import controllers.ControllerInterface;
import controllers.gui.GUIController;
import game.match.MatchBuilderInterface;

public class App {
    public static void main(String[] args) throws Exception {
        MatchBuilderInterface matchBuilder = new SocketMatchBuilder();
        ControllerInterface controller = new GUIController(matchBuilder);

        controller.start();
    }
}