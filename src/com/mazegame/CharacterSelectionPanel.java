package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CharacterSelectionPanel extends JPanel {
    private Image backgroundImage;
    private int selectedCharacter = 0;
    private final int TOTAL_CHARACTERS = 3;
    private Font dragonSlayerFont;

    private ImageIcon[] characterPreviews = new ImageIcon[3];

    public CharacterSelectionPanel() {
        loadResources();
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
    }

    private void loadResources() {
        try {
            java.io.InputStream bgStream = getClass().getResourceAsStream("/general/image.png");
            if (bgStream != null) {
                backgroundImage = new ImageIcon(bgStream.readAllBytes()).getImage();
            }

            java.io.InputStream fontStream = getClass().getResourceAsStream("/dragonslayer.ttf");
            if (fontStream != null) {
                dragonSlayerFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(36f);
            } else {
                dragonSlayerFont = new Font("Arial", Font.BOLD, 36);
            }

            // Pre-load all character preview images using down_2.png
            loadCharacterPreviews();

        } catch (Exception e) {
            System.out.println("Error loading selection screen resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCharacterPreviews() {
        for (int i = 0; i < TOTAL_CHARACTERS; i++) {
            try {
                String folder = getCharacterFolder(i);
                String path = "/" + folder + "/down_2.png";

                System.out.println("Loading character " + i + " from: " + path);

                ImageIcon preview = loadScaledSprite(path, 120, 160);

                if (preview != null) {
                    System.out.println("SUCCESS: Loaded down_2.png for character " + i);
                    characterPreviews[i] = preview;
                } else {
                    System.out.println("FAILED: Could not load down_2.png for character " + i + ", creating fallback");
                    characterPreviews[i] = createFallbackPreview(i);
                }

            } catch (Exception e) {
                System.out.println("ERROR loading preview for character " + i + ": " + e.getMessage());
                characterPreviews[i] = createFallbackPreview(i);
            }
        }
    }

    private ImageIcon loadScaledSprite(String path, int width, int height) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                ImageIcon originalIcon = new ImageIcon(bytes);

                BufferedImage originalBuffered = new BufferedImage(
                        originalIcon.getIconWidth(),
                        originalIcon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2dOriginal = originalBuffered.createGraphics();
                g2dOriginal.drawImage(originalIcon.getImage(), 0, 0, null);
                g2dOriginal.dispose();

                Image scaledImage = originalBuffered.getScaledInstance(width, height, Image.SCALE_SMOOTH);

                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRect(0, 0, width, height);

                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();

                return new ImageIcon(bufferedImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading sprite from " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private ImageIcon createFallbackPreview(int characterIndex) {
        BufferedImage img = new BufferedImage(120, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN};
        g2d.setColor(colors[characterIndex % colors.length]);
        g2d.fillRoundRect(10, 10, 100, 140, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("CHAR " + (characterIndex + 1), 25, 80);
        g2d.drawString("down_2.png", 20, 100);
        g2d.drawString("MISSING", 30, 120);

        g2d.dispose();
        return new ImageIcon(img);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setFont(dragonSlayerFont.deriveFont(42f));

        drawCharacterSelection(g2d);
        g2d.setFont(dragonSlayerFont.deriveFont(16f));
        drawTextWithShadow(g2d, "USE A/D OR LEFT/RIGHT TO SELECT", getWidth() / 2, 500, Color.LIGHT_GRAY, Color.BLACK);
        drawTextWithShadow(g2d, "PRESS ENTER TO CONFIRM", getWidth() / 2, 530, Color.LIGHT_GRAY, Color.BLACK);
    }

    private void drawCharacterSelection(Graphics2D g2d) {
        int totalWidth = 600;
        int spacing = totalWidth / TOTAL_CHARACTERS;
        int startX = (getWidth() - totalWidth) / 2;
        int yPos = 300;

        for (int i = 0; i < TOTAL_CHARACTERS; i++) {
            int xPos = startX + i * spacing + spacing / 2;

            if (i == selectedCharacter) {
                g2d.setColor(new Color(255, 215, 0, 100));
                g2d.fillRoundRect(xPos - 80, yPos - 100, 160, 200, 25, 25);
                g2d.setColor(new Color(255, 215, 0));
                g2d.setStroke(new BasicStroke(4));
                g2d.drawRoundRect(xPos - 80, yPos - 100, 160, 200, 25, 25);
            }

            if (characterPreviews[i] != null) {
                Image img = characterPreviews[i].getImage();
                g2d.drawImage(img, xPos - 60, yPos - 80, this);
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(dragonSlayerFont.deriveFont(20f));
            drawTextWithShadow(g2d, "CHARACTER " + (i + 1), xPos, yPos + 90, Color.WHITE, Color.BLACK);
        }
    }



    private String getCharacterFolder(int index) {
        switch (index) {
            case 0: return "player1";
            case 1: return "player2";
            case 2: return "player3";
            default: return "player1";
        }
    }

    private void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Color textColor, Color shadowColor) {
        g2d.setColor(shadowColor);
        g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2 + 2, y + 2);
        g2d.setColor(textColor);
        g2d.drawString(text, x - g2d.getFontMetrics().stringWidth(text) / 2, y);
    }

    public int getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(int selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
        repaint();
    }

    public int getTotalCharacters() {
        return TOTAL_CHARACTERS;
    }
}