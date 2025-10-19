package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class PixelEnemy implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient ImageIcon[][] spriteFrames = new ImageIcon[4][3];

    private float x, y;
    private int width = 48, height = 64;
    private float speed = 2.5f;
    private Direction facing = Direction.DOWN;

    private int currentFrame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 8;

    private float chaseRange = 300f;
    private boolean isChasing = false;
    private boolean moving = false;

    private int maxHealth = 3;
    private int currentHealth;
    private boolean alive = true;
    private long lastDamageTime = 0;
    private static final long DAMAGE_FLASH_DURATION = 200; // milliseconds

    // Enemy type
    private int enemyType;
    private int damage = 1;

    public PixelEnemy(float startX, float startY) {
        this(startX, startY, 1);
    }

    public PixelEnemy(float startX, float startY, int enemyType) {
        this.x = startX;
        this.y = startY;
        this.enemyType = enemyType;
        this.currentHealth = maxHealth;

        applyEnemyTypeProperties();

        loadSpriteFrames();
        System.out.println("Enemy type " + enemyType + " created at: " + x + ", " + y);
    }

    private void applyEnemyTypeProperties() {
        switch (enemyType) {
            case 1:
                this.speed = 2.0f;
                this.maxHealth = 3;
                this.damage = 1;
                this.chaseRange = 250f;
                break;
            case 2:
                this.speed = 3.0f;
                this.maxHealth = 2;
                this.damage = 1;
                this.chaseRange = 350f;
                break;
            case 3:
                this.speed = 1.5f;
                this.maxHealth = 5;
                this.damage = 2;
                this.chaseRange = 200f;
                break;
        }
        this.currentHealth = maxHealth;
    }

    private void loadSpriteFrames() {
        try {
            System.out.println("=== LOADING ENEMY TYPE " + enemyType + " SPRITES ===");

            String enemyFolder = "/enemy/enemy" + enemyType + "/";

            spriteFrames[0][0] = loadAndScaleSprite(enemyFolder + "up_1.png", width, height);
            spriteFrames[0][1] = loadAndScaleSprite(enemyFolder + "up_2.png", width, height);
            spriteFrames[0][2] = loadAndScaleSprite(enemyFolder + "up_3.png", width, height);

            spriteFrames[1][0] = loadAndScaleSprite(enemyFolder + "down_1.png", width, height);
            spriteFrames[1][1] = loadAndScaleSprite(enemyFolder + "down_2.png", width, height);
            spriteFrames[1][2] = loadAndScaleSprite(enemyFolder + "down_3.png", width, height);

            spriteFrames[2][0] = loadAndScaleSprite(enemyFolder + "left_1.png", width, height);
            spriteFrames[2][1] = loadAndScaleSprite(enemyFolder + "left_2.png", width, height);
            spriteFrames[2][2] = loadAndScaleSprite(enemyFolder + "left_3.png", width, height);

            spriteFrames[3][0] = loadAndScaleSprite(enemyFolder + "right_1.png", width, height);
            spriteFrames[3][1] = loadAndScaleSprite(enemyFolder + "right_2.png", width, height);
            spriteFrames[3][2] = loadAndScaleSprite(enemyFolder + "right_3.png", width, height);

            boolean allLoaded = true;
            for (int dir = 0; dir < 4; dir++) {
                for (int frame = 0; frame < 3; frame++) {
                    if (spriteFrames[dir][frame] == null) {
                        allLoaded = false;
                        System.out.println("Missing sprite: " + enemyFolder + " direction " + dir + ", frame " + frame);
                    }
                }
            }

            if (!allLoaded) {
                System.out.println("Some sprites failed to load for enemy type " + enemyType + ", creating fallback...");
                createFallbackSprites();
            } else {
                System.out.println("All enemy type " + enemyType + " sprites loaded successfully!");
            }

        } catch (Exception e) {
            System.out.println("Error loading enemy type " + enemyType + " sprites: " + e.getMessage());
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
        System.out.println("Creating animated fallback enemy sprites for type " + enemyType + "...");

        // Different colors for different enemy types
        Color[][] frameColors = {
                { // Type 1 - Red
                        new Color(200, 0, 0),
                        new Color(150, 0, 0),
                        new Color(255, 0, 0)
                },
                { // Type 2 - Blue
                        new Color(0, 0, 200),
                        new Color(0, 0, 150),
                        new Color(0, 0, 255)
                },
                { // Type 3 - Green
                        new Color(0, 150, 0),
                        new Color(0, 100, 0),
                        new Color(0, 255, 0)
                }
        };

        String[] directionLabels = {"UP", "DOWN", "LEFT", "RIGHT"};

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 3; frame++) {
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();

                Color[] colors = frameColors[enemyType - 1];
                g2d.setColor(colors[frame]);
                g2d.fillRect(0, 0, width, height);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("ENEMY" + enemyType, 5, 15);
                g2d.drawString(directionLabels[dir], 5, 30);
                g2d.drawString("F" + (frame + 1), 5, 45);

                switch (enemyType) {
                    case 1:
                        g2d.drawString("BASIC", 5, 55);
                        break;
                    case 2:
                        g2d.drawString("FAST", 5, 55);
                        break;
                    case 3:
                        g2d.drawString("STRONG", 5, 55);
                        break;
                }

                g2d.dispose();
                spriteFrames[dir][frame] = new ImageIcon(img);
            }
        }
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    public void update(PixelPlayer player, PixelMaze maze) {
        if (!alive) return;

        float playerCenterX = player.getX() + player.getWidth() / 2;
        float playerCenterY = player.getY() + player.getHeight() / 2;
        float enemyCenterX = x + width / 2;
        float enemyCenterY = y + height / 2;

        float distance = (float) Math.sqrt(
                Math.pow(playerCenterX - enemyCenterX, 2) +
                        Math.pow(playerCenterY - enemyCenterY, 2)
        );

        isChasing = (distance <= chaseRange);
        moving = isChasing;

        if (isChasing) {
            chasePlayer(player, maze);
        } else {
            wander(maze);
        }

        updateAnimation();
    }

    private void chasePlayer(PixelPlayer player, PixelMaze maze) {
        float playerCenterX = player.getX() + player.getWidth() / 2;
        float playerCenterY = player.getY() + player.getHeight() / 2;
        float enemyCenterX = x + width / 2;
        float enemyCenterY = y + height / 2;

        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;

        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        float newX = x + dx * speed;
        float newY = y + dy * speed;

        updateFacingDirection(dx, dy);

        if (!maze.isWallAtPixel(newX, newY, width, height)) {
            x = newX;
            y = newY;
        }
    }

    private void wander(PixelMaze maze) {
        if (Math.random() < 0.02) {
            changeWanderDirection();
        }

        float dx = 0, dy = 0;

        switch (facing) {
            case UP: dy = -1; break;
            case DOWN: dy = 1; break;
            case LEFT: dx = -1; break;
            case RIGHT: dx = 1; break;
        }

        float newX = x + dx * (speed * 0.3f);
        float newY = y + dy * (speed * 0.3f);

        if (maze.isWallAtPixel(newX, newY, width, height)) {
            changeWanderDirection();
            moving = false;
        } else {
            x = newX;
            y = newY;
            moving = true;
        }
    }

    private void changeWanderDirection() {
        Direction[] directions = Direction.values();
        facing = directions[(int)(Math.random() * directions.length)];
    }

    private void updateFacingDirection(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            facing = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else {
            facing = (dy > 0) ? Direction.DOWN : Direction.UP;
        }
    }

    private void updateAnimation() {
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

    public ImageIcon getCurrentSprite() {
        int directionIndex = facing.ordinal();
        return spriteFrames[directionIndex][currentFrame];
    }

    public boolean collidesWith(PixelPlayer player) {
        return x < player.getX() + player.getWidth() &&
                x + width > player.getX() &&
                y < player.getY() + player.getHeight() &&
                y + height > player.getY();
    }

    public boolean collidesWith(Projectile projectile) {
        return x < projectile.getX() + projectile.getWidth() &&
                x + width > projectile.getX() &&
                y < projectile.getY() + projectile.getHeight() &&
                y + height > projectile.getY();
    }

    public void takeDamage(int damage) {
        if (!alive) return;

        currentHealth -= damage;
        lastDamageTime = System.currentTimeMillis();

        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
            System.out.println("Enemy type " + enemyType + " defeated!");
        } else {
            System.out.println("Enemy type " + enemyType + " took " + damage + " damage! Health: " + currentHealth + "/" + maxHealth);
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isFlashing() {
        return System.currentTimeMillis() - lastDamageTime < DAMAGE_FLASH_DURATION;
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isChasing() { return isChasing; }
    public float getChaseRange() { return chaseRange; }
    public int getEnemyType() { return enemyType; }
    public float getSpeed() {
        return speed;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        spriteFrames = new ImageIcon[4][3];
        loadSpriteFrames();
        System.out.println("Enemy type " + enemyType + " sprites reloaded after deserialization");
    }
}