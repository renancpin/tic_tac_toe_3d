package controllers.keyboard;

import java.util.Scanner;

import communication.command.Command;
import communication.command.CommandType;
import controllers.Controller;
import game.instructions.InstructionType;
import game.match.Match;
import game.match.MatchBuilder;
import game.move.Move;

public class KeyboardController implements Controller {
    private MatchBuilder matchBuilder;
    private Match match;
    private Scanner console = new Scanner(System.in);

    public KeyboardController(MatchBuilder matchBuilder) {
        this.matchBuilder = matchBuilder;
    }

    public void start() {
        System.out.println("Bem vindo!");
        System.out.println("Deseja criar um jogo(1) ou conectar a um(2)?: ");
        String tipoPartida = console.nextLine();
        boolean isHost = tipoPartida.equals("1");

        match = matchBuilder.createMatch(isHost);
        match.listen(this::handleUpdate);
        match.start();

        handleInteraction();

        console.close();
    }

    private void handleMessage(String input) {
        match.handleMessage(input);
    }

    private void handleMove(String input) {
        int player = match.getThisPlayer();
        Move move = null;

        if (input.charAt(0) == '/') {

            if (input.equals("/ff")) {
                move = Move.Surrender(player);
            } else if (input.equals("/skip")) {
                move = Move.SkipTurn(player);
            } else {
                System.out.println("Movimento invalido");
            }

            if (move != null) {
                match.handleMove(move);
            }

            return;
        }

        int board = Character.getNumericValue(input.charAt(0));
        int line = Character.getNumericValue(input.charAt(1));
        int column = Character.getNumericValue(input.charAt(2));

        move = new Move(player, board, line, column);

        match.handleMove(move);
    }

    private void printCells() {
        final int currentPlayer = match.getCurrentPlayer();

        System.out.print('\n');
        for (int line = 0; line < Match.BOARD_SIZE; line++) {
            for (int board = 0; board < Match.BOARD_SIZE; board++) {
                for (int column = 0; column < Match.BOARD_SIZE; column++) {
                    System.out.print(match.getCell(board, line, column));
                }
                System.out.print(' ');
            }
            System.out.print('\n');
        }
        System.out.println("Jogador atual: " + currentPlayer);
    }

    private void promptInteraction() {
        int thisPlayer = match.getThisPlayer();
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

            if (Character.isDigit(input.charAt(0)) || input.charAt(0) == '/') {
                handleMove(input);
            } else {
                handleMessage(input);
            }

            promptInteraction();
        }
    }

    private void handleUpdate(Command command) {
        CommandType commandType = command.getType();

        switch (commandType) {
            case MOVE:
                System.out.println("Controller nao processa movimentos");
                promptInteraction();
                break;
            case MESSAGE:
                System.out.println("\033[2K\r" + command.getMessage());
                promptInteraction();
                break;
            case INSTRUCTION:
                InstructionType instructionType = command.getInstruction().getType();

                switch (instructionType) {
                    case SET_GUEST:
                        System.out.println("Você é o jogador " + match.getThisPlayer() + "!");
                        printCells();
                        break;
                    case SET_TURN:
                        int thisPlayer = match.getThisPlayer();
                        int currentPlayer = match.getCurrentPlayer();
                        System.out.println(thisPlayer == currentPlayer ? "Sua vez" : "Aguarde sua vez");
                        break;
                    case END_MATCH:
                        System.out.println("Partida encerrada!");
                        System.out.println("Vitória do jogador " + match.getCurrentPlayer());
                        console.close();
                        return;
                    default:
                        break;
                }

                promptInteraction();
                break;
        }
    }
}
