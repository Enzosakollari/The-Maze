package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class ShopPanel extends JPanel {
    private PixelGameController gameController;
    private ImageIcon shopBackground;
    private ImageIcon arrowIcon;
    private ImageIcon warningIcon;
    private ImageIcon congratsIcon;


    private boolean shopOpen = true;

    private Rectangle sanguineVigorArea = new Rectangle(300, 250, 150, 50);
    private Rectangle scarletBoltArea = new Rectangle(500, 250, 150, 50);
    private Rectangle labyrinthMapArea = new Rectangle(300, 350, 150, 50);
    private Rectangle closeArea = new Rectangle(350, 500, 100, 30);

    // Track selected item
    private int selectedItem = 0;
    private final String[] itemNames = {"SANGUINE VIGOR", "SCARLET BOLT", "LABYRINTH MAP"};
    private final int[] itemPrices = {250, 180, 180};

    // Arrow positions for horizontal layout
    private final int[][] arrowPositions = {
            {170, 370},
            {370, 370},
            {570, 370}
    };

    public ShopPanel(PixelGameController gameController) {
        this.gameController = gameController;
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        loadShopBackground();
        loadArrowIcon();
        loadWarningIcon();
        loadCongratsIcon();

        setupMouseListener();
        setupKeyListener();
        gameController.setPaused(true);
    }

    private void applyItemEffect(String item) {
        PixelPlayer player = gameController.getPlayer();
        switch (item) {
            case "SANGUINE VIGOR":
                player.doubleSpeed();
                System.out.println("Max health increased! Lives: " + player.getLives());
                break;
            case "SCARLET BOLT":
                player.setProjectileType("spell");
                System.out.println("Projectiles changed to spells! Damage: " + player.getProjectileDamage());
                break;
            case "LABYRINTH MAP":
                System.out.println("Labyrinth Map purchased!");
                player.setHasMap(true);
                PixelMazePanel mazePanel = gameController.getMazePanel();
                if (mazePanel != null) {
                    mazePanel.setShowMiniMap(true);
                    System.out.println("Mini-map enabled! Press M to toggle.");
                } else {
                    System.out.println("Maze panel not available, but map acquired. Mini-map will work when available.");
                }
                break;
        }
    }
    private void loadCongratsIcon() {
        try {
            java.io.InputStream congratsStream = getClass().getResourceAsStream("/general/congrats.png");
            if (congratsStream != null) {
                ImageIcon originalCongrats = new ImageIcon(congratsStream.readAllBytes());
                Image scaledCongrats = originalCongrats.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                congratsIcon = new ImageIcon(scaledCongrats);
                System.out.println("Congrats icon loaded successfully");
            } else {
                System.out.println("Congrats icon not found at /general/congrats.png");
                createFallbackCongratsIcon();
            }
        } catch (Exception e) {
            System.out.println("Error loading congrats icon: " + e.getMessage());
            createFallbackCongratsIcon();
        }
    }
    private void createFallbackCongratsIcon() {
        BufferedImage img = new BufferedImage(350, 250, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 150, 0, 200));
        g2d.fillRoundRect(0, 0, 300, 200, 20, 20);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("PURCHASE", 80, 80);
        g2d.drawString("SUCCESSFUL!", 70, 110);

        // Add some celebration stars
        g2d.setColor(Color.WHITE);
        g2d.fillRect(50, 130, 10, 10);
        g2d.fillRect(120, 140, 8, 8);
        g2d.fillRect(200, 135, 12, 12);

        g2d.dispose();
        congratsIcon = new ImageIcon(img);
    }
    private void loadWarningIcon() {
        try {
            java.io.InputStream warningStream = getClass().getResourceAsStream("/general/warning.png");
            if (warningStream != null) {
                ImageIcon originalWarning = new ImageIcon(warningStream.readAllBytes());
                Image scaledWarning = originalWarning.getImage().getScaledInstance(350, 250, Image.SCALE_SMOOTH);
                warningIcon = new ImageIcon(scaledWarning);
                System.out.println("Warning icon loaded successfully");
            } else {
                System.out.println("Warning icon not found at /general/warning.png");
                createFallbackWarningIcon();
            }
        } catch (Exception e) {
            System.out.println("Error loading warning icon: " + e.getMessage());
            createFallbackWarningIcon();
        }
    }
    private void createFallbackWarningIcon() {
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.YELLOW);
        int[] xPoints = {40, 10, 70};
        int[] yPoints = {10, 70, 70};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(38, 20, 4, 30);
        g2d.fillRect(38, 55, 4, 10);

        g2d.dispose();
        warningIcon = new ImageIcon(img);
    }
    private void loadShopBackground() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/general/shop.png");
            if (is != null) {
                ImageIcon original = new ImageIcon(is.readAllBytes());
                Image scaled = original.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
                shopBackground = new ImageIcon(scaled);
                System.out.println("Shop background loaded successfully");
            } else {
                System.out.println("Shop background not found at /general/shop.png");
            }
        } catch (Exception e) {
            System.out.println("Could not load shop background: " + e.getMessage());
        }
    }

    private void loadArrowIcon() {
        try {
            java.io.InputStream arrowStream = getClass().getResourceAsStream("/general/arrowUp.png");
            if (arrowStream != null) {
                ImageIcon originalArrow = new ImageIcon(arrowStream.readAllBytes());
                Image scaledArrow = originalArrow.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                arrowIcon = new ImageIcon(scaledArrow);
                System.out.println("ArrowUp icon loaded successfully");
            } else {
                System.out.println("ArrowUp icon not found, creating fallback");
                createFallbackArrow();
            }
        } catch (Exception e) {
            System.out.println("Error loading arrow icon: " + e.getMessage());
            createFallbackArrow();
        }
    }

    private void createFallbackArrow() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.YELLOW);
        int[] xPoints = {25, 5, 45};
        int[] yPoints = {5, 45, 45};
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.dispose();
        arrowIcon = new ImageIcon(img);
    }

    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (sanguineVigorArea.contains(x, y)) {
                    selectedItem = 0;
                    buySelectedItem();
                } else if (scarletBoltArea.contains(x, y)) {
                    selectedItem = 1;
                    buySelectedItem();
                } else if (labyrinthMapArea.contains(x, y)) {
                    selectedItem = 2;
                    buySelectedItem();
                } else if (closeArea.contains(x, y)) {
                    closeShop();
                }
            }
        });
    }

    private void setupKeyListener() {
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        selectedItem = (selectedItem - 1 + 3) % 3;
                        repaint();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        selectedItem = (selectedItem + 1) % 3;
                        repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        buySelectedItem();
                        break;
                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_B:
                        closeShop();
                        break;
                }
            }
        });
    }

    private void buySelectedItem() {
        String item = itemNames[selectedItem];
        int cost = itemPrices[selectedItem];

        PixelPlayer player = gameController.getPlayer();
        if (player.getShards() >= cost) {
            player.deductShards(cost);
            applyItemEffect(item);
            showCongratsImage();
        } else {
            showWarningImage();
        }
        repaint();
    }
    private void showCongratsImage() {
        JLabel congratsLabel = new JLabel(congratsIcon);
        congratsLabel.setBounds(250, 200, 300, 200);

        add(congratsLabel);
        revalidate();
        repaint();

        Timer timer = new Timer(2000, e -> {
            remove(congratsLabel);
            revalidate();
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showWarningImage() {
        JLabel warningLabel = new JLabel(warningIcon);
        warningLabel.setBounds(225, 175, 350, 250);

        add(warningLabel);
        revalidate();
        repaint();

        Timer timer = new Timer(1500, e -> {
            remove(warningLabel);
            revalidate();
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showPurchaseMessage(String message, Color color) {
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(color);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setBounds(200, 550, 400, 30);
        messageLabel.setOpaque(true);
        messageLabel.setBackground(new Color(0, 0, 0, 200));

        add(messageLabel);
        revalidate();
        repaint();

        Timer timer = new Timer(2000, e -> {
            remove(messageLabel);
            revalidate();
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }


    private void closeShop() {
        shopOpen = false;
        gameController.setPaused(false);

        if (getParent() != null) {
            Container parent = getParent();
            parent.remove(this);
            parent.revalidate();
            parent.repaint();
        }

        if (gameController != null && gameController.getMazePanel() != null) {
            gameController.getMazePanel().requestFocusInWindow();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (shopBackground != null) {
            g2d.drawImage(shopBackground.getImage(), 0, 0, this);
        } else {
            drawFallbackShop(g2d);
        }

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(""+ gameController.getPlayer().getShards(), 320, 155);

        drawSelectionArrow(g2d);

        drawSelectionInfo(g2d);
    }

    private void drawSelectionArrow(Graphics2D g2d) {
        if (arrowIcon != null) {
            int arrowX = arrowPositions[selectedItem][0];
            int arrowY = arrowPositions[selectedItem][1];

            g2d.drawImage(arrowIcon.getImage(), arrowX, arrowY, this);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            int arrowX = arrowPositions[selectedItem][0] + 20;
            int arrowY = arrowPositions[selectedItem][1] + 20;
            g2d.drawString("^", arrowX, arrowY);
        }
    }

    private void drawSelectionInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        g2d.drawString("Press ENTER to buy, A/D to navigate, ESC to close", 200, 540);
    }

    private void drawFallbackShop(Graphics2D g2d) {
        g2d.setColor(new Color(30, 10, 40));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("THE MAZE SHOP", 300, 100);
        g2d.drawString("YOUR SHARDS: " + gameController.getPlayer().getShards(), 300, 150);

        String[] items = {"SANGUINE VIGOR", "SCARLET BOLT", "LABYRINTH MAP"};
        int[] prices = {250, 180, 180};

        for (int i = 0; i < items.length; i++) {
            int x = 200 + i * 200;
            int y = 300;

            if (i == selectedItem) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("^", x + 40, y - 20); // Arrow above selected item
                g2d.drawString(items[i], x, y);
                g2d.drawString(prices[i] + " shards", x, y + 30);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString(items[i], x, y);
                g2d.drawString(prices[i] + " shards", x, y + 30);
            }
        }
    }

    public boolean isShopOpen() {
        return shopOpen;
    }
}