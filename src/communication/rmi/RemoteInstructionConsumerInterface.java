package communication.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import game.instructions.Instruction;
import game.instructions.InstructionConsumerInterface;

public interface RemoteInstructionConsumerInterface extends Remote, InstructionConsumerInterface {
    @Override
    public void handleInstruction(Instruction instruction) throws RemoteException;
}
