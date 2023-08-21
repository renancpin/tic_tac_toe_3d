package controllers.keyboard;

import java.util.Scanner;

import communication.*;
import controllers.Controller;
import game.*;

public class KeyboardController implements Controller {
    private Communicator comm;
    private Match match;
    private Thread receiver;

    public KeyboardController(Communicator comm) {
        this.comm = comm;

        Scanner console = new Scanner(System.in);

        System.out.println("Deseja criar (1) ou conectar(2) ?: ");
        String tipo = console.nextLine();

        if (tipo.equals("1")) {
            this.match = new HostedMatch();
            this.comm.host(3000);
        } else {
            this.match = new GuestMatch();
            this.comm.connect("", 3000);
        }

        this.receiver = new Thread(this);

        receiver.start();

        while (true) {
            System.out.println("Teste um movimento: ");

            String play = console.nextLine();

            int player = 0;
            int board = Character.getNumericValue(play.charAt(0));
            int line = Character.getNumericValue(play.charAt(1));
            int column = Character.getNumericValue(play.charAt(2));

            Move move = new Move(player, board, line, column);

            try {
                comm.sendCommand(new Command(move));
            } catch (Exception e) {
                break;
            }
        }

        console.close();
    }

    public void run() {
        Command command;
        CommandType type;
        String message;
        Move move;
        Exception error;

        try {
            while (true) {
                command = this.comm.receiveCommand();
                type = command.getType();

                if (type == CommandType.MESSAGE) {
                    message = command.getMessage();
                    System.out.println("[MSG] " + message);
                } else if (type == CommandType.MOVE) {
                    move = command.getMove();
                    // this.match.makeMove(move);
                    System.out.println(move.toString());
                } else {
                    error = command.getException();
                    throw error;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }
}
