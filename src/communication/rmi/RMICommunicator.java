package communication.rmi;

import java.rmi.RemoteException;

import communication.CommunicatorInterface;
import game.instructions.Instruction;
import game.instructions.InstructionConsumerInterface;
import game.match.MatchInterface;
import game.move.Move;

public class RMICommunicator implements RMICommunicatorInterface {
    private Server server = null;
    private InstructionConsumerInterface client = null;

    public RMICommunicator() throws RemoteException {
    }

    public void setCallableObject(MatchInterface match) {
        this.server = new Server(match);
    }

    public void setCallableObject(CommunicatorInterface communicator) {
        this.server = new Server(communicator);
    }

    @Override
    public void sendMessage(String message, int player) {
        server.sendMessage(message, player);
    }

    @Override
    public void sendMove(Move move) {
        server.sendMove(move);
    }

    @Override
    public void listen(InstructionConsumerInterface listener) {
        this.client = listener;
    }

    @Override
    public void handleInstruction(Instruction instruction) {
        try {
            client.handleInstruction(instruction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Server {
        MatchInterface match = null;
        CommunicatorInterface communicator = null;

        private Server(MatchInterface match) {
            this.match = match;
        }

        private Server(CommunicatorInterface communicator) {
            this.communicator = communicator;
        }

        public void sendMessage(String message, int player) {
            try {
                if (this.match != null) {
                    this.match.sendMessage(message, player);
                } else {
                    this.communicator.sendMessage(message, player);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendMove(Move move) {
            try {
                if (this.match != null) {
                    this.match.sendMove(move);
                } else {
                    this.communicator.sendMove(move);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
