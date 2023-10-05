package communication.rmi;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import game.match.GuestMatch;
import game.match.HostedMatch;
import game.match.MatchBuilderInterface;
import game.match.MatchInterface;

public class RMIMatchBuilder implements MatchBuilderInterface {
    private final String HOST = "rmi://localhost:";
    private final int SERVER_PORT = 3000;
    private final String SERVER_ADDRESS = HOST + SERVER_PORT + "/TicTacToe3D";
    private final int CLIENT_PORT = 3001;
    private final String CLIENT_ADDRESS = HOST + CLIENT_PORT + "/TicTacToe3DClient";

    @Override
    public MatchInterface createMatch(boolean asHost) {
        try {
            RMICommunicator thisCommunicator = createCommunicator(asHost);
            RMICommunicatorInterface otherCommunicator = getCommunicator(!asHost);

            MatchInterface match = asHost ? new HostedMatch() : new GuestMatch(thisCommunicator);

            if (asHost) {
                thisCommunicator.setCallableObject(match);
                match.join(thisCommunicator);
                thisCommunicator.listen(otherCommunicator);
            } else {
                thisCommunicator.setCallableObject(otherCommunicator);
            }

            return match;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private RMICommunicator createCommunicator(boolean asHost) {
        final int port = asHost ? SERVER_PORT : CLIENT_PORT;
        final String address = asHost ? SERVER_ADDRESS : CLIENT_ADDRESS;

        Registry registry = null;
        RMICommunicator communicator = null;

        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (registry == null) {
                registry = LocateRegistry.getRegistry(port);
            }
            communicator = new RMICommunicator();

            Remote object = UnicastRemoteObject.exportObject(communicator, port);

            registry.bind(address, object);

            return communicator;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private RMICommunicatorInterface getCommunicator(boolean asClient) {
        final int port = asClient ? SERVER_PORT : CLIENT_PORT;
        final String address = asClient ? SERVER_ADDRESS : CLIENT_ADDRESS;

        Registry registry = null;
        RMICommunicatorInterface communicator = null;

        try {
            registry = LocateRegistry.getRegistry(port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (communicator == null) {
            try {
                communicator = (RMICommunicatorInterface) registry.lookup(address);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return communicator;
    }
}
