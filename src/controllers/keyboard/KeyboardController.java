package controllers.keyboard;

import java.util.Scanner;

import controllers.ControllerInterface;
import game.instructions.Instruction;
import game.instructions.InstructionType;
import game.match.MatchInterface;
import game.match.MatchBuilderInterface;
import game.move.Move;

public class KeyboardController implements ControllerInterface {
    private MatchBuilderInterface matchBuilder;
    private MatchInterface match;
    private Scanner console = new Scanner(System.in);
    private int thisPlayer;

    public KeyboardController(MatchBuilderInterface matchBuilder) {
        this.matchBuilder = matchBuilder;
    }

    public void start() {
        System.out.println("Bem vindo!");
        System.out.println("Deseja criar um jogo(1) ou conectar a um(2)?: ");
        String tipoPartida = console.nextLine();
        boolean isHost = tipoPartida.equals("1");

        match = matchBuilder.createMatch(isHost);
        match.join(this);
        match.start();

        handleInteraction();

        console.close();
    }

    private void handleMessage(String input) {
        match.sendMessage(input, thisPlayer);
    }

    private void handleMove(String input) {
        Move move = null;

        if (input.charAt(0) == '/') {

            if (input.equals("/ff")) {
                move = Move.Surrender(thisPlayer);
            } else if (input.equals("/skip")) {
                move = Move.SkipTurn(thisPlayer);
            } else {
                System.out.println("Movimento invalido");
            }

            if (move != null) {
                match.sendMove(move);
            }

            return;
        }

        int board = Character.getNumericValue(input.charAt(0));
        int line = Character.getNumericValue(input.charAt(1));
        int column = Character.getNumericValue(input.charAt(2));

        move = new Move(thisPlayer, board, line, column);

        match.sendMove(move);
    }

    private void printCells() {
        final int currentPlayer = match.getCurrentPlayer();

        System.out.print('\n');
        for (int line = 0; line < MatchInterface.BOARD_SIZE; line++) {
            for (int board = 0; board < MatchInterface.BOARD_SIZE; board++) {
                for (int column = 0; column < MatchInterface.BOARD_SIZE; column++) {
                    System.out.print(match.getCell(board, line, column));
                }
                System.out.print(' ');
            }
            System.out.print('\n');
        }
        System.out.println("Jogador atual: " + currentPlayer);
    }

    private void promptInteraction() {
        int currentPlayer = match.getCurrentPlayer();

        String options = thisPlayer == currentPlayer ? "um movimento ou mensagem" : "uma mensagem";
        String prompt = "[" + thisPlayer + "] Digite " + options + ": ";

        System.out.print(prompt);
    }

    private void handleInteraction() {
        System.out.println("Aguardando outro jogador...");

        while (!match.getIsRunning()) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        }

        while (match.getIsRunning()) {
            String input;

            try {
                input = console.nextLine();
            } catch (Exception e) {
                System.out.println("\nErro");
                break;
            }

            if (input.length() > 0 && (Character.isDigit(input.charAt(0)) || input.charAt(0) == '/')) {
                handleMove(input);
            } else {
                handleMessage(input);
            }

            promptInteraction();
        }
    }

    @Override
    public void handleInstruction(Instruction instruction) {
        InstructionType instructionType = instruction.getType();

        switch (instructionType) {
            case SET_GUEST:
                System.out.println("Você é o jogador " + instruction.getPlayer() + "!");
                printCells();
                break;
            case SET_TURN:
                int currentPlayer = match.getCurrentPlayer();
                System.out.println(thisPlayer == currentPlayer ? "Sua vez" : "Aguarde sua vez");
                promptInteraction();
                break;
            case SET_CELL:
                printCells();
                promptInteraction();
                break;
            case MESSAGE:
                System.out.println("\033[2K\r" + "[" + instruction.getPlayer() + "] " + instruction.getMessage());
                promptInteraction();
                break;
            case END_MATCH:
                System.out.println("Partida encerrada!");
                System.out.println("Vitória do jogador " + match.getCurrentPlayer());
                console.close();
                return;
            default:
                break;

        }
    }
}
