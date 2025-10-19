package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

public class PixelGameGUI extends JFrame {
    private transient ImageIcon[][] spriteFrames = new ImageIcon[4][3];

    private ShopPanel shopPanel;

    private PixelGameController gameController;
    private PixelMazePanel mazePanel;
    private CharacterSelectionPanel characterSelectionPanel;
    private boolean[] keys = new boolean[4];
    private Timer inputTimer;


    private int selectedOption = 0;
    private final String[] menuOptions = {"START GAME", "RESUME GAME","EXIT"};
    private DifficultySelectionPanel difficultySelectionPanel;
    private int selectedDifficulty = 0;
    private final String[] difficultyOptions = {"EASY", "MEDIUM", "HARD"};
    private Font dragonSlayerFont;
    private Image backgroundImage;
    private Image iconImage;
    private ImageIcon arrowIcon;

    private int selectedCharacter = 0;

    public PixelGameGUI() {
        super("Pixel Maze Adventure");
        loadResources();
        setCustomIcon();
        initializeMainFrame();
    }

    private class DifficultySelectionPanel extends JPanel {
        private int currentSelectedDifficulty = 0;
        private Image difficultyBackgroundImage;

        private final int[] optionYPositions = {220, 300, 370};
        private final int indicatorX = 230;

        public DifficultySelectionPanel() {
            setPreferredSize(new Dimension(800, 600));
            setFocusable(true);
            loadDifficultyBackground();
        }

        private void loadDifficultyBackground() {
            try {
                java.io.InputStream bgStream = getClass().getResourceAsStream("/general/difficulty.png");
                if (bgStream != null) {
                    difficultyBackgroundImage = new ImageIcon(bgStream.readAllBytes()).getImage();
                    System.out.println("Difficulty background image loaded successfully");
                } else {
                    System.out.println("Difficulty background image not found at /general/difficulty.png");
                    difficultyBackgroundImage = backgroundImage;
                }
            } catch (Exception e) {
                System.out.println("Error loading difficulty background: " + e.getMessage());
                difficultyBackgroundImage = backgroundImage;
            }
        }

        public void setSelectedDifficulty(int difficulty) {
            this.currentSelectedDifficulty = difficulty;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;


            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);



            if (difficultyBackgroundImage != null) {
                g2d.drawImage(difficultyBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {

                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }


            g2d.setFont(dragonSlayerFont.deriveFont(32f));
            for (int i = 0; i < difficultyOptions.length; i++) {
                Color color = (i == currentSelectedDifficulty) ? new Color(255, 215, 0) : Color.WHITE;
                Color shadowColor = (i == currentSelectedDifficulty) ? new Color(180, 150, 0) : new Color(50, 50, 50);

                int yPos = optionYPositions[i];



                if (i == currentSelectedDifficulty && arrowIcon != null) {
                    int arrowX = indicatorX;
                    int arrowY = yPos - 25;
                    g2d.drawImage(arrowIcon.getImage(), arrowX, arrowY, this);
                } else if (i == currentSelectedDifficulty) {
                    g2d.setColor(new Color(255, 215, 0));
                    g2d.setFont(dragonSlayerFont.deriveFont(36f));
                    g2d.drawString(">", indicatorX, yPos);
                }

                g2d.setFont(dragonSlayerFont.deriveFont(32f));
            }


            g2d.setColor(new Color(200, 200, 200, 180));
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.drawString("Press ENTER to start game", getWidth() / 2 - 80, 500);
            g2d.drawString("Press ESC to go back", getWidth() / 2 - 60, 530);
        }

        private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color textColor, Color shadowColor) {
            g2d.setColor(shadowColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2 + 2, y + 2);

            g2d.setColor(textColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2, y);
        }
    }
    private void loadResources() {
        try {
            java.io.InputStream fontStream = getClass().getResourceAsStream("/dragonslayer.ttf");
            if (fontStream != null) {
                dragonSlayerFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(36f);
                System.out.println("DragonSlayer font loaded successfully");
            } else {
                System.out.println("DragonSlayer font not found, using fallback");
                dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            }

            java.io.InputStream bgStream = getClass().getResourceAsStream("/general/background.png");
            if (bgStream != null) {
                backgroundImage = new ImageIcon(bgStream.readAllBytes()).getImage();
                System.out.println("Background image loaded successfully");
            } else {
                System.out.println("Background image not found, using fallback");
                backgroundImage = createFallbackBackground();
            }

            java.io.InputStream iconStream = getClass().getResourceAsStream("/general/icon.png");
            if (iconStream != null) {
                byte[] iconBytes = iconStream.readAllBytes();
                iconImage = new ImageIcon(iconBytes).getImage();
                System.out.println("Window icon loaded successfully");
            } else {
                System.out.println("Window icon not found at /general/icon.png");
                createFallbackIcon();
            }
            java.io.InputStream arrowStream = getClass().getResourceAsStream("/general/arrow.png");
            if (arrowStream != null) {
                ImageIcon originalArrow = new ImageIcon(arrowStream.readAllBytes());
                Image scaledArrow = originalArrow.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                arrowIcon = new ImageIcon(scaledArrow);
                System.out.println("Arrow icon loaded successfully");
            } else {
                System.out.println("Arrow icon not found at /general/arrow.png");
                createFallbackArrow();
            }

        } catch (Exception e) {
            System.out.println("Error loading resources: " + e.getMessage());
            dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            backgroundImage = createFallbackBackground();
            createFallbackIcon();
        }
    }

