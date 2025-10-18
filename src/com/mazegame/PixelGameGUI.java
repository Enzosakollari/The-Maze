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


    // Menu navigation
    private int selectedOption = 0;
    private final String[] menuOptions = {"START GAME", "RESUME GAME"}; // Changed from "EXIT" to "RESUME GAME"
    private DifficultySelectionPanel difficultySelectionPanel;
    private int selectedDifficulty = 0;
    private final String[] difficultyOptions = {"EASY", "MEDIUM", "HARD"};
    private Font dragonSlayerFont;
    private Image backgroundImage;
    private Image iconImage;
    private ImageIcon arrowIcon;

    private int selectedCharacter = 0; // Track selected character

    public PixelGameGUI() {
        super("Pixel Maze Adventure");
        loadResources();
        setCustomIcon();
        initializeMainFrame();
    }

    // Custom panel for difficulty selection (using your background image and arrow icon)
    private class DifficultySelectionPanel extends JPanel {
        private int currentSelectedDifficulty = 0;
        private Image difficultyBackgroundImage;

        // Adjust these positions to match your image layout
        private final int[] optionYPositions = {220, 300, 370}; // Y positions for EASY, MEDIUM, HARD
        private final int indicatorX = 230; // X position for the arrow

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
                    difficultyBackgroundImage = null;
                }
            } catch (Exception e) {
                System.out.println("Error loading difficulty background: " + e.getMessage());
                difficultyBackgroundImage = null;
            }
        }

        public void setSelectedDifficulty(int difficulty) {
            this.currentSelectedDifficulty = difficulty;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw the difficulty background image
            if (difficultyBackgroundImage != null) {
                g2d.drawImage(difficultyBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw fallback text if image is missing
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.drawString("SELECT DIFFICULTY", getWidth()/2 - 180, 150);

                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                g2d.drawString("EASY", getWidth()/2 - 50, 300);
                g2d.drawString("MEDIUM", getWidth()/2 - 70, 380);
                g2d.drawString("HARD", getWidth()/2 - 50, 460);
            }

            // Draw arrow icon for selected option
            if (arrowIcon != null) {
                int yPos = optionYPositions[currentSelectedDifficulty] - 20; // Adjust Y position to center with text
                g2d.drawImage(arrowIcon.getImage(), indicatorX, yPos, this);
            } else {
                // Fallback: red > text
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                int yPos = optionYPositions[currentSelectedDifficulty];
                g2d.drawString(">", indicatorX, yPos);
            }
        }
    }

    private void loadResources() {
        try {
            // Load DragonSlayer font
            java.io.InputStream fontStream = getClass().getResourceAsStream("/dragonslayer.ttf");
            if (fontStream != null) {
                dragonSlayerFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(36f);
                System.out.println("DragonSlayer font loaded successfully");
            } else {
                System.out.println("DragonSlayer font not found, using fallback");
                dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            }

            // Load background image
            java.io.InputStream bgStream = getClass().getResourceAsStream("/general/background.png");
            if (bgStream != null) {
                backgroundImage = new ImageIcon(bgStream.readAllBytes()).getImage();
                System.out.println("Background image loaded successfully");
            } else {
                System.out.println("Background image not found, using fallback");
                backgroundImage = createFallbackBackground();
            }

            // Load window icon
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
                // Scale the arrow to appropriate size (adjust as needed)
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

        // Draw a simple maze-like icon
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
        // Create a simple fallback arrow if the image is missing
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Enable anti-aliasing for smooth arrow
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a gold arrow (matching menu selection color)
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

        // Show main menu immediately
        showMainMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showMainMenu() {
        // Stop music if coming back from game
        if (gameController != null) {
            gameController.stopGame();
        }

        getContentPane().removeAll();

        // Create a custom panel with background and menu
        MenuPanel menuPanel = new MenuPanel();
        setContentPane(menuPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        // Setup menu navigation input
        setupMenuInputHandling();

        revalidate();
        repaint();
    }

    public void showCharacterSelection() {
        // Stop music if coming back from game
        if (gameController != null) {
            gameController.stopGame();
        }

        getContentPane().removeAll();

        characterSelectionPanel = new CharacterSelectionPanel();
        setContentPane(characterSelectionPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        // Setup character selection input
        setupCharacterSelectionInput();

        revalidate();
        repaint();
    }

    private void setupMenuInputHandling() {
        // Clear existing listeners
        for (KeyListener listener : getKeyListeners()) {
            removeKeyListener(listener);
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                        repaint();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        selectedOption = (selectedOption + 1) % menuOptions.length;
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
        // Clear existing listeners
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
        switch (selectedOption) {
            case 0: // START GAME
                showCharacterSelection();
                break;
            case 1: // RESUME GAME (was EXIT)
                resumeGame();
                break;
        }
    }

    // ADD THIS METHOD to resume game
    private void resumeGame() {
        String filename = "gamesave.dat";
        File saveFile = new File(filename);

        if (saveFile.exists()) {
            // Load the saved game
            PixelGameController loadedController = PixelGameController.loadGame(filename);

            if (loadedController != null) {
                this.gameController = loadedController;
                initializeGamePanel();
                setupGameInputHandling();

                // Restore the maze panel reference
                gameController.setMazePanel(mazePanel);

                System.out.println("Game resumed from save file");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to load saved game!\nStarting new game instead.",
                        "Load Error",
                        JOptionPane.WARNING_MESSAGE);
                showCharacterSelection(); // Fallback to new game
            }
        } else {
            // No save file exists
            JOptionPane.showMessageDialog(this,
                    "No saved game found!\nPlease start a new game.",
                    "No Save File",
                    JOptionPane.INFORMATION_MESSAGE);
            showCharacterSelection(); // Go to character selection
        }
    }

    // Custom panel for the menu
    private class MenuPanel extends JPanel {
        public MenuPanel() {
            setPreferredSize(new Dimension(800, 600));
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing for smoother text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw background image
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            // Draw menu options with arrow icon indicator
            g2d.setFont(dragonSlayerFont.deriveFont(32f));
            for (int i = 0; i < menuOptions.length; i++) {
                Color color = (i == selectedOption) ? new Color(255, 215, 0) : Color.WHITE;
                Color shadowColor = (i == selectedOption) ? new Color(180, 150, 0) : new Color(50, 50, 50);

                int yPos = 380 + i * 60;

                // Draw selection indicator (arrow icon)
                if (i == selectedOption && arrowIcon != null) {
                    int arrowX = getWidth() / 2 - 200; // Position arrow to the left of text
                    int arrowY = yPos - 60; // Adjust Y to center with text
                    g2d.drawImage(arrowIcon.getImage(), arrowX, arrowY, this);
                } else if (i == selectedOption) {
                    // Fallback: > character
                    g2d.setColor(new Color(255, 215, 0));
                    g2d.setFont(dragonSlayerFont.deriveFont(36f));
                    g2d.drawString(">", getWidth() / 2 - 180, yPos);
                }

                // Draw menu option text
                g2d.setFont(dragonSlayerFont.deriveFont(32f));

                String optionText = menuOptions[i];

                // Add indicator if RESUME GAME and save file exists
                if (i == 1) { // RESUME GAME option
                    File saveFile = new File("gamesave.dat");
                    if (saveFile.exists()) {
                        optionText = "RESUME GAME ●"; // Add a dot indicator
                    }
                }

                drawTextWithShadow(g2d, optionText, getWidth() / 2, yPos, color, shadowColor);
            }
        }

        private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color textColor, Color shadowColor) {
            // Draw shadow
            g2d.setColor(shadowColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2 + 2, y + 2);

            // Draw main text
            g2d.setColor(textColor);
            g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2, y);
        }
    }

    private void initializeGamePanel() {
        System.out.println("Initializing game panel...");

        getContentPane().removeAll();

        mazePanel = new PixelMazePanel(gameController);
        setContentPane(mazePanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        revalidate();
        repaint();
        System.out.println("Game panel initialized");
    }

    private void showPauseMenu() {
        if (gameController == null || !gameController.isGameOngoing()) return;

        String[] options = {"Resume", "Save Game", "Load Game", "Music ON/OFF", "Restart", "Main Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(this,
                "Game Paused\n\nTreasures: " + gameController.getPlayer().getTreasuresCollected() +
                        "\nPoints: " + gameController.getPlayer().getPoints() +
                        "\nShards: " + gameController.getPlayer().getShards(),
                "Game Paused",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 1: // Save Game
                saveGame();
                showPauseMenu(); // Show menu again
                return;
            case 2: // Load Game
                loadGame();
                break;
            case 3: // Music ON/OFF
                toggleMusic();
                showPauseMenu();
                return;
            case 4: // Restart
                gameController.stopGame();
                startNewGameWithSelectedCharacterAndDifficulty();
                break;
            case 5: // Main Menu
                gameController.stopGame();
                showMainMenu();
                break;
            case 6: // Exit
                System.exit(0);
                break;
            // case 0 is Resume - do nothing
        }

        if (mazePanel != null) {
            mazePanel.requestFocusInWindow();
        }
    }

    private void toggleMusic() {
        if (gameController != null) {
            SoundManager soundManager = gameController.getSoundManager();
            if (soundManager.isMusicLoaded()) {
                // Toggle between volume on and off
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
        setContentPane(difficultySelectionPanel);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        setupDifficultySelectionInput();

        revalidate();
        repaint();
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
            case 0: // EASY
                baseSize = 15 + rand.nextInt(6); // 15-20 (smaller)
                break;
            case 1: // MEDIUM
                baseSize = 20 + rand.nextInt(6); // 20-25
                break;
            case 2: // HARD
                baseSize = 25 + rand.nextInt(6); // 25-30
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

            // Small delay to let the game end properly
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

        // Clear existing listeners
        KeyListener[] listeners = getKeyListeners();
        for (KeyListener listener : listeners) {
            removeKeyListener(listener);
        }

        // Add key listener to the mazePanel
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
                    // ADD THIS: B key for shop
                    case KeyEvent.VK_B:
                        if (gameController != null && gameController.isGameOngoing()) {
                            showShop();
                        }
                        break;
                    case KeyEvent.VK_F5: // Save game
                        if (gameController != null && gameController.isGameOngoing()) {
                            saveGame();
                        }
                        break;
                    case KeyEvent.VK_F9: // Load game
                        loadGame();
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

        // CRITICAL: Ensure mazePanel can get mouse focus for projectile throwing
        mazePanel.setFocusable(true);
        mazePanel.requestFocusInWindow();

        // Enable mouse events on the maze panel
        mazePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Ensure the maze panel has focus when clicked
                mazePanel.requestFocusInWindow();
            }
        });

        // Stop existing timer
        if (inputTimer != null) {
            inputTimer.stop();
        }

        // Start game loop
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

        // Use JLayeredPane instead of replacing content
        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(shopPanel, JLayeredPane.MODAL_LAYER);
        shopPanel.setBounds(0, 0, getWidth(), getHeight());

        revalidate();
        repaint();
        shopPanel.requestFocusInWindow();
        System.out.println("Shop opened - game paused");
    }

    // Add getter for maze panel in PixelGameController
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

        // Ensure the end screen gets focus for input
        endPanel.requestFocusInWindow();
    }

    // ADD THESE METHODS for save/load functionality
    private void saveGame() {
        String filename = "gamesave.dat";
        if (gameController.saveGame(filename)) {
            JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save game!", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGame() {
        String filename = "gamesave.dat";
        PixelGameController loadedController = PixelGameController.loadGame(filename);

        if (loadedController != null) {
            this.gameController = loadedController;
            initializeGamePanel();
            setupGameInputHandling();

            // Restore the maze panel reference
            gameController.setMazePanel(mazePanel);

            JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to load game!", "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method to start the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PixelGameGUI();
        });
    }
}