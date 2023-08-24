package game.match;

import communication.communicator.Communicator;

public class Game implements MatchBuilder {
    private Communicator communicator;

    public Game(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public Match createMatch(boolean asHost) {
        return asHost ? new HostedMatch(communicator) : new GuestMatch(communicator);
    }
}
