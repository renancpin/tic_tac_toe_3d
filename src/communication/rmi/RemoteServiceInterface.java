package communication.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import communication.CommunicatorInterface;
import game.instructions.InstructionConsumerInterface;
import game.move.Move;

public interface RemoteServiceInterface extends Remote, CommunicatorInterface {
    @Override
    public void sendMessage(String message, int player) throws RemoteException;

    @Override
    public void sendMove(Move move) throws RemoteException;

    @Override
    public void listen(InstructionConsumerInterface listener) throws RemoteException;
}
