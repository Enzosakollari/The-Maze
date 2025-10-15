package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

public class PixelMazePanel extends JPanel {
    private static final int TILE_SIZE = 64;
    private PixelGameController gameController;
    private ImageIcon pathIcon, wallIcon, treasureIcon, exitIcon;
    private boolean useImages = true;
    private ImageIcon lifeIcon;
    private ImageIcon lifePotionIcon;

    private float cameraX, cameraY;
    private int viewportWidth = 800;
    private int viewportHeight = 600;
    private float cameraFollowSpeed = 0.08f;

    private Timer renderTimer;

    public PixelMazePanel(PixelGameController gameController) {
        this.gameController = gameController;
        setPreferredSize(new Dimension(viewportWidth, viewportHeight));
        setBackground(new Color(20, 20, 30));

        setFocusable(true);
        setRequestFocusEnabled(true);

        initializeIcons();
        setupMouseInput();
        setupRenderLoop();
    }

    private void initializeIcons() {
        try {
            System.out.println("=== ATTEMPTING TO LOAD TILES ===");

            // Try loading each tile with detailed debug info
            pathIcon = loadAndScaleImage("/tiles/path4.png", TILE_SIZE);
            wallIcon = loadAndScaleImage("/tiles/wall3.png", TILE_SIZE);
            treasureIcon = loadAndScaleImage("/tiles/treasure.png", TILE_SIZE);
            lifePotionIcon = loadAndScaleImage("/tiles/life_potion.png", TILE_SIZE);
            exitIcon = loadAndScaleImage("/tiles/exit.png", TILE_SIZE);
            lifeIcon = loadAndScaleImage("/tiles/life.png", TILE_SIZE);

            // Also try with tiles/ prefix
            if (pathIcon == null) pathIcon = loadAndScaleImage("/tiles/path4.png", TILE_SIZE);
            if (wallIcon == null) wallIcon = loadAndScaleImage("/tiles/wall3.png", TILE_SIZE);
            if (treasureIcon == null) treasureIcon = loadAndScaleImage("/tiles/treasure.png", TILE_SIZE);
            if (exitIcon == null) exitIcon = loadAndScaleImage("/tiles/exit.png", TILE_SIZE);
            if (lifeIcon == null) lifeIcon = loadAndScaleImage("/tiles/life.png", TILE_SIZE);
            if (lifePotionIcon == null) lifePotionIcon = loadAndScaleImage("/tiles/life_potion.png", TILE_SIZE);

            System.out.println("=== TILE LOADING RESULTS ===");
            System.out.println("Path tile: " + (pathIcon != null ? "LOADED" : "MISSING"));
            System.out.println("Wall tile: " + (wallIcon != null ? "LOADED" : "MISSING"));
            System.out.println("Treasure tile: " + (treasureIcon != null ? "LOADED" : "MISSING"));
            System.out.println("Exit tile: " + (exitIcon != null ? "LOADED" : "MISSING"));
            System.out.println("Life tile: " + (lifeIcon != null ? "LOADED" : "MISSING"));
            System.out.println("Life Potion tile: " + (lifePotionIcon != null ? "LOADED" : "MISSING"));

            if (pathIcon != null && wallIcon != null && treasureIcon != null && exitIcon != null) {
                useImages = true;
                System.out.println("SUCCESS: All tiles loaded at 64x64 (may be blurry if original size differs)");
            } else {
                useImages = false;
                System.out.println("FALLBACK: Using colored tiles instead of images");
            }

        } catch (Exception e) {
            useImages = false;
            System.out.println("ERROR loading tiles: " + e.getMessage());
        }
    }

