package communication;

import game.instructions.InstructionConsumerInterface;
import game.move.Move;

public interface CommunicatorInterface extends InstructionConsumerInterface {
    public void sendMessage(String message, int player) throws Exception;

    public void sendMove(Move move) throws Exception;

    public void listen(InstructionConsumerInterface listener) throws Exception;
}
