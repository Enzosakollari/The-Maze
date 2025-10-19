package com.mazegame;

import javax.swing.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.*;

public class PixelGameController implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient PixelMazePanel mazePanel;
    private transient SoundManager soundManager;

    private PixelPlayer player;
    private PixelMaze maze;
    private boolean gameOngoing;
    private Random random;
    private List<PixelEnemy> enemies;
    private boolean paused = false;

    private int currentDifficulty;
    private int currentCharacterIndex;

    public PixelGameController(int width, int height, int characterIndex, int difficulty) {
        this.random = new Random();
        this.maze = new PixelMaze(width, height, difficulty);
        this.player = new PixelPlayer(characterIndex);
        this.enemies = new ArrayList<>();
        this.gameOngoing = false;
        this.soundManager = new SoundManager();
        this.currentDifficulty = difficulty;
        this.currentCharacterIndex = characterIndex;

        player.initializeMap(width, height);

        debugMazeTiles();

        spawnEnemies(difficulty);

        applyDifficultySettings(difficulty);

        comprehensiveEnemyDebug();
        debugLifePotions();
    }
    public PixelMazePanel getMazePanel() {
        return mazePanel;
    }

    public void setMazePanel(PixelMazePanel mazePanel) {
        this.mazePanel = mazePanel;
    }

    public PixelGameController(int width, int height) {
        this(width, height, 0, 1);
    }

    private void spawnEnemies(int difficulty) {
        System.out.println("=== SPAWNING ENEMIES FOR DIFFICULTY " + difficulty + " ===");

        int enemyCount;
        switch (difficulty) {
            case 0: // EASY
                enemyCount = 15;
                break;
            case 1: // MEDIUM
                enemyCount = 25;
                break;
            case 2: // HARD
                enemyCount = 40;
                break;
            default:
                enemyCount = 15;
        }

        int enemiesSpawned = 0;
        int attempts = 0;
        int maxAttempts = enemyCount * 20;

        while (enemiesSpawned < enemyCount && attempts < maxAttempts) {
            float enemyX, enemyY;

            if (attempts % 3 == 0) {
                int mazePixelWidth = maze.getWidth() * 64;
                int mazePixelHeight = maze.getHeight() * 64;
                enemyX = random.nextInt(mazePixelWidth - 48);
                enemyY = random.nextInt(mazePixelHeight - 64);
            } else {
                int gridX = random.nextInt(maze.getWidth());
                int gridY = random.nextInt(maze.getHeight());
                enemyX = gridX * 64f;
                enemyY = gridY * 64f;
            }

            if (isValidEnemySpawnPosition(enemyX, enemyY)) {
                int enemyType;
                if (difficulty == 0) {
                    enemyType = random.nextInt(2) + 1;
                } else if (difficulty == 1) {
                    enemyType = random.nextInt(3) + 1;
                } else {
                    int rand = random.nextInt(10);
                    if (rand < 3) { // 30% basic
                        enemyType = 1;
                    } else if (rand < 6) {
                        enemyType = 2;
                    } else {
                        enemyType = 3;
                    }
                }

                enemies.add(new PixelEnemy(enemyX, enemyY, enemyType));
                enemiesSpawned++;
                System.out.println("Spawned enemy type " + enemyType + " at: " + enemyX + ", " + enemyY);
            }
            attempts++;
        }

        System.out.println("Successfully spawned " + enemiesSpawned + " enemies for difficulty " + difficulty);
        if (enemiesSpawned < enemyCount) {
            System.out.println("WARNING: Could only spawn " + enemiesSpawned + " out of " + enemyCount + " enemies");
        }
    }

    private boolean isValidEnemySpawnPosition(float x, float y) {
        if (maze.isWallAtPixel(x, y, 48, 64)) {
            return false;
        }

        for (PixelEnemy existingEnemy : enemies) {
            float distanceToEnemy = (float) Math.sqrt(
                    Math.pow(x - existingEnemy.getX(), 2) + Math.pow(y - existingEnemy.getY(), 2)
            );
            if (distanceToEnemy < 50) {
                return false;
            }
        }

        return true;
    }

    private void spawnEnemies() {
        spawnEnemies(1);
    }

    public void debugMazeTiles() {
        System.out.println("=== MAZE TILE ANALYSIS ===");
        char[][] grid = maze.getGrid();
        int pathCount = 0;
        int wallCount = 0;
        int treasureCount = 0;
        int lifePotionCount = 0;
        int startCount = 0;
        int exitCount = 0;

        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                char tile = grid[y][x];
                switch (tile) {
                    case '.': pathCount++; break;
                    case '#': wallCount++; break;
                    case 'T': treasureCount++; break;
                    case 'L': lifePotionCount++; break;
                    case 'S': startCount++; break;
                    case 'E': exitCount++; break;
                }
            }
        }

        System.out.println("Path tiles (.): " + pathCount);
        System.out.println("Wall tiles (#): " + wallCount);
        System.out.println("Treasure tiles (T): " + treasureCount);
        System.out.println("Life Potion tiles (L): " + lifePotionCount);
        System.out.println("Start tiles (S): " + startCount);
        System.out.println("Exit tiles (E): " + exitCount);
        System.out.println("Total tiles: " + (pathCount + wallCount + treasureCount + lifePotionCount + startCount + exitCount));
        System.out.println("=== END TILE ANALYSIS ===");
    }

    private void debugLifePotions() {
        System.out.println("=== LIFE POTION DEBUG ===");
        List<int[]> lifePotions = maze.getLifePotionPositions();
        System.out.println("Life potions in maze: " + lifePotions.size());
        for (int[] pos : lifePotions) {
            System.out.println("Life potion at tile: (" + pos[0] + ", " + pos[1] + ")");
        }
        System.out.println("=== END LIFE POTION DEBUG ===");
    }

    public void startGame() {
        float startX = maze.getStartPixelX();
        float startY = maze.getStartPixelY();

        player.setPosition(startX, startY);
        gameOngoing = true;
        if (soundManager != null) {
            soundManager.startGameMusic();
        }

        System.out.println("Pixel Maze Game Started!");
        System.out.println("Player start position: (" + startX + ", " + startY + ")");
        System.out.println("Enemies spawned: " + enemies.size());
        System.out.println("Use WASD or Arrow Keys to move!");
        System.out.println("Press ESC for pause menu");
        System.out.println("Click to throw daggers at enemies!");

        debugEnemies();
    }

    public void stopGame() {
        gameOngoing = false;
        if (soundManager != null) {
            soundManager.stopGameMusic();
        }
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void comprehensiveEnemyDebug() {
        System.out.println("=== COMPREHENSIVE ENEMY DEBUG ===");

        System.out.println("Game ongoing: " + gameOngoing);
        System.out.println("Player: " + (player != null ? "exists" : "null"));
        System.out.println("Maze: " + (maze != null ? "exists" : "null"));

        System.out.println("Enemies list: " + enemies);
        System.out.println("Enemies list size: " + enemies.size());

        if (enemies != null && !enemies.isEmpty()) {
            for (int i = 0; i < enemies.size(); i++) {
                PixelEnemy enemy = enemies.get(i);
                System.out.println("Enemy " + i + ": " + enemy);
                if (enemy != null) {
                    System.out.println("  Position: " + enemy.getX() + ", " + enemy.getY());
                    System.out.println("  Width/Height: " + enemy.getWidth() + "x" + enemy.getHeight());
                    System.out.println("  Health: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
                    System.out.println("  Alive: " + enemy.isAlive());

                    try {
                        ImageIcon sprite = enemy.getCurrentSprite();
                        System.out.println("  Sprite: " + (sprite != null ? "loaded" : "null"));
                        if (sprite != null) {
                            System.out.println("  Sprite size: " + sprite.getIconWidth() + "x" + sprite.getIconHeight());
                        }
                    } catch (Exception e) {
                        System.out.println("  Sprite error: " + e.getMessage());
                    }
                } else {
                    System.out.println("  Enemy " + i + " is NULL!");
                }
            }
        } else {
            System.out.println("No enemies in list or list is null");
        }

        // 4. Check maze structure for spawn positions
        if (maze != null) {
            char[][] grid = maze.getGrid();
            int pathTiles = 0;
            int treasureTiles = 0;
            int lifePotionTiles = 0; // ADD THIS

            for (int y = 0; y < maze.getHeight(); y++) {
                for (int x = 0; x < maze.getWidth(); x++) {
                    if (grid[y][x] == '.') pathTiles++;
                    if (grid[y][x] == 'T') treasureTiles++;
                    if (grid[y][x] == 'L') lifePotionTiles++;
                }
            }

            System.out.println("Maze path tiles: " + pathTiles);
            System.out.println("Maze treasure tiles: " + treasureTiles);
            System.out.println("Maze life potion tiles: " + lifePotionTiles);
            System.out.println("Total possible spawn tiles: " + (pathTiles + treasureTiles + lifePotionTiles));
        }

        System.out.println("=== END COMPREHENSIVE DEBUG ===");
    }

    public void updatePlayer(boolean[] keys) {
        if (!gameOngoing || !player.isAlive() || paused) return;

        player.update(keys, maze);

        if (player.hasMap()) {
            int playerTileX = (int)(player.getX() / 64);
            int playerTileY = (int)(player.getY() / 64);
            player.markPositionExplored(playerTileX, playerTileY);

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    player.markPositionExplored(playerTileX + dx, playerTileY + dy);
                }
            }
        }

        updateProjectiles();

        for (PixelEnemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            enemy.update(player, maze);

            if (enemy.collidesWith(player) && !player.isInvulnerable()) {
                player.takeDamage();

                float knockbackX = player.getX() - enemy.getX();
                float knockbackY = player.getY() - enemy.getY();
                float length = (float) Math.sqrt(knockbackX * knockbackX + knockbackY * knockbackY);
                if (length > 0) {
                    knockbackX = (knockbackX / length) * 50;
                    knockbackY = (knockbackY / length) * 50;

                    float newX = player.getX() + knockbackX;
                    float newY = player.getY() + knockbackY;

                    if (!maze.isWallAtPixel(newX, newY, player.getWidth(), player.getHeight())) {
                        player.setPosition(newX, newY);
                    }
                }
            }
        }

        enemies.removeIf(enemy -> !enemy.isAlive());

        if (maze.isTreasureAtPixel(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
            maze.collectTreasureAt(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            player.collectTreasure();
        }

        if (maze.isLifePotionAtPixel(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
            maze.collectLifePotionAt(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            player.collectLifePotion();
            System.out.println("Life potion collected! Lives: " + player.getLives());
        }

        if (maze.isExitAtPixel(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
            gameOngoing = false;
            if (soundManager != null) {
                soundManager.stopGameMusic();
            }
            System.out.println("Exit reached! Game over.");
        }

        if (!player.isAlive()) {
            gameOngoing = false;
            if (soundManager != null) {
                soundManager.stopGameMusic();
            }
            System.out.println("Player died! Game over.");
        }
    }
    public void playerThrowDirectionalProjectile() {
        if (player != null && gameOngoing && player.isAlive()) {
            player.throwProjectileInFacingDirection();
        }
    }

    private void updateProjectiles() {
        List<Projectile> projectiles = player.getProjectiles();
        Iterator<Projectile> projectileIterator = projectiles.iterator();

        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();

            for (PixelEnemy enemy : enemies) {
                if (enemy.isAlive() && projectile.collidesWith(enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    projectileIterator.remove();
                    System.out.println("Enemy hit! Health: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
                    break;
                }
            }
        }
    }

    public void playerThrowProjectile(int targetX, int targetY) {
        if (player != null && gameOngoing && player.isAlive()) {
            player.throwProjectile(targetX, targetY);
        }
    }

    public void debugEnemies() {
        System.out.println("=== ENEMY DEBUG INFO ===");
        System.out.println("Total enemies in list: " + enemies.size());
        System.out.println("Game ongoing: " + gameOngoing);
        System.out.println("Player alive: " + (player != null ? player.isAlive() : "null"));

        if (player != null) {
            System.out.println("Player position: " + player.getX() + ", " + player.getY());
        }

        if (enemies.isEmpty()) {
            System.out.println("NO ENEMIES SPAWNED - Possible issues:");
            System.out.println("- Maze might be too small for enemy placement");
            System.out.println("- All spawn positions might be blocked by walls");
            System.out.println("- spawnEnemies() might not have been called");
        } else {
            System.out.println("Enemy details:");
            for (int i = 0; i < enemies.size(); i++) {
                PixelEnemy enemy = enemies.get(i);
                System.out.println("Enemy " + i + ":");
                System.out.println("  Position: " + enemy.getX() + ", " + enemy.getY());
                System.out.println("  Chasing: " + enemy.isChasing());
                System.out.println("  Health: " + enemy.getCurrentHealth() + "/" + enemy.getMaxHealth());
                System.out.println("  Alive: " + enemy.isAlive());

                boolean onWall = maze.isWallAtPixel(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
                System.out.println("  On wall: " + onWall);

                if (player != null) {
                    float distance = (float) Math.sqrt(
                            Math.pow(player.getX() - enemy.getX(), 2) +
                                    Math.pow(player.getY() - enemy.getY(), 2)
                    );
                    System.out.println("  Distance to player: " + distance);
                    System.out.println("  Within chase range: " + (distance <= 300));
                }
            }
        }
        System.out.println("=== END DEBUG ===");
    }

    private void applyDifficultySettings(int difficulty) {
        System.out.println("=== APPLYING DIFFICULTY SETTINGS ===");
        System.out.println("Difficulty level: " + difficulty);
        System.out.println("Enemies to modify: " + enemies.size());

        switch (difficulty) {
            case 0:
                System.out.println("Easy difficulty applied");
                for (PixelEnemy enemy : enemies) {
                    enemy.setSpeed(enemy.getSpeed() * 0.8f);
                }
                break;
            case 1: // MEDIUM
                System.out.println("Medium difficulty applied");
                break;
            case 2: // HARD
                System.out.println("Hard difficulty applied");
                for (PixelEnemy enemy : enemies) {
                    enemy.setSpeed(enemy.getSpeed() * 1.2f); // 20% faster
                }
                break;
        }
        System.out.println("=== DIFFICULTY SETTINGS APPLIED ===");
    }

    public PixelPlayer getPlayer() {
        return player;
    }

    public PixelMaze getMaze() {
        return maze;
    }

    public List<PixelEnemy> getEnemies() {
        return enemies;
    }

    public boolean isGameOngoing() {
        return gameOngoing;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    // SAVE/LOAD METHODS
    public boolean saveGame(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("Game saved successfully to: " + filename);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static PixelGameController loadGame(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            PixelGameController controller = (PixelGameController) ois.readObject();
            System.out.println("Game loaded successfully from: " + filename);
            return controller;
        } catch (Exception e) {
            System.out.println("Error loading game: " + e.getMessage());
            return null;
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.soundManager = new SoundManager();
        this.random = new Random();
        System.out.println("Transient fields reinitialized after loading");
    }
}