    private void setCustomIcon() {
        if (iconImage != null) {
            this.setIconImage(iconImage);
            System.out.println("Custom window icon set successfully");
        } else {
            System.out.println("No custom icon available, using default Java icon");
        }
    }

    private void createFallbackIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(0, 0, 32, 32);

        g2d.setColor(Color.YELLOW);
        g2d.fillRect(4, 4, 24, 24);

        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(8, 8, 4, 16);
        g2d.fillRect(16, 8, 4, 8);
        g2d.fillRect(20, 16, 4, 8);

        g2d.setColor(Color.RED);
        g2d.fillOval(12, 20, 8, 8);

        g2d.dispose();
        iconImage = img;
        System.out.println("Created fallback window icon");
    }

    private void createFallbackArrow() {
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 215, 0)); // Gold color
        int[] xPoints = {5, 35, 5};
        int[] yPoints = {5, 20, 35};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.dispose();
        arrowIcon = new ImageIcon(img);
        System.out.println("Created fallback arrow");
    }

    private Image createFallbackBackground() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 30, 60),
                800, 600, new Color(10, 10, 30));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, 800, 600);

        g2d.setColor(new Color(40, 40, 80));
        for (int i = 0; i < 800; i += 40) {
            for (int j = 0; j < 600; j += 40) {
                g2d.drawRect(i, j, 20, 20);
            }
        }

        g2d.dispose();
        return img;
    }

    private void initializeMainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameController != null && gameController.isGameOngoing()) {
                    saveGame();
                }
                showExitImage();
            }
        });

        showMainMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showMainMenu() {
        if (gameController != null) {
            gameController.stopGame();
        }

        getContentPane().removeAll();

        MenuPanel menuPanel = new MenuPanel();
        setContentPane(menuPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        setupMenuInputHandling();

        revalidate();
        repaint();
    }

    public void showCharacterSelection() {
        if (gameController != null) {
            gameController.stopGame();
        }

        getContentPane().removeAll();

        characterSelectionPanel = new CharacterSelectionPanel();
        setContentPane(characterSelectionPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        setupCharacterSelectionInput();

        revalidate();
        repaint();
    }

    private void setupMenuInputHandling() {
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                File saveFile = new File("gamesave.dat");
                boolean saveExists = saveFile.exists();
                int optionCount = saveExists ? 3 : 2;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        selectedOption = (selectedOption - 1 + optionCount) % optionCount;
                        repaint();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        selectedOption = (selectedOption + 1) % optionCount;
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        handleMenuSelection();
                        break;
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    private void setupCharacterSelectionInput() {
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        selectedCharacter = (selectedCharacter - 1 + 3) % 3;
                        characterSelectionPanel.setSelectedCharacter(selectedCharacter);
                        repaint();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        selectedCharacter = (selectedCharacter + 1) % 3;
                        characterSelectionPanel.setSelectedCharacter(selectedCharacter);
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        showDifficultySelection();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        showMainMenu();
                        break;
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    private void handleMenuSelection() {
        File saveFile = new File("gamesave.dat");
        boolean saveExists = saveFile.exists();

        int actualSelection;
        if (saveExists) {
            actualSelection = selectedOption;
        } else {
            actualSelection = selectedOption == 0 ? 0 : 2;
        }

        switch (actualSelection) {
            case 0:
                showCharacterSelection();
                break;
            case 1:
                resumeGame();
                break;
            case 2:
                showExitImage();
                break;
        }
    }

    private void resumeGame() {
        String filename = "gamesave.dat";
        File saveFile = new File(filename);

        if (saveFile.exists()) {
            try {
                PixelGameController loadedController = PixelGameController.loadGame(filename);

                if (loadedController != null) {
                    this.gameController = loadedController;
                    initializeGamePanel();
                    setupGameInputHandling();

                    if (mazePanel != null) {
                        gameController.setMazePanel(mazePanel);
                    }

                    System.out.println("Game resumed successfully from save file");
                    System.out.println("Player position: " + gameController.getPlayer().getX() + ", " + gameController.getPlayer().getY());
                    System.out.println("Player lives: " + gameController.getPlayer().getLives());
                    System.out.println("Player shards: " + gameController.getPlayer().getShards());

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Save file is corrupted!\nStarting new game instead.",
                            "Load Error",
                            JOptionPane.WARNING_MESSAGE);
                    showCharacterSelection();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading save file: " + e.getMessage() + "\nStarting new game instead.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
                showCharacterSelection();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No saved game found!\nPlease start a new game.",
                    "No Save File",
                    JOptionPane.INFORMATION_MESSAGE);
            showCharacterSelection();
        }
    }
    private class MenuPanel extends JPanel {
        public MenuPanel() {
            setPreferredSize(new Dimension(800, 600));
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            g2d.setFont(dragonSlayerFont.deriveFont(32f));
            for (int i = 0; i < menuOptions.length; i++) {
                Color color = (i == selectedOption) ? new Color(255, 215, 0) : Color.WHITE;
                Color shadowColor = (i == selectedOption) ? new Color(180, 150, 0) : new Color(50, 50, 50);

                int yPos = 380 + i * 60;

                if (i == selectedOption && arrowIcon != null) {
                    int arrowX = getWidth() / 2 - 200;
                    int arrowY = yPos - 60;
                    g2d.drawImage(arrowIcon.getImage(), arrowX, arrowY, this);
                } else if (i == selectedOption) {
                    g2d.setColor(new Color(255, 215, 0));
                    g2d.setFont(dragonSlayerFont.deriveFont(36f));
                    g2d.drawString(">", getWidth() / 2 - 180, yPos);
                }

                g2d.setFont(dragonSlayerFont.deriveFont(32f));

                String optionText = menuOptions[i];

                if (i == 1) {
                    File saveFile = new File("gamesave.dat");
                    if (saveFile.exists()) {
                        optionText = "RESUME GAME ";
                    }
                }

                drawTextWithShadow(g2d, optionText, getWidth() / 2, yPos, color, shadowColor);
            }
        }

        private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color textColor, Color shadowColor) {
            g2d.setColor(shadowColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2 + 2, y + 2);

            g2d.setColor(textColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2, y);
        }
    }

    private void initializeGamePanel() {
        System.out.println("Initializing game panel...");

        getContentPane().removeAll();

        mazePanel = new PixelMazePanel(gameController);
        setContentPane(mazePanel);

        gameController.setMazePanel(mazePanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        revalidate();
        repaint();

        mazePanel.requestFocusInWindow();
        System.out.println("Game panel initialized - maze panel reference set in controller");
    }

    private void showPauseMenu() {
        if (gameController == null || !gameController.isGameOngoing()) return;

        String[] options = {"Resume", "Save Game", "Load Game", "Music ON/OFF", "Restart", "Main Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(this,
                "Game Paused\n\nTreasures: " + gameController.getPlayer().getTreasuresCollected() +
                        "\nPoints: " + gameController.getPlayer().getPoints() +
                        "\nShards: " + gameController.getPlayer().getShards() +
                        "\nLives: " + gameController.getPlayer().getLives() +
                        (gameController.getPlayer().hasMap() ? "\nMap: Acquired" : "\nMap: Not owned"),
                "Game Paused",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0:

                break;
            case 1:
                saveGame();
                showPauseMenu();
                return;
            case 2:
                loadGame();
                break;
            case 3:
                toggleMusic();
                showPauseMenu();
                return;
            case 4:
                gameController.stopGame();
                startNewGameWithSelectedCharacterAndDifficulty();
                break;
            case 5:
                if (gameController.isGameOngoing()) {
                    saveGame();
                }
                gameController.stopGame();
                showMainMenu();
                break;
            case 6:
                showExitImage();
                break;
        }
        if (mazePanel != null) {
            mazePanel.requestFocusInWindow();
        }
    }
    private void toggleMusic() {
        if (gameController != null) {
            SoundManager soundManager = gameController.getSoundManager();
            if (soundManager.isMusicLoaded()) {
                if (soundManager.getVolume() > 0) {
                    soundManager.setVolume(0f);
                    JOptionPane.showMessageDialog(this, "Music OFF");
                } else {
                    soundManager.setVolume(0.7f);
                    JOptionPane.showMessageDialog(this, "Music ON");
                }
            }
        }
    }

    public void showDifficultySelection() {
        if (gameController != null) {
            gameController.stopGame();
        }

        getContentPane().removeAll();

        difficultySelectionPanel = new DifficultySelectionPanel();
        difficultySelectionPanel.setSelectedDifficulty(selectedDifficulty);
        setContentPane(difficultySelectionPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        setupDifficultySelectionInput();

        revalidate();
        repaint();

        System.out.println("Showing difficulty selection with selected: " + selectedDifficulty);
    }
    private void setupDifficultySelectionInput() {
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        selectedDifficulty = (selectedDifficulty - 1 + difficultyOptions.length) % difficultyOptions.length;
                        difficultySelectionPanel.setSelectedDifficulty(selectedDifficulty);
                        repaint();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        selectedDifficulty = (selectedDifficulty + 1) % difficultyOptions.length;
                        difficultySelectionPanel.setSelectedDifficulty(selectedDifficulty);
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        startNewGameWithSelectedCharacterAndDifficulty();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        showCharacterSelection();
                        break;
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }

    private void startNewGameWithSelectedCharacterAndDifficulty() {
        System.out.println("Starting new game with character: " + selectedCharacter + " and difficulty: " + selectedDifficulty);

        // Generate random maze size based on difficulty
        Random rand = new Random();
        int baseSize;
        switch (selectedDifficulty) {
            case 0:
                baseSize = 15 + rand.nextInt(6);
                break;
            case 1:
                baseSize = 20 + rand.nextInt(6);
                break;
            case 2:
                baseSize = 25 + rand.nextInt(6);
                break;
            default:
                baseSize = 20;
        }

        System.out.println("Creating " + difficultyOptions[selectedDifficulty] + " maze: " + baseSize + "x" + baseSize);

        try {
            gameController = new PixelGameController(baseSize, baseSize, selectedCharacter, selectedDifficulty);
            initializeGamePanel();
            setupGameInputHandling();
            gameController.startGame();

            saveGame();

            mazePanel.requestFocusInWindow();
            System.out.println("Game started with character " + selectedCharacter + " on " + difficultyOptions[selectedDifficulty] + " difficulty");

        } catch (Exception e) {
            System.out.println("Error starting game: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting game: " + e.getMessage());
        }
    }
    private void checkGameStatus() {
        if (gameController != null && !gameController.isGameOngoing()) {
            if (inputTimer != null) {
                inputTimer.stop();
            }

            Timer endGameTimer = new Timer(500, e -> {
                boolean playerWon = gameController.getPlayer().isAlive();
                int treasures = gameController.getPlayer().getTreasuresCollected();
                int points = gameController.getPlayer().getPoints();

                showEndScreen(playerWon, treasures, points);
            });
            endGameTimer.setRepeats(false);
            endGameTimer.start();
        }
    }

    private void setupGameInputHandling() {
        System.out.println("Setting up game input handling...");

        KeyListener[] listeners = getKeyListeners();
        for (KeyListener listener : listeners) {
            removeKeyListener(listener);
        }

        mazePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        keys[0] = true;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        keys[1] = true;
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        keys[2] = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        keys[3] = true;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        showPauseMenu();
                        break;
                    case KeyEvent.VK_F3:
                        if (gameController != null) {
                            gameController.debugEnemies();
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        if (gameController != null) {
                            gameController.playerThrowDirectionalProjectile();
                        }
                        break;
                    case KeyEvent.VK_B:
                        if (gameController != null && gameController.isGameOngoing()) {
                            showShop();
                        }
                        break;
                    case KeyEvent.VK_F5:
                        if (gameController != null && gameController.isGameOngoing()) {
                            saveGame();
                        }
                        break;
                    case KeyEvent.VK_F9:
                        loadGame();
                        break;

                    case KeyEvent.VK_M:
                        if (gameController != null && gameController.isGameOngoing()) {
                            PixelPlayer player = gameController.getPlayer();
                            PixelMazePanel mazePanel = gameController.getMazePanel();

                            if (player != null && player.hasMap()) {
                                if (mazePanel != null) {
                                    boolean currentState = mazePanel.isMiniMapVisible();
                                    mazePanel.setShowMiniMap(!currentState);
                                    System.out.println("Mini-map " + (!currentState ? "ON" : "OFF"));
                                } else {
                                    System.out.println("Maze panel not available yet. Try moving first.");
                                }
                            } else if (player != null && !player.hasMap()) {
                                System.out.println("You need to purchase the Labyrinth Map from the shop first!");
                            }
                        }
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        keys[0] = false;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        keys[1] = false;
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        keys[2] = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        keys[3] = false;
                        break;
                }
            }
        });

        mazePanel.setFocusable(true);
        mazePanel.requestFocusInWindow();

        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mazePanel.requestFocusInWindow();
            }
        });

        if (inputTimer != null) {
            inputTimer.stop();
        }

        inputTimer = new Timer(16, e -> {
            if (gameController != null && gameController.isGameOngoing()) {
                gameController.updatePlayer(keys);
                mazePanel.repaint();
                checkGameStatus();
            }
        });
        inputTimer.start();

        System.out.println("Game input handling setup complete");
    }
    private void showShop() {
        shopPanel = new ShopPanel(gameController);

        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(shopPanel, JLayeredPane.MODAL_LAYER);
        shopPanel.setBounds(0, 0, getWidth(), getHeight());

        revalidate();
        repaint();
        shopPanel.requestFocusInWindow();
        System.out.println("Shop opened - game paused");
    }

    public PixelMazePanel getMazePanel() {
        return mazePanel;
    }

    private void showEndScreen(boolean playerWon, int treasuresCollected, int totalPoints) {
        getContentPane().removeAll();

        EndScreenPanel endPanel = new EndScreenPanel(playerWon, treasuresCollected, totalPoints);
        setContentPane(endPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        revalidate();
        repaint();

        endPanel.requestFocusInWindow();
    }

    private void saveGame() {
        String filename = "gamesave.dat";
        if (gameController != null && gameController.saveGame(filename)) {
            System.out.println("Game auto-saved successfully!");
        } else {
            System.out.println("Failed to auto-save game!");
        }
    }

    private void loadGame() {
        String filename = "gamesave.dat";

        PixelGameController loadedController = PixelGameController.loadGame(filename);

        if (loadedController != null) {
            this.gameController = loadedController;

            initializeGamePanel();
            setupGameInputHandling();

            JOptionPane.showMessageDialog(this,
                    "Game loaded successfully!",
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to load game!",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showExitImage() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/general/gamesaved.png");
            if (is != null) {
                ImageIcon exitIcon = new ImageIcon(is.readAllBytes());

                JDialog dialog = new JDialog(this, false);
                dialog.setUndecorated(true);
                dialog.setBackground(new Color(0, 0, 0, 0));

                JLabel imageLabel = new JLabel(exitIcon);
                dialog.add(imageLabel);

                imageLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        dialog.dispose();
                        dispose();
                    }
                });

                Timer timer = new Timer(10000, e -> {
                    dialog.dispose();
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();

                dialog.pack();
                dialog.setLocationRelativeTo(null);

                dialog.setVisible(true);

            } else {
                Timer timer = new Timer(2000, e -> dispose());
                timer.setRepeats(false);
                timer.start();
            }
        } catch (Exception e) {
            Timer timer = new Timer(2000, event -> dispose());
            timer.setRepeats(false);
            timer.start();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PixelGameGUI();
        });
    }
}