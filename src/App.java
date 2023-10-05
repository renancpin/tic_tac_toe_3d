import communication.rmi.RMIMatchBuilder;
import controllers.ControllerInterface;
import controllers.gui.GUIController;
import game.match.MatchBuilderInterface;

public class App {
    public static void main(String[] args) throws Exception {
        MatchBuilderInterface matchBuilder = new RMIMatchBuilder();
        ControllerInterface controller = new GUIController(matchBuilder);

        controller.start();
    }
}