package controllers.keyboard;

import java.util.Scanner;

import communication.command.Command;
import communication.command.CommandType;
import communication.communicator.Communicator;
import controllers.Controller;
import game.instructions.Instruction;
import game.match.GuestMatch;
import game.match.HostedMatch;
import game.match.Match;
import game.move.Move;

public class KeyboardController implements Controller {
    private Communicator communicator;
    private Match match;
    private Thread receiver;
    private static final int PORT = 3000;

    public KeyboardController(Communicator communicator) {
        this.communicator = communicator;
    }

    public void start() {
        Scanner console = new Scanner(System.in);

        System.out.println("Bem vindo!");
        System.out.println("Deseja criar um jogo(1) ou conectar a um(2)?: ");
        String tipo = console.nextLine();
        System.out.println("Aguardando outro jogador...");
        if (tipo.equals("1")) {
            this.match = new HostedMatch(this.communicator, PORT);
        } else {
            this.match = new GuestMatch(this.communicator, PORT);
        }

        this.receiver = new Thread(this);
        receiver.start();

        while (match.getIsRunning()) {
            printCells();
            final int player = this.match.getThisPlayer();
            final int currentPlayer = this.match.getCurrentPlayer();

            if (player == currentPlayer) {
                System.out.println("[" + this.match.getThisPlayer() + "] Digite um movimento ou mensagem: ");
            } else {
                System.out.println("[" + this.match.getThisPlayer() + "] Digite uma mensagem: ");
            }

            String input = console.nextLine();

            if (Character.isDigit(input.charAt(0)) || input.charAt(0) == '/') {
                parseMove(input);
            } else {
                parseMessage(input);
            }
        }

        System.out.println("Partida encerrada");

        console.close();
    }

    public void run() {
        try {
            while (this.match.getIsRunning()) {
                Command command = this.communicator.receiveCommand();
                CommandType type = command.getType();

                if (type == CommandType.MESSAGE) {
                    String message = command.getMessage();
                    System.out.println(message);
                } else if (type == CommandType.MOVE) {
                    Move move = command.getMove();
                    this.match.handleMove(move);
                    System.out.println(move.toString());
                } else {
                    Instruction instruction = command.getInstruction();
                    this.match.handleInstruction(instruction);
                    System.out.println(instruction.toString());
                }
            }
        } catch (Exception e) {
            this.match.handleMove(Move.Surrender(this.match.getThisPlayer()));
            System.out.println("Partida encerrada com erro");
        }
    }

    public void parseMessage(String input) {
        this.match.handleMessage(input);
    }

    public void parseMove(String input) {
        int player = match.getThisPlayer();

        if (input.charAt(0) == '/') {
            Move move = (input.equals("/ff")) ? Move.Surrender(player) : Move.SkipTurn(player);

            this.match.handleMove(move);
            return;
        }

        int board = Character.getNumericValue(input.charAt(0));
        int line = Character.getNumericValue(input.charAt(1));
        int column = Character.getNumericValue(input.charAt(2));

        Move move = new Move(player, board, line, column);

        this.match.handleMove(move);
    }

    private void printCells() {
        final int currentPlayer = this.match.getCurrentPlayer();

        System.out.println("Jogador atual: " + currentPlayer);
        for (int line = 0; line < Match.BOARD_SIZE; line++) {
            for (int board = 0; board < Match.BOARD_SIZE; board++) {
                for (int column = 0; column < Match.BOARD_SIZE; column++) {
                    System.out.print(this.match.getCell(board, line, column));
                }

                System.out.print(' ');
            }

            System.out.print('\n');
        }
    }
}
