package controllers.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.CardLayout;
import java.awt.GridLayout;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.Font;
import communication.command.Command;
import communication.command.CommandType;
import controllers.Controller;
import game.instructions.InstructionType;
import game.match.Match;
import game.match.MatchBuilder;
import game.move.Move;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class GUIController implements Controller {
	private int BOARD_SIZE = Match.BOARD_SIZE;
	private JFrame frame;
	private JTextField textField;
	private JTextPane chatBox;
	private JButton[][][] cells = new JButton[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
	private Set<JButton> allButtons = new HashSet<>();
	private MatchBuilder matchBuilder;
	private Match match;

	/**
	 * Create the application.
	 */
	public GUIController(MatchBuilder matchBuilder) {
		this.matchBuilder = matchBuilder;
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));

		JPanel panelStart = new JPanel();
		frame.getContentPane().add(panelStart, "panelStart");
		panelStart.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel lblHome = new JLabel("Jogo da Velha 3D");
		lblHome.setHorizontalAlignment(SwingConstants.CENTER);
		lblHome.setFont(new Font("Trebuchet MS", Font.BOLD, 50));
		panelStart.add(lblHome);

		JPanel panelMatch = new JPanel();
		panelStart.add(panelMatch);
		panelMatch.setLayout(null);

		JButton btnHostMatch = new JButton("Criar Partida");
		btnHostMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				waitForMatch(true);
			}
		});
		btnHostMatch.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnHostMatch.setBounds(202, 53, 137, 38);
		panelMatch.add(btnHostMatch);

		JButton btnConnectMatch = new JButton("Conectar");
		btnConnectMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				waitForMatch(false);
			}
		});
		btnConnectMatch.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnConnectMatch.setBounds(394, 53, 137, 38);
		panelMatch.add(btnConnectMatch);

		JPanel panelGame = new JPanel();
		frame.getContentPane().add(panelGame, "panelGame");
		panelGame.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panelBoards = new JPanel();
		panelGame.add(panelBoards);
		panelBoards.setLayout(new GridLayout(0, 3, 2, 2));

		for (int board = 0; board < BOARD_SIZE; board++) {
			JPanel panelBoard = new JPanel();
			panelBoard.setBorder(new LineBorder(new Color(0, 0, 0), 2));
			panelBoards.add(panelBoard);
			panelBoard.setLayout(new GridLayout(3, 3, 0, 0));

			for (int line = 0; line < BOARD_SIZE; line++) {
				for (int column = 0; column < BOARD_SIZE; column++) {
					JButton button = new JButton(" ");
					panelBoard.add(button);
					cells[board][line][column] = button;
					allButtons.add(button);
					button.setForeground(Color.BLACK);
					button.setFont(new Font("Trebuchet MS", Font.BOLD, 37));
					button.setEnabled(false);
					button.setActionCommand("" + board + line + column);
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String command = e.getActionCommand();
							int board = Character.getNumericValue(command.charAt(0));
							int line = Character.getNumericValue(command.charAt(1));
							int column = Character.getNumericValue(command.charAt(2));
							Move move = new Move(match.getThisPlayer(), board, line, column);
							match.handleMove(move);
						}
					});
				}
			}
		}

		JPanel panelControls = new JPanel();
		panelGame.add(panelControls);
		panelControls.setLayout(null);

		textField = new JTextField();
		textField.setBounds(23, 171, 556, 26);
		panelControls.add(textField);
		textField.setColumns(10);

		JButton btnSendMessage = new JButton("Enviar");
		btnSendMessage.setFont(new Font("Tahoma", Font.BOLD, 10));
		btnSendMessage.setForeground(new Color(0, 128, 0));
		btnSendMessage.setBounds(621, 171, 89, 25);
		btnSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = textField.getText();
				if (!message.equals("")) {
					match.handleMessage(message);
					addText(message);
					textField.setText("");
				}
			}
		});
		panelControls.add(btnSendMessage);

		JButton btnSurrender = new JButton("Desistir");
		btnSurrender.setFont(new Font("Tahoma", Font.BOLD, 10));
		btnSurrender.setForeground(new Color(255, 0, 0));
		btnSurrender.setBounds(621, 10, 89, 26);
		btnSurrender.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Move move = Move.Surrender(match.getThisPlayer());
				match.handleMove(move);
			}
		});
		panelControls.add(btnSurrender);
		allButtons.add(btnSurrender);

		JLabel lblChatBox = new JLabel("Chat");
		lblChatBox.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblChatBox.setBounds(23, 23, 45, 13);
		panelControls.add(lblChatBox);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(23, 46, 687, 117);
		panelControls.add(scrollPane);

		chatBox = new JTextPane();
		chatBox.setEditable(false);
		scrollPane.setViewportView(chatBox);

		frame.setVisible(true);
	}

	private void waitForMatch(boolean asHost) {
		Container container = frame.getContentPane();
		CardLayout layout = (CardLayout) container.getLayout();
		layout.next(container);

		addText("Aguardando jogador...");

		GUIController controller = this;
		(new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				match = matchBuilder.createMatch(asHost);
				match.listen(controller::handleUpdate);
				match.start();
				while (!match.getIsRunning()) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				controller.addText("Partida iniciada");
				return null;
			}
		}).execute();
	}

	private void addText(String text) {
		String currentText = chatBox.getText();
		chatBox.setText(currentText.equals("") ? text : currentText + '\n' + text);
	}

	private void toggleTurn() {
		boolean isMyTurn = match.getCurrentPlayer() == match.getThisPlayer();
		for (JButton button : allButtons) {
			button.setEnabled(isMyTurn);
		}
	}

	private void handleUpdate(Command command) {
		CommandType commandType = command.getType();

		switch (commandType) {
			case MOVE:
				addText("Controller nao processa movimentos");
				break;
			case MESSAGE:
				addText("[YOU] " + command.getMessage());
				break;
			case INSTRUCTION:
				InstructionType instructionType = command.getInstruction().getType();

				switch (instructionType) {
					case SET_GUEST:
						int thisPlayer = match.getThisPlayer();
						addText("Você é o jogador " + thisPlayer + "!");
						toggleTurn();
						break;
					case SET_TURN:
						toggleTurn();
						break;
					case SET_CELL:
						Move move = command.getInstruction().getMove();

						int player = move.getPlayer();
						int board = move.getBoard();
						int line = move.getLine();
						int column = move.getColumn();

						cells[board][line][column].setText(player == 1 ? "X" : "O");
						break;
					case END_MATCH:
						addText("Partida encerrada!");
						addText("Vitória do jogador " + match.getCurrentPlayer());
						return;
					default:
						break;
				}
				break;
		}
	}
}
