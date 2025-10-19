package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Projectile {
    private static final long serialVersionUID = 1L;

    private float x, y;
    private float velocityX, velocityY;
    private int width, height;
    private boolean active = true;
    private ImageIcon sprite;
    private int damage = 1;
    private static final float PROJECTILE_SPEED = 8.0f;
    private Direction direction;
    private String type;

    public Projectile(float startX, float startY, Direction direction, String type) {
        this.x = startX;
        this.y = startY;
        this.direction = direction;
        this.type = type;

        if ("spell".equals(type)) {
            this.width = 64;
            this.height = 64;
        } else {
            this.width = 32;
            this.height = 32;
        }

        switch (direction) {
            case UP:
                velocityX = 0;
                velocityY = -PROJECTILE_SPEED;
                break;
            case DOWN:
                velocityX = 0;
                velocityY = PROJECTILE_SPEED;
                break;
            case LEFT:
                velocityX = -PROJECTILE_SPEED;
                velocityY = 0;
                break;
            case RIGHT:
                velocityX = PROJECTILE_SPEED;
                velocityY = 0;
                break;
        }

        // Set damage based on type
        this.damage = "spell".equals(type) ? 2 : 1;

        loadSprite();
    }

    public Projectile(float startX, float startY, float targetX, float targetY, String type) {
        this.x = startX;
        this.y = startY;
        this.type = type;

        // ADD SIZE LOGIC HERE TOO
        if ("spell".equals(type)) {
            this.width = 64;  // Big spells
            this.height = 64;
        } else {
            this.width = 32;  // Normal blades
            this.height = 32;
        }

        // Calculate direction based on target
        float dx = targetX - startX;
        float dy = targetY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            this.velocityX = (dx / length) * PROJECTILE_SPEED;
            this.velocityY = (dy / length) * PROJECTILE_SPEED;

            if (Math.abs(dx) > Math.abs(dy)) {
                direction = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            } else {
                direction = (dy > 0) ? Direction.DOWN : Direction.UP;
            }
        } else {
            velocityX = PROJECTILE_SPEED;
            velocityY = 0;
            direction = Direction.RIGHT;
        }

        this.damage = "spell".equals(type) ? 2 : 1;

        loadSprite();
    }
    public Projectile(float startX, float startY, Direction direction) {
        this(startX, startY, direction, "blade"); // Default to blade
    }

    public Projectile(float startX, float startY, float targetX, float targetY) {
        this(startX, startY, targetX, targetY, "blade"); // Default to blade
    }

    private void loadSprite() {
        try {
            String spritePath = getSpritePath();
            InputStream is = getClass().getResourceAsStream(spritePath);
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                ImageIcon original = new ImageIcon(bytes);
                Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                sprite = new ImageIcon(scaled);
                System.out.println("Projectile sprite loaded: " + spritePath);
            } else {
                System.out.println("Projectile sprite not found: " + spritePath);
                createFallbackSprite();
            }
        } catch (Exception e) {
            System.out.println("Error loading projectile sprite: " + e.getMessage());
            createFallbackSprite();
        }
    }

    private String getSpritePath() {
        String basePath = "/tiles/";

        if ("spell".equals(type)) {
            // Spell projectiles
            switch (direction) {
                case UP: return basePath + "spell_up.png";
                case DOWN: return basePath + "spell_down.png";
                case LEFT: return basePath + "spell_left.png";
                case RIGHT: return basePath + "spell_right.png";
                default: return basePath + "spell.png";
            }
        } else {
            // Blade projectiles (default)
            switch (direction) {
                case UP: return basePath + "blade_up.png";
                case DOWN: return basePath + "blade_down.png";
                case LEFT: return basePath + "blade_left.png";
                case RIGHT: return basePath + "blade_right.png";
                default: return basePath + "blade.png";
            }
        }
    }

    private void createFallbackSprite() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        Color projectileColor;
        if ("spell".equals(type)) {
            switch (direction) {
                case UP: projectileColor = new Color(255, 100, 255); break;    // Pinkish
                case DOWN: projectileColor = new Color(100, 255, 255); break;  // Cyan
                case LEFT: projectileColor = new Color(255, 255, 100); break;  // Yellow
                case RIGHT: projectileColor = new Color(100, 255, 100); break; // Green
                default: projectileColor = new Color(200, 100, 255);           // Purple
            }

            // Draw circular spell effect
            g2d.setColor(projectileColor);
            g2d.fillOval(8, 8, 16, 16);

            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(10, 10, 12, 12);

        } else {
            switch (direction) {
                case UP: projectileColor = Color.CYAN; break;
                case DOWN: projectileColor = Color.BLUE; break;
                case LEFT: projectileColor = Color.MAGENTA; break;
                case RIGHT: projectileColor = Color.ORANGE; break;
                default: projectileColor = Color.BLUE;
            }

            switch (direction) {
                case UP:
                    g2d.setColor(projectileColor);
                    g2d.fillRect(12, 8, 8, 16);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(8, 20, 16, 4);
                    break;
                case DOWN:
                    g2d.setColor(projectileColor);
                    g2d.fillRect(12, 8, 8, 16);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(8, 8, 16, 4);
                    break;
                case LEFT:
                    g2d.setColor(projectileColor);
                    g2d.fillRect(8, 12, 16, 8);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(20, 8, 4, 16);
                    break;
                case RIGHT:
                    g2d.setColor(projectileColor);
                    g2d.fillRect(8, 12, 16, 8);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillRect(8, 8, 4, 16);
                    break;
            }
        }

        g2d.dispose();
        sprite = new ImageIcon(img);
    }

    public void update() {
        if (!active) return;
        x += velocityX;
        y += velocityY;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public boolean collidesWith(PixelEnemy enemy) {
        if (!active || !enemy.isAlive()) return false;

        return x < enemy.getX() + enemy.getWidth() &&
                x + width > enemy.getX() &&
                y < enemy.getY() + enemy.getHeight() &&
                y + height > enemy.getY();
    }

    public int getDamage() {
        return damage;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getType() {
        return type;
    }

    // Getters for rendering
    public float getX() { return x; }
    public float getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public ImageIcon getSprite() { return sprite; }

}