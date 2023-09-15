package communication.command;

import game.instructions.Instruction;
import game.instructions.InstructionType;
import game.match.Match;
import game.move.Move;
import game.move.MoveType;

public class Command {
    private String message;
    private Move move;
    private Instruction instruction;
    private CommandType type;

    public Command(String message) {
        this.type = CommandType.MESSAGE;
        this.message = message;
    }

    public Command(Move move) {
        this.type = CommandType.MOVE;
        this.move = move;
    }

    public Command(Instruction instruction) {
        this.type = CommandType.INSTRUCTION;
        this.instruction = instruction;
    }

    public CommandType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public Move getMove() {
        return this.move;
    }

    public Instruction getInstruction() {
        return this.instruction;
    }

    public static String serialize(Command command) {
        final CommandType commandType = command.getType();
        String serializedCommandType = Integer.toString(commandType.toInt());

        switch (commandType) {
            case MOVE:
                return serializedCommandType + serializeMove(command.getMove());
            case INSTRUCTION:
                return serializedCommandType + serializeInstruction(command.getInstruction());
            case MESSAGE:
                return serializedCommandType + command.getMessage();
            default:
                return "0null";
        }
    }

    private static String serializeMove(Move move) {
        final MoveType moveType = move.getMoveType();
        final int player = move.getPlayer();

        if (moveType == MoveType.SKIP_TURN) {
            return "" + player + (Match.BOARD_SIZE + 1);
        } else if (moveType == MoveType.SURRENDER) {
            return "" + player + (Match.BOARD_SIZE + 2);
        }

        final int board = move.getBoard();
        final int line = move.getLine();
        final int column = move.getColumn();

        String serialized = "" + player + board + line + column;

        return serialized;
    }

    private static String serializeInstruction(Instruction instruction) {
        final InstructionType instructionType = instruction.getType();
        final int player = instruction.getPlayer();

        String serialized = Integer.toString(instructionType.toInt()) + Integer.toString(player);

        if (instructionType == InstructionType.SET_CELL) {
            Move move = instruction.getMove();

            if (move != null) {
                serialized += serializeMove(move).substring(1);
            }
        }

        return serialized;
    }

    public static Command deserialize(String input) {
        final char firstChar = input.charAt(0);
        final CommandType commandType = CommandType.fromChar(firstChar);

        final String message = input.substring(1);

        switch (commandType) {
            case MESSAGE:
                return new Command(message);
            case MOVE:
                return new Command(deserializeMove(message));
            case INSTRUCTION:
                return new Command(deserializeInstruction(message));
            default:
                return null;
        }
    }

    private static Move deserializeMove(String input) {
        final int player = Character.getNumericValue(input.charAt(0));
        final int board = Character.getNumericValue(input.charAt(1));

        if (board == Match.BOARD_SIZE + 1) {
            return Move.SkipTurn(player);
        } else if (board == Match.BOARD_SIZE + 2) {
            return Move.Surrender(player);
        }

        final int line = Character.getNumericValue(input.charAt(2));
        final int column = Character.getNumericValue(input.charAt(3));

        final Move move = new Move(player, board, line, column);

        return move;
    }

    private static Instruction deserializeInstruction(String input) {
        final int type = Character.getNumericValue(input.charAt(0));
        final int player = Character.getNumericValue(input.charAt(1));

        final InstructionType instructionType = InstructionType.fromInt(type);

        switch (instructionType) {
            case SET_GUEST:
                return Instruction.setGuest(player);
            case SET_TURN:
                return Instruction.setTurn(player);
            case END_MATCH:
                return Instruction.endMatch(player);
            case SET_CELL:
                return Instruction.setCell(deserializeMove(input.substring(1)));
            default:
                return Instruction.invalid();
        }
    }
}
