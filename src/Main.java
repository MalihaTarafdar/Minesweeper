/*
 * Run Minesweeper.jar to play
 *
 * Default and Monochrome theme icons created by "Black Squirrel".
 * Dark theme icons created by me.
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main implements ActionListener, MouseListener {
	private final int TILE_SIZE = 32; //2x original tile size
	private final int SCOREBOARD_PADDING = 5;
	private final String SCOREBOARD_PLACEHOLDER = "000"; //placeholder for number of digits in scoreboard panels

	private final Theme DEFAULT = new Theme("default", new Color(192, 192, 192), Color.WHITE, new Color(128, 128, 128),
			Color.BLACK, new Color(200, 200, 200), Color.BLACK, Color.WHITE);
	private final Theme MONOCHROME = new Theme("monochrome", null, Color.WHITE, Color.BLACK,
			Color.WHITE, Color.BLACK, Color.BLACK, Color.WHITE);
	private final Theme DARK = new Theme("dark", new Color(47, 49, 49), new Color(103, 103, 103), Color.BLACK,
			new Color(216, 216, 216), new Color(24, 24, 24), new Color(216, 216, 216), new Color(72, 72, 72));

	private final Difficulty BEGINNER = new Difficulty(9, 9, 10); //DEFAULT
	private final Difficulty INTERMEDIATE = new Difficulty(16, 16, 40);
	private final Difficulty EXPERT = new Difficulty(16, 30, 99);

	private JFrame frame;
	private JPanel mainPanel, gamePanel, scoreboardPanel, flagsPanel, timePanel;
	private JMenuBar menuBar;
	private JMenuItem beginnerItem, intermediateItem, expertItem, defaultItem, monochromeItem, darkItem;
	private JLabel[] flagsLabels, timeLabels;
	private JButton resetButton;

	private Theme theme;
	private ImageIcon tileIcon, revealedTileIcon, mineIcon, flagIcon, unknownIcon, mineSelectedIcon, incorrectGuessIcon;
	private ImageIcon resetIcon, resetDownIcon, resetTileDownIcon, resetWinIcon, resetLoseIcon;
	private ImageIcon[] numberIcons, scoreboardNumberIcons;
	private Font menuFont, labelFont;

	private JToggleButton[][] gameGrid;
	private Difficulty difficulty;
	private Timer timer;
	private int flags;
	private int time; //in seconds
	private boolean firstReveal;

	public Main() {
		frame = new JFrame("Minesweeper");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//UIManager
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {}

		UIManager.put("MenuBar.border", false);
		UIManager.put("PopupMenu.border", false);
		UIManager.put("Menu.border", new EmptyBorder(4, 4, 4, 4));
		UIManager.put("MenuItem.border", new EmptyBorder(4, 4, 4, 4));

		//FONT
		try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			menuFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/mine-sweeper.ttf"));
			labelFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/FORCED SQUARE.ttf"));
			ge.registerFont(menuFont);
			ge.registerFont(labelFont);
		} catch (IOException|FontFormatException ignored) {}

		//SET THEME
		loadTheme(theme = DEFAULT);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		scoreboardPanel = new JPanel();
		scoreboardPanel.setLayout(new GridBagLayout());

		//FLAGS PANEL
		flagsPanel = new JPanel();
		flagsPanel.setLayout(new GridLayout(1, SCOREBOARD_PLACEHOLDER.length()));
		flagsLabels = new JLabel[SCOREBOARD_PLACEHOLDER.length()];
		//add icons to flags panel
		for (int i = 0; i < flagsLabels.length; i++) {
			flagsLabels[i] = new JLabel(scoreboardNumberIcons[Character.getNumericValue(SCOREBOARD_PLACEHOLDER.charAt(i))]);
			flagsPanel.add(flagsLabels[i]);
		}

		//TIME PANEL
		timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(1, SCOREBOARD_PLACEHOLDER.length()));
		timeLabels = new JLabel[SCOREBOARD_PLACEHOLDER.length()];
		//add icons to time panel
		for (int i = 0; i < timeLabels.length; i++) {
			timeLabels[i] = new JLabel(scoreboardNumberIcons[Character.getNumericValue(SCOREBOARD_PLACEHOLDER.charAt(i))]);
			timePanel.add(timeLabels[i]);
		}

		//RESET BUTTON
		resetButton = new JButton();
		resetButton.setBorderPainted(false);
		resetButton.setFocusPainted(false);
		resetButton.addMouseListener(this);

		//CONSTRAINTS FOR SCOREBOARD PANEL
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(0, SCOREBOARD_PADDING, SCOREBOARD_PADDING * 2 / 3, 0);
		scoreboardPanel.add(flagsPanel, constraints);
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(0, 0, SCOREBOARD_PADDING * 2 / 3, 0);
		scoreboardPanel.add(resetButton, constraints);
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.insets = new Insets(0, 0, SCOREBOARD_PADDING * 2 / 3, SCOREBOARD_PADDING);
		scoreboardPanel.add(timePanel, constraints);

		mainPanel.add(scoreboardPanel, BorderLayout.NORTH);

		createMenu();
		init(BEGINNER); //starts on default difficulty

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void expand(final int row, final int col) {
		if (gameGrid[row][col].getIcon() != tileIcon) return; //only empty tiles should expand

		int mineCount = (Integer)gameGrid[row][col].getClientProperty("mines");
		if (mineCount > 0) {
			gameGrid[row][col].setIcon(numberIcons[mineCount - 1]); //change number of tile
		} else {
			gameGrid[row][col].setIcon(revealedTileIcon);
			for (int r = row - 1; r <= row + 1; r++) {
				for (int c = col - 1; c <= col + 1; c++) {
					if (r < 0 || c < 0 || r >= gameGrid.length || c >= gameGrid[0].length) continue; //try catch is a lot slower than if-statement if exception occurs
					expand(r, c);
				}
			}
		}
		gameGrid[row][col].setPressedIcon(gameGrid[row][col].getIcon());
		gameGrid[row][col].putClientProperty("enabled", false);
	}

	public void setMines(final int currentRow, final int currentCol) {
		for (int i = 0; i < difficulty.getMines(); i++) {
			//SET MINES
			int row, col;
			do {
				row = (int)(Math.random() * gameGrid.length);
				col = (int)(Math.random() * gameGrid[0].length);
			} while (Math.abs(row - currentRow) <= 1 || Math.abs(col - currentCol) <= 1 || (Integer)gameGrid[row][col].getClientProperty("mines") == -1);

			gameGrid[row][col].putClientProperty("mines", -1); //mine = -1

			//SET MINE COUNTS
			//adds 1 to mines property of all adjacent tiles
			for (int r = row - 1; r <= row + 1; r++) {
				for (int c = col - 1; c <= col + 1; c++) {
					if (r < 0 || c < 0 || r >= gameGrid.length || c >= gameGrid[0].length) continue;
					if ((Integer)gameGrid[r][c].getClientProperty("mines") != -1) {
						gameGrid[r][c].putClientProperty("mines", (Integer)gameGrid[r][c].getClientProperty("mines") + 1);
					}
				}
			}
		}
	}

	public boolean isWin() {
		if (flags < 0) return false; //not win if flags is negative
		for (int r = 0; r < gameGrid.length; r++) {
			for (int c = 0; c < gameGrid[0].length; c++) {
				//not win if a flag, unknown, or empty tile is not a mine
				if (gameGrid[r][c].getIcon() == flagIcon || gameGrid[r][c].getIcon() == unknownIcon || gameGrid[r][c].getIcon() == tileIcon) {
					if ((Integer)gameGrid[r][c].getClientProperty("mines") != -1) return false;
				}
			}
		}
		return true;
	}

	public void endGame(boolean win) {
		timer.cancel();

		//set reset button icon
		if (win) resetButton.setIcon(resetWinIcon);
		else resetButton.setIcon(resetLoseIcon);
		resetButton.setBorder(null);

		for (int r = 0; r < gameGrid.length; r++) {
			for (int c = 0; c < gameGrid[0].length; c++) {
				if (win) {
					//sets icons of empty & unknown tiles that have mines to flag icon (behavior of actual game)
					if (gameGrid[r][c].getIcon() == tileIcon || gameGrid[r][c].getIcon() == unknownIcon) gameGrid[r][c].setIcon(flagIcon);
				} else {
					//reveal mines
					if ((Integer)gameGrid[r][c].getClientProperty("mines") == -1) {
						if (gameGrid[r][c].getIcon() != flagIcon && gameGrid[r][c].getIcon() != mineSelectedIcon) gameGrid[r][c].setIcon(mineIcon);
					} else {
						if (gameGrid[r][c].getIcon() == flagIcon) gameGrid[r][c].setIcon(incorrectGuessIcon);
					}
				}
				//disable button
				gameGrid[r][c].setDisabledIcon(gameGrid[r][c].getIcon());
				gameGrid[r][c].setEnabled(false);
			}
		}

		if (win) updateScoreboard(0, "flags");
	}

	public void updateScoreboard(int value, String component) {
		if (Integer.toString(value).length() > SCOREBOARD_PLACEHOLDER.length()) return; //don't display numbers larger than scoreboard allows
		boolean negative = value < 0;
		value = Math.abs(value);
		//convert number to scoreboard format
		String panelStr = SCOREBOARD_PLACEHOLDER.substring(0, SCOREBOARD_PLACEHOLDER.length() - Integer.toString(value).length()) + value;
		if (component.equals("flags")) { //set flags panel icons
			for (int i = 0; i < flagsLabels.length; i++) {
				flagsLabels[i].setIcon(scoreboardNumberIcons[Character.getNumericValue(panelStr.charAt(i))]);
			}
			if (negative) flagsLabels[0].setIcon(scoreboardNumberIcons[scoreboardNumberIcons.length - 1]);
		} else if (component.equals("time")) { //set time panel icons
			for (int i = 0; i < timeLabels.length; i++) {
				timeLabels[i].setIcon(scoreboardNumberIcons[Character.getNumericValue(panelStr.charAt(i))]);
			}
			if (negative) timeLabels[0].setIcon(scoreboardNumberIcons[scoreboardNumberIcons.length - 1]);
		}
	}

	public void init(Difficulty difficulty) {
		final int ROWS = difficulty.getRows(), COLUMNS = difficulty.getColumns();

		frame.getContentPane().remove(mainPanel);
		if (gamePanel != null) mainPanel.remove(gamePanel);
		if (timer != null) timer.cancel();

		//INITIALIZE GAME VARIABLES
		this.difficulty = difficulty;
		gameGrid = new JToggleButton[ROWS][COLUMNS];
		timer = new Timer();
		flags = difficulty.getMines();
		time = 0;
		firstReveal = true;

		//SCOREBOARD PANEL
		scoreboardPanel.setBorder(BorderFactory.createCompoundBorder(
			new EmptyBorder(CustomBorder.getWidth() + SCOREBOARD_PADDING, CustomBorder.getWidth() + SCOREBOARD_PADDING, 2 * CustomBorder.getHSWidth() + SCOREBOARD_PADDING, CustomBorder.getWidth() + SCOREBOARD_PADDING),
			new CustomBorder.ScoreboardBorder(SCOREBOARD_PADDING, theme)
		));

		//GAME PANEL
		gamePanel = new JPanel();
		gamePanel.setLayout(new GridLayout(ROWS, COLUMNS));
		gamePanel.setBorder(BorderFactory.createCompoundBorder(
			new EmptyBorder(CustomBorder.getWidth() * 2 / 3, CustomBorder.getWidth(), CustomBorder.getWidth(), CustomBorder.getWidth()),
			new CustomBorder.GameBorder(theme)
		));

		//FRAME SIZE
		if (difficulty == BEGINNER) mainPanel.setPreferredSize(new Dimension(311, 406));
		else if (difficulty == INTERMEDIATE) mainPanel.setPreferredSize(new Dimension(528, 598));
		else mainPanel.setPreferredSize(new Dimension(962, 598));

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLUMNS; col++) {
				gameGrid[row][col] = new JToggleButton();
				gameGrid[row][col].setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
				gameGrid[row][col].setIcon(tileIcon);
				gameGrid[row][col].setPressedIcon(revealedTileIcon);
				gameGrid[row][col].setBorderPainted(false);
				gameGrid[row][col].setFocusPainted(false);
				gameGrid[row][col].putClientProperty("mines", 0);
				gameGrid[row][col].putClientProperty("enabled", true); //allows for disabling a button but keeping it clickable (useful for revealing adjacent tiles)
				gameGrid[row][col].addMouseListener(this);
				gamePanel.add(gameGrid[row][col]);
			}
		}

		//ADD GAME PANEL
		mainPanel.add(gamePanel, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.revalidate();

		//RESET BUTTON
		resetButton.setIcon(resetIcon);
		resetButton.setPressedIcon(resetDownIcon);
		resetButton.setBorder(null);

		//UPDATE FLAGS AND TIME PANELS
		updateScoreboard(flags, "flags");
		updateScoreboard(time, "time");
	}

	public void loadTheme(Theme theme) {
		UIManager.put("MenuBar.background", theme.getMenuBackground());
		UIManager.put("Menu.foreground", theme.getMenuForeground());
		UIManager.put("Menu.background", theme.getMenuBackground());
		UIManager.put("Menu.selectionForeground", theme.getMenuSelectionForeground());
		UIManager.put("Menu.selectionBackground", theme.getMenuSelectionBackground());
		UIManager.put("MenuItem.foreground", theme.getMenuSelectionForeground());
		UIManager.put("MenuItem.background", theme.getMenuSelectionBackground());
		UIManager.put("MenuItem.selectionForeground", theme.getMenuForeground());
		UIManager.put("MenuItem.selectionBackground", theme.getMenuBackground());
		UIManager.put("Label.foreground", theme.getMenuSelectionForeground());
		UIManager.put("Label.background", theme.getMenuSelectionBackground());
		setIcons(theme.getIconSet());
		createMenu();
	}

	public void createMenu() {
		menuBar = new JMenuBar();

		//GAME MENU
		JMenu gameMenu = getMenu("Game");
		gameMenu.add(beginnerItem = getMenuItem("Beginner"));
		gameMenu.add(intermediateItem = getMenuItem("Intermediate"));
		gameMenu.add(expertItem = getMenuItem("Expert"));

		//ICONS MENU
		JMenu themeMenu = getMenu("Theme");
		themeMenu.add(defaultItem = getMenuItem("Default"));
		themeMenu.add(monochromeItem = getMenuItem("Monochrome"));
		themeMenu.add(darkItem = getMenuItem("Dark"));

		//CONTROLS MENU
		JMenu controlsMenu = getMenu("Controls");
		JLabel controlsLabel = new JLabel(
			"<html><p>&bull; <b>Left-click</b> an empty tile to reveal it.</p>" +
			"<p>&bull; <b>Right-click</b> an unrevealed tile to cycle through flagged, unknown, and empty states.</p>" +
			"<p>&bull; Hold <b>middle-click</b> or <b>ctrl + left-click</b> on a tile to reveal its adjacent tiles.</p>" +
			"<p>&bull; Changing the icon set will <b>reset the game</b>.</p></html>"
		);
		controlsLabel.setOpaque(true);
		controlsLabel.setFont(labelFont.deriveFont(16f));
		controlsLabel.setPreferredSize(new Dimension(250, 150));
		controlsLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		controlsMenu.add(controlsLabel);

		//ADD MENUS
		menuBar.add(gameMenu);
		menuBar.add(themeMenu);
		menuBar.add(controlsMenu);

		frame.setJMenuBar(menuBar);
	}

	public JMenu getMenu(String name) {
		JMenu menu = new JMenu(name);
		menu.setOpaque(true);
		menu.setFont(menuFont.deriveFont(10.5f));
		return menu;
	}

	public JMenuItem getMenuItem(String name) {
		JMenuItem item = new JMenuItem(name);
		item.setOpaque(true);
		item.setFont(menuFont.deriveFont(8.5f));
		item.addActionListener(this);
		return item;
	}

	public void setIcons(String iconSet) {
		final int RESET_BUTTON_SIZE = 52; //2x original reset button size
		final int SCOREBOARD_NUMBERS_HEIGHT = 46; //2x height of original scoreboard icons
		String iconSetDir = "/icons/" + iconSet + "/";

		tileIcon = getImageIcon(iconSetDir + "tile.png", TILE_SIZE, TILE_SIZE);
		revealedTileIcon = getImageIcon(iconSetDir + "revealed_tile.png", TILE_SIZE, TILE_SIZE);
		mineIcon = getImageIcon(iconSetDir + "mine.png", TILE_SIZE, TILE_SIZE);
		flagIcon = getImageIcon(iconSetDir + "flag.png", TILE_SIZE, TILE_SIZE);
		unknownIcon = getImageIcon(iconSetDir + "unknown.png", TILE_SIZE, TILE_SIZE);
		mineSelectedIcon = getImageIcon(iconSetDir + "mine_selected.png", TILE_SIZE, TILE_SIZE);
		incorrectGuessIcon = getImageIcon(iconSetDir + "incorrect_guess.png", TILE_SIZE, TILE_SIZE);

		resetIcon = getImageIcon(iconSetDir + "reset/reset.png", RESET_BUTTON_SIZE, RESET_BUTTON_SIZE);
		resetDownIcon = getImageIcon(iconSetDir + "reset/down.png", RESET_BUTTON_SIZE, RESET_BUTTON_SIZE);
		resetTileDownIcon = getImageIcon(iconSetDir + "reset/tile_down.png", RESET_BUTTON_SIZE, RESET_BUTTON_SIZE);
		resetWinIcon = getImageIcon(iconSetDir + "reset/win.png", RESET_BUTTON_SIZE, RESET_BUTTON_SIZE);
		resetLoseIcon = getImageIcon(iconSetDir + "reset/lose.png", RESET_BUTTON_SIZE, RESET_BUTTON_SIZE);

		numberIcons = new ImageIcon[8];
		for (int i = 0; i < numberIcons.length; i++) {
			numberIcons[i] = getImageIcon(iconSetDir + "numbers/" + (i + 1) + ".png", TILE_SIZE, TILE_SIZE);
		}

		scoreboardNumberIcons = new ImageIcon[11];
		for (int i = 0; i < scoreboardNumberIcons.length; i++) {
			//keep aspect ratio of scoreboard numbers since not square
			scoreboardNumberIcons[i] = new ImageIcon(getClass().getResource(iconSetDir + "scoreboard_numbers/" + (i != scoreboardNumberIcons.length - 1 ? i : "negative") + ".png"));
			scoreboardNumberIcons[i] = new ImageIcon(scoreboardNumberIcons[i].getImage().getScaledInstance(
				SCOREBOARD_NUMBERS_HEIGHT * scoreboardNumberIcons[i].getIconWidth() / scoreboardNumberIcons[i].getIconHeight(),
				SCOREBOARD_NUMBERS_HEIGHT,
				Image.SCALE_SMOOTH
			));
		}
	}

	public ImageIcon getImageIcon(String dir, int width, int height) {
		ImageIcon imageIcon = new ImageIcon(getClass().getResource(dir));
		imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		return imageIcon;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == beginnerItem) init(BEGINNER);
		else if (src == intermediateItem) init(INTERMEDIATE);
		else if (src == expertItem) init(EXPERT);
		else if (src == defaultItem) {
			loadTheme(theme = DEFAULT);
			init(difficulty);
		} else if (src == monochromeItem) {
			loadTheme(theme = MONOCHROME);
			init(difficulty);
		} else if (src == darkItem) {
			loadTheme(theme = DARK);
			init(difficulty);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		JComponent src = (JComponent)e.getSource();
		if (!src.isEnabled()) return;

		if (src instanceof JToggleButton) {
			JToggleButton button = (JToggleButton)e.getSource();
			//no need for storing row & col in property - x and y of button is relative to panel
			int row = button.getY() / button.getHeight();
			int col = button.getX() / button.getWidth();

			if (SwingUtilities.isRightMouseButton(e)) {
				//cycle through flag, unknown, and empty tile
				if (gameGrid[row][col].getIcon() == tileIcon) { //change to flag
					gameGrid[row][col].setIcon(flagIcon);
					gameGrid[row][col].setPressedIcon(flagIcon);
					updateScoreboard(--flags, "flags");
				} else if (gameGrid[row][col].getIcon() == flagIcon) { //change to unknown
					gameGrid[row][col].setIcon(unknownIcon);
					gameGrid[row][col].setPressedIcon(unknownIcon);
					updateScoreboard(++flags, "flags");
				} else if (gameGrid[row][col].getIcon() == unknownIcon) { //change to empty
					gameGrid[row][col].setIcon(tileIcon);
					gameGrid[row][col].setPressedIcon(revealedTileIcon);
				}
			} else if (!e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
				//change reset button icon when tile is held down
				if (gameGrid[row][col].getIcon() == tileIcon) {
					resetButton.setIcon(resetTileDownIcon);
					resetButton.setBorder(null);
				}
			} else if (SwingUtilities.isMiddleMouseButton(e) || (e.isControlDown() && SwingUtilities.isLeftMouseButton(e))) {
				//reveal adjacent tiles
				for (int r = row - 1; r <= row + 1; r++) {
					for (int c = col - 1; c <= col + 1; c++) {
						if (r < 0 || c < 0 || r >= gameGrid.length || c >= gameGrid[0].length) continue;
						if ((Boolean)gameGrid[r][c].getClientProperty("enabled") && gameGrid[r][c].getIcon() == tileIcon) {
							gameGrid[r][c].setIcon(revealedTileIcon);
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		JComponent src = (JComponent)e.getSource();
		if (!src.isEnabled()) return;

		if (src == resetButton) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				resetButton.setIcon(resetIcon);
				resetButton.setBorder(null);
				init(difficulty);
			}
		} else if (src instanceof JToggleButton) {
			JToggleButton button = (JToggleButton)e.getSource();
			int row = button.getY() / button.getHeight();
			int col = button.getX() / button.getWidth();

			if (!e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
				if (gameGrid[row][col].getIcon() != tileIcon) return;
				//end game if mine is clicked
				if ((Integer)gameGrid[row][col].getClientProperty("mines") == -1) {
					gameGrid[row][col].setIcon(mineSelectedIcon);
					endGame(false);
					return;
				}
				//set reset button icon
				resetButton.setIcon(resetIcon);
				resetButton.setBorder(null);
				//check first reveal
				if (firstReveal) {
					setMines(row, col); //set mines
					//start timer
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							updateScoreboard(++time, "time");
						}
					}, 0, 1000);
					firstReveal = false;
				}
				//expand tiles
				expand(row, col);
				if (isWin()) endGame(true); //check win
			} else if (SwingUtilities.isMiddleMouseButton(e) || (e.isControlDown() && SwingUtilities.isLeftMouseButton(e))) {
				//unreveal adjacent tiles
				for (int r = row - 1; r <= row + 1; r++) {
					for (int c = col - 1; c <= col + 1; c++) {
						if (r < 0 || c < 0 || r >= gameGrid.length || c >= gameGrid[0].length) continue;
						if ((Boolean)gameGrid[r][c].getClientProperty("enabled") && gameGrid[r][c].getIcon() == revealedTileIcon) {
							gameGrid[r][c].setIcon(tileIcon);
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	public static void main(String[] args) {
		new Main();
	}
}