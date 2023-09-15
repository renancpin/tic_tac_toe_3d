package communication.socket;

import game.match.MatchInterface;
import game.match.GuestMatch;
import game.match.HostedMatch;
import game.match.MatchBuilderInterface;

public class SocketMatchBuilder implements MatchBuilderInterface {
    @Override
    public MatchInterface createMatch(boolean asHost) {
        SocketCommunicator communicator = new SocketCommunicator();
        MatchInterface match = asHost ? new HostedMatch() : new GuestMatch(communicator);

        communicator.connect(asHost, match);

        if (asHost) {
            match.join(communicator);
        }

        return match;
    }
}
