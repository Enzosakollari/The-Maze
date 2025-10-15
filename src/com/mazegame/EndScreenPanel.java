package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class EndScreenPanel extends JPanel {
    private Image backgroundImage;
    private int selectedOption = 0;
    private final String[] endOptions = {"PLAY AGAIN", "MAIN MENU", "EXIT"};
    private ImageIcon arrowIcon; // ADD THIS FIELD

    private Font dragonSlayerFont;

    public EndScreenPanel(boolean playerWon, int treasuresCollected, int totalPoints) {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        loadResources();
        setupInputHandling();
    }

    private void loadResources() {
        try {
            // Load DragonSlayer font
            InputStream fontStream = getClass().getResourceAsStream("/dragonslayer.ttf");
            if (fontStream != null) {
                dragonSlayerFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(36f);
            } else {
                dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            }

            // Load end screen background - using your end.png image
            InputStream bgStream = getClass().getResourceAsStream("/general/end.png");
            if (bgStream != null) {
                byte[] bgBytes = bgStream.readAllBytes();
                backgroundImage = new ImageIcon(bgBytes).getImage();
                System.out.println("End screen background loaded successfully");
            } else {
                System.out.println("End screen background not found at /general/end.png, using fallback");
                backgroundImage = createFallbackBackground();
            }

            // ADD THIS: Load arrow icon
            InputStream arrowStream = getClass().getResourceAsStream("/general/arrow.png");
            if (arrowStream != null) {
                ImageIcon originalArrow = new ImageIcon(arrowStream.readAllBytes());
                Image scaledArrow = originalArrow.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                arrowIcon = new ImageIcon(scaledArrow);
                System.out.println("Arrow icon loaded for end screen");
            } else {
                System.out.println("Arrow icon not found at /general/arrow.png");
                createFallbackArrow();
            }

        } catch (Exception e) {
            System.out.println("Error loading end screen resources: " + e.getMessage());
            dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            backgroundImage = createFallbackBackground();
            createFallbackArrow();
        }
    }
    private void createFallbackArrow() {
        // Create a simple fallback arrow if the image is missing
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        int[] xPoints = {5, 35, 5};
        int[] yPoints = {5, 20, 35};
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.dispose();
        arrowIcon = new ImageIcon(img);
    }
    private Image createFallbackBackground() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        // Create a dark background similar to your game's style
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40),
                800, 600, new Color(10, 10, 20));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, 800, 600);

        // Add "GAME OVER" text in the fallback
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString("GAME OVER", 280, 150);

        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.drawString("PLAY AGAIN", 300, 300);
        g2d.drawString("MAIN MENU", 300, 360);
        g2d.drawString("EXIT", 300, 420);

        g2d.dispose();
        return img;
    }

    private void setupInputHandling() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        selectedOption = (selectedOption - 1 + endOptions.length) % endOptions.length;
                        repaint();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        selectedOption = (selectedOption + 1) % endOptions.length;
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        handleSelection();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        returnToMainMenu();
                        break;
                }
            }
        });
    }

    private void handleSelection() {
        switch (selectedOption) {
            case 0: // PLAY AGAIN
                playAgain();
                break;
            case 1: // MAIN MENU
                returnToMainMenu();
                break;
            case 2: // EXIT
                System.exit(0);
                break;
        }
    }

    private void playAgain() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof PixelGameGUI) {
            PixelGameGUI gameGUI = (PixelGameGUI) parentFrame;
            gameGUI.showCharacterSelection();
        }
    }

    private void returnToMainMenu() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof PixelGameGUI) {
            PixelGameGUI gameGUI = (PixelGameGUI) parentFrame;
            gameGUI.showMainMenu();
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw the background image (your end.png)
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Draw arrows for menu selection
        drawArrows(g2d);
    }
    private void drawArrows(Graphics2D g2d) {
        // Define positions for the arrows based on where the text is in your image
        int[] optionYPositions = {270, 335, 390}; // Adjust these based on your image layout
        int arrowX = 200; // Position to the left of the text

        for (int i = 0; i < endOptions.length; i++) {
            if (i == selectedOption) {
                if (arrowIcon != null) {
                    int arrowY = optionYPositions[i] - 20; // Adjust Y position
                    g2d.drawImage(arrowIcon.getImage(), arrowX, arrowY, this);
                } else {
                    // Fallback: red > character
                    g2d.setColor(Color.RED);
                    g2d.setFont(new Font("Arial", Font.BOLD, 36));
                    g2d.drawString(">", arrowX, optionYPositions[i]);
                }
            }
        }
    }

    private void drawRedArrows(Graphics2D g2d) {
        // Define positions for the arrows based on where the text is in your image
        int[] optionYPositions = {310, 390, 440}; // Adjust these based on your image layout
        int arrowX = 250; // Position to the left of the text

        for (int i = 0; i < endOptions.length; i++) {
            if (i == selectedOption) {
                drawRedArrow(g2d, arrowX, optionYPositions[i]);
            }
        }
    }

    private void drawRedArrow(Graphics2D g2d, int x, int y) {
        // Set red color for the arrow
        g2d.setColor(Color.RED);

        // Make the arrow thicker and larger
        g2d.setFont(new Font("Arial", Font.BOLD, 36));

        // Simply draw the > character
        g2d.drawString(">", x, y);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}