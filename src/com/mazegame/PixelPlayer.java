package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.awt.image.BufferedImage;

public class PixelPlayer implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean hasMap = false;
    private boolean[][] exploredTiles;

    private int mazeWidth, mazeHeight;

    private float x, y;
    private int shards = 250;
    private int width = 48, height = 64;
    private float speed = 4.0f;
    private float originalSpeed = 4.0f;
    private Direction facing = Direction.DOWN;

    private int treasuresCollected = 0;
    private int points = 0;
    private int lives = 3;
    private boolean alive = true;
    private boolean invulnerable = false;
    private long invulnerabilityEndTime = 0;
    private static final long INVULNERABILITY_DURATION = 2000;

    // Make spriteFrames transient since ImageIcon isn't serializable
    private transient ImageIcon[][] spriteFrames = new ImageIcon[4][3];
    private int currentFrame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 6; // Higher = slower animation

    // Projectile system
    private java.util.List<Projectile> projectiles = new ArrayList<>();
    private long lastThrowTime = 0;
    private static final long THROW_COOLDOWN = 500; // milliseconds

    // Character type
    private int characterType;

    private String projectileType = "blade";
    private int projectileDamage = 1;

    public PixelPlayer(int characterType) {
        this.characterType = characterType;
        this.originalSpeed = 4.0f;
        this.speed = this.originalSpeed;
        loadSpriteFrames();
        System.out.println("Player created with character type: " + characterType);
    }

    private void loadSpriteFrames() {
        try {
            System.out.println("=== LOADING PLAYER SPRITES ===");

            String characterFolder = "/player" + (characterType + 1) + "/";

            spriteFrames[0][0] = loadAndScaleSprite(characterFolder + "up_1.png", width, height);
            spriteFrames[0][1] = loadAndScaleSprite(characterFolder + "up_2.png", width, height);
            spriteFrames[0][2] = loadAndScaleSprite(characterFolder + "up_3.png", width, height);

            spriteFrames[1][0] = loadAndScaleSprite(characterFolder + "down_1.png", width, height);
            spriteFrames[1][1] = loadAndScaleSprite(characterFolder + "down_2.png", width, height);
            spriteFrames[1][2] = loadAndScaleSprite(characterFolder + "down_3.png", width, height);

            spriteFrames[2][0] = loadAndScaleSprite(characterFolder + "left_1.png", width, height);
            spriteFrames[2][1] = loadAndScaleSprite(characterFolder + "left_2.png", width, height);
            spriteFrames[2][2] = loadAndScaleSprite(characterFolder + "left_3.png", width, height);

            spriteFrames[3][0] = loadAndScaleSprite(characterFolder + "right_1.png", width, height);
            spriteFrames[3][1] = loadAndScaleSprite(characterFolder + "right_2.png", width, height);
            spriteFrames[3][2] = loadAndScaleSprite(characterFolder + "right_3.png", width, height);

            boolean allLoaded = true;
            for (int dir = 0; dir < 4; dir++) {
                for (int frame = 0; frame < 3; frame++) {
                    if (spriteFrames[dir][frame] == null) {
                        allLoaded = false;
                        System.out.println("Missing player sprite: direction " + dir + ", frame " + frame);
                    }
                }
            }

            if (!allLoaded) {
                System.out.println("Some player sprites failed to load, creating fallback...");
                createFallbackSprites();
            } else {
                System.out.println("All player sprites loaded successfully!");
            }

        } catch (Exception e) {
            System.out.println("Error loading player sprites: " + e.getMessage());
            createFallbackSprites();
        }
    }

    private ImageIcon loadAndScaleSprite(String path, int targetWidth, int targetHeight) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                ImageIcon original = new ImageIcon(bytes);
                Image scaled = original.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } else {
                System.out.println("FAILED: " + path + " not found");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + path + " - " + e.getMessage());
        }
        return null;
    }

    private void createFallbackSprites() {
        System.out.println("Creating animated fallback player sprites...");

        Color[] frameColors = {
                new Color(0, 100, 200),    // Dark blue
                new Color(0, 150, 255),    // Medium blue
                new Color(100, 200, 255)   // Light blue
        };

        String[] directionLabels = {"UP", "DOWN", "LEFT", "RIGHT"};

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 3; frame++) {
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setColor(frameColors[frame]);
                g2d.fillRect(0, 0, width, height);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("PLAYER", 5, 15);
                g2d.drawString(directionLabels[dir], 5, 30);
                g2d.drawString("F" + (frame + 1), 5, 45);
                g2d.dispose();
                spriteFrames[dir][frame] = new ImageIcon(img);
            }
        }
    }

    public void update(boolean[] keys, PixelMaze maze) {
        if (!alive) return;

        updateInvulnerability();

        handleMovement(keys, maze);

        updateProjectiles(maze);

        updateAnimation();
    }

    private void handleMovement(boolean[] keys, PixelMaze maze) {
        float newX = x;
        float newY = y;
        boolean moving = false;

        if (keys[0]) {
            newY -= speed;
            facing = Direction.UP;
            moving = true;
        }
        if (keys[1]) {
            newY += speed;
            facing = Direction.DOWN;
            moving = true;
        }
        if (keys[2]) {
            newX -= speed;
            facing = Direction.LEFT;
            moving = true;
        }
        if (keys[3]) {
            newX += speed;
            facing = Direction.RIGHT;
            moving = true;
        }

        if (!maze.isWallAtPixel(newX, newY, width, height)) {
            x = newX;
            y = newY;
        }

        if (moving) {
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                currentFrame = (currentFrame + 1) % 3;
                animationCounter = 0;
            }
        } else {
            currentFrame = 0;
        }
    }

    private void updateAnimation() {
    }

    private void updateInvulnerability() {
        if (invulnerable && System.currentTimeMillis() >= invulnerabilityEndTime) {
            invulnerable = false;
        }
    }

    public void updateProjectiles(PixelMaze maze) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update();

            boolean hitWall;
            if ("spell".equals(projectile.getType())) {
                // For spells, use centered 32x32 collision area instead of full 64x64
                float centerX = projectile.getX() + (projectile.getWidth() - 32) / 2;
                float centerY = projectile.getY() + (projectile.getHeight() - 32) / 2;
                hitWall = maze.isWallAtPixel(centerX, centerY, 32, 32);
            } else {
                hitWall = maze.isWallAtPixel(projectile.getX(), projectile.getY(),
                        projectile.getWidth(), projectile.getHeight());
            }

            if (projectile.getX() < 0 || projectile.getX() > maze.getWidth() * 64 ||
                    projectile.getY() < 0 || projectile.getY() > maze.getHeight() * 64 ||
                    hitWall) {
                iterator.remove();
            }
        }
    }

    public void setProjectileType(String type) {
        this.projectileType = type;
        System.out.println("Projectile type changed to: " + type);

        // Increase damage when switching to spell
        if ("spell".equals(type)) {
            this.projectileDamage = 2; // Spell does more damage
        } else {
            this.projectileDamage = 1; // Blade does normal damage
        }
    }

    public String getProjectileType() {
        return projectileType;
    }

    public void throwProjectileInFacingDirection() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastThrowTime >= THROW_COOLDOWN) {
            float startX = x + width / 2 - 16;
            float startY = y + height / 2 - 16;

            // Create projectile with current type
            Projectile projectile = new Projectile(startX, startY, facing, projectileType);
            projectiles.add(projectile);
            lastThrowTime = currentTime;

            System.out.println("Throwing " + facing + " " + projectileType + "!");
        }
    }

    public void throwProjectile(float targetX, float targetY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastThrowTime >= THROW_COOLDOWN) {
            float startX = x + width / 2 - 16;
            float startY = y + height / 2 - 16;

            Projectile projectile = new Projectile(startX, startY, targetX, targetY, projectileType);
            projectiles.add(projectile);
            lastThrowTime = currentTime;
        }
    }

    public java.util.List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void takeDamage() {
        if (invulnerable || !alive) return;

        lives--;
        invulnerable = true;
        invulnerabilityEndTime = System.currentTimeMillis() + INVULNERABILITY_DURATION;

        if (lives <= 0) {
            lives = 0;
            alive = false;
            System.out.println("Player died!");
        } else {
            System.out.println("Player took damage! Lives: " + lives);
        }
    }

    public void collectTreasure() {
        treasuresCollected++;
        points += 100;
        addShards(10);
        System.out.println("Treasure collected! Total: " + treasuresCollected + " Points: " + points);
    }

    public void collectLifePotion() {
        lives++;
        points += 50;
        System.out.println("Life potion collected! Lives: " + lives);
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTreasuresCollected() { return treasuresCollected; }
    public int getPoints() { return points; }
    public int getLives() { return lives; }
    public boolean isAlive() { return alive; }
    public boolean isInvulnerable() { return invulnerable; }
    public int getShards() {
        return shards;
    }
    public void addShards(int amount) {
        this.shards += amount;
        System.out.println("+10 shards! Total: " + shards);
    }
    public void collectShard() {
        addShards(1);
    }
    public void deductShards(int amount) {
        this.shards -= amount;
        if (shards < 0) shards = 0;
    }

    public void increaseMaxHealth(int amount) {
        this.lives += amount;
    }

    public void increaseProjectileDamage(int amount) {

        System.out.println("Projectile damage increased by " + amount);
    }

    public void addShardsFromEnemy(int enemyType) {
        switch (enemyType) {
            case 1: addShards(5); break;
            case 2: addShards(8); break;
            case 3: addShards(12); break;
            default: addShards(5); break;
        }
        System.out.println("Collected shards! Total: " + shards);
    }

    public ImageIcon getCurrentSprite() {
        int directionIndex = facing.ordinal();
        return spriteFrames[directionIndex][currentFrame];
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getProjectileDamage(){
        return projectileDamage;
    }

    // ADD THESE SPEED METHODS
    public void doubleSpeed() {
        this.speed = this.originalSpeed * 2; // Double the speed
        System.out.println("Speed doubled! Now: " + speed);
    }

    public void resetSpeed() {
        this.speed = this.originalSpeed;
    }

    public float getSpeed() {
        return speed;
    }
    public void initializeMap(int mazeWidth, int mazeHeight) {
        this.mazeWidth = mazeWidth;
        this.mazeHeight = mazeHeight;
        this.exploredTiles = new boolean[mazeHeight][mazeWidth];

        // Mark starting position as explored
        int startX = (int)(getX() / 64);
        int startY = (int)(getY() / 64);
        if (startX >= 0 && startX < mazeWidth && startY >= 0 && startY < mazeHeight) {
            exploredTiles[startY][startX] = true;

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int adjX = startX + dx;
                    int adjY = startY + dy;
                    if (adjX >= 0 && adjX < mazeWidth && adjY >= 0 && adjY < mazeHeight) {
                        exploredTiles[adjY][adjX] = true;
                    }
                }
            }
        }
        System.out.println("Player map initialized for " + mazeWidth + "x" + mazeHeight + " maze");
    }

    public void setHasMap(boolean hasMap) {
        this.hasMap = hasMap;
        if (hasMap && exploredTiles == null) {
            // Initialize explored tiles array when map is acquired later
            exploredTiles = new boolean[mazeHeight][mazeWidth];
            // Mark current position as explored
            int currentX = (int)(getX() / 64);
            int currentY = (int)(getY() / 64);
            if (currentX >= 0 && currentX < mazeWidth && currentY >= 0 && currentY < mazeHeight) {
                exploredTiles[currentY][currentX] = true;
            }
        }
        System.out.println("Labyrinth Map: " + (hasMap ? "ACQUIRED" : "NOT OWNED"));
    }

    public boolean hasMap() {
        return hasMap;
    }

    public void markPositionExplored(int x, int y) {
        if (exploredTiles != null && x >= 0 && x < mazeWidth && y >= 0 && y < mazeHeight) {
            exploredTiles[y][x] = true;
        }
    }

    public boolean[][] getExploredTiles() {
        return exploredTiles;
    }

    public int getExploredTileCount() {
        if (exploredTiles == null) return 0;

        int count = 0;
        for (int y = 0; y < mazeHeight; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                if (exploredTiles[y][x]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        spriteFrames = new ImageIcon[4][3];
        loadSpriteFrames();
        System.out.println("Player sprites reloaded after deserialization");
    }
}