    private ImageIcon loadAndScaleImage(String path, int size) {
        try {
            System.out.println("Trying to load: '" + path + "'");
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                System.out.println("SUCCESS: Found " + path);
                byte[] bytes = is.readAllBytes();
                ImageIcon originalIcon = new ImageIcon(bytes);
                System.out.println("Original size: " + originalIcon.getIconWidth() + "x" + originalIcon.getIconHeight());

                Image scaledImage = originalIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);
                System.out.println("Scaled to: " + scaledIcon.getIconWidth() + "x" + scaledIcon.getIconHeight());
                return scaledIcon;
            } else {
                System.out.println("FAILED: " + path + " not found");
            }
        } catch (Exception e) {
            System.out.println("ERROR loading " + path + ": " + e.getMessage());
        }
        return null;
    }

    private void setupMouseInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameController != null && gameController.isGameOngoing()) {
                    // Convert screen coordinates to world coordinates
                    float worldX = e.getX() + cameraX;
                    float worldY = e.getY() + cameraY;
                    gameController.playerThrowProjectile((int)worldX, (int)worldY);
                    System.out.println("Throwing dagger at: " + worldX + ", " + worldY);
                }
            }
        });
    }

    private void setupRenderLoop() {
        renderTimer = new Timer(16, e -> {
            updateCamera();
            repaint();
        });
        renderTimer.start();
    }

    private void updateCamera() {
        PixelPlayer player = gameController.getPlayer();
        if (player != null) {
            // Calculate target camera position (center on player)
            float playerCenterX = player.getX() + player.getWidth() / 2;
            float playerCenterY = player.getY() + player.getHeight() / 2;

            float targetX = playerCenterX - viewportWidth / 2;
            float targetY = playerCenterY - viewportHeight / 2;

            // Smooth camera follow
            cameraX = lerp(cameraX, targetX, cameraFollowSpeed);
            cameraY = lerp(cameraY, targetY, cameraFollowSpeed);

            // Keep camera within maze bounds
            PixelMaze maze = gameController.getMaze();
            int mazePixelWidth = maze.getWidth() * TILE_SIZE;
            int mazePixelHeight = maze.getHeight() * TILE_SIZE;

            cameraX = Math.max(0, Math.min(cameraX, mazePixelWidth - viewportWidth));
            cameraY = Math.max(0, Math.min(cameraY, mazePixelHeight - viewportHeight));
        }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (gameController.getMaze() == null) return;

        drawMaze(g2d);
        drawPlayer(g2d);
        drawEnemies(g2d);
        drawProjectiles(g2d);
        drawHUD(g2d);
    }

    private void drawMaze(Graphics2D g2d) {
        PixelMaze maze = gameController.getMaze();
        char[][] grid = maze.getGrid();

        // Calculate visible tile range for optimization
        int startTileX = Math.max(0, (int)(cameraX / TILE_SIZE));
        int startTileY = Math.max(0, (int)(cameraY / TILE_SIZE));
        int endTileX = Math.min(maze.getWidth(), (int)((cameraX + viewportWidth) / TILE_SIZE) + 1);
        int endTileY = Math.min(maze.getHeight(), (int)((cameraY + viewportHeight) / TILE_SIZE) + 1);

        // Draw visible tiles only
        for (int y = startTileY; y < endTileY; y++) {
            for (int x = startTileX; x < endTileX; x++) {
                int screenX = (int)(x * TILE_SIZE - cameraX);
                int screenY = (int)(y * TILE_SIZE - cameraY);

                drawTile(g2d, grid[y][x], screenX, screenY);
            }
        }
    }

    private void drawTile(Graphics2D g2d, char tileType, int x, int y) {
        if (useImages) {
            ImageIcon icon = getTileIcon(tileType);
            if (icon != null) {
                g2d.drawImage(icon.getImage(), x, y, this);
            }
        } else {
            // Colored fallback
            Color color = getTileColor(tileType);
            g2d.setColor(color);
            g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    private ImageIcon getTileIcon(char tileType) {
        switch (tileType) {
            case '.': return pathIcon;
            case '#': return wallIcon;
            case 'T': return treasureIcon;
            case 'E': return exitIcon;
            case 'S': return pathIcon;
            case 'L': return lifePotionIcon;
            default: return pathIcon;
        }
    }

    private Color getTileColor(char tileType) {
        switch (tileType) {
            case '.': return new Color(220, 220, 220);
            case '#': return new Color(40, 40, 80);
            case 'T': return Color.YELLOW;
            case 'E': return Color.GREEN;
            case 'S': return new Color(180, 180, 255);
            case 'L': return Color.PINK;
            default: return Color.GRAY;
        }
    }

    private void drawPlayer(Graphics2D g2d) {
        PixelPlayer player = gameController.getPlayer();
        if (player != null) {
            // Convert world coordinates to screen coordinates
            int screenX = (int)(player.getX() - cameraX);
            int screenY = (int)(player.getY() - cameraY);

            ImageIcon playerSprite = player.getCurrentSprite();
            if (playerSprite != null) {
                g2d.drawImage(playerSprite.getImage(), screenX, screenY, this);
            } else {
                // Fallback
                g2d.setColor(Color.RED);
                g2d.fillRect(screenX, screenY, player.getWidth(), player.getHeight());
            }
        }
    }

    private void drawEnemies(Graphics2D g2d) {
        java.util.List<PixelEnemy> enemies = gameController.getEnemies();

        if (enemies != null) {
            for (PixelEnemy enemy : enemies) {
                if (enemy == null || !enemy.isAlive()) continue;

                int screenX = (int)(enemy.getX() - cameraX);
                int screenY = (int)(enemy.getY() - cameraY);

                // Draw enemy sprite (with flashing effect if damaged)
                ImageIcon enemySprite = enemy.getCurrentSprite();
                if (enemySprite != null) {
                    if (enemy.isFlashing()) {
                        // Apply red tint when enemy is flashing from damage
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2d.setColor(Color.RED);
                        g2d.fillRect(screenX, screenY, enemy.getWidth(), enemy.getHeight());
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                    g2d.drawImage(enemySprite.getImage(), screenX, screenY, this);
                }

                // Draw health bar above enemy
                drawEnemyHealthBar(g2d, enemy, screenX, screenY);
            }
        }
    }

    private void drawEnemyHealthBar(Graphics2D g2d, PixelEnemy enemy, int screenX, int screenY) {
        int healthBarWidth = enemy.getWidth();
        int healthBarHeight = 6;
        int healthBarY = screenY - 10; // Position above enemy

        // Health bar background (red)
        g2d.setColor(Color.RED);
        g2d.fillRect(screenX, healthBarY, healthBarWidth, healthBarHeight);

        // Health bar foreground (green) based on current health
        float healthPercent = (float) enemy.getCurrentHealth() / enemy.getMaxHealth();
        int currentHealthWidth = (int)(healthBarWidth * healthPercent);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(screenX, healthBarY, currentHealthWidth, healthBarHeight);

        // Health bar border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(screenX, healthBarY, healthBarWidth, healthBarHeight);
    }

    private void drawProjectiles(Graphics2D g2d) {
        PixelPlayer player = gameController.getPlayer();
        if (player != null) {
            java.util.List<Projectile> projectiles = player.getProjectiles();

            for (Projectile projectile : projectiles) {
                if (!projectile.isActive()) continue;

                int screenX = (int)(projectile.getX() - cameraX);
                int screenY = (int)(projectile.getY() - cameraY);

                ImageIcon projectileSprite = projectile.getSprite();
                if (projectileSprite != null) {
                    g2d.drawImage(projectileSprite.getImage(), screenX, screenY, this);
                } else {
                    // Fallback dagger shape
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(screenX + 8, screenY + 12, 16, 8); // Blade
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(screenX + 12, screenY + 8, 8, 16); // Handle
                }
            }
        }
    }

    private void drawHUD(Graphics2D g2d) {
        PixelPlayer player = gameController.getPlayer();

        // Only draw life icons
        drawLifeIcons(g2d, player.getLives());

        // Show invulnerability status if needed
        if (player.isInvulnerable()) {
            g2d.setColor(new Color(255, 255, 0, 150));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("INVULNERABLE", 150, 90);
        }

        // Optional: Keep the throwing instructions
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Click to throw daggers at enemies!", 15, getHeight() - 20);
    }
    private void drawLifeIcons(Graphics2D g2d, int lives) {
        int startX = 20;
        int startY = getHeight() - 60;
        int spacing = 50;
        int iconSize = 30;

        for (int i = 0; i < lives; i++) {
            int x = startX + (i * spacing);

            if (lifeIcon != null) {
                g2d.drawImage(lifeIcon.getImage(), x, startY, this);
            } else {
                // Fallback life icon
                g2d.setColor(Color.RED);
                g2d.fillOval(x, startY, iconSize, iconSize);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("♥", x + 10, startY + 20);
            }
        }
    }

    public void stopRendering() {
        if (renderTimer != null) {
            renderTimer.stop();
        }
    }
}