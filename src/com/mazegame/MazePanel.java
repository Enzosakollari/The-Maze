package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

public class MazePanel extends JPanel {
    private static final int ICON_SIZE = 80;
    private GameController gameController;
    private ImageIcon pathIcon;
    private ImageIcon wallIcon;
    private ImageIcon treasureIcon;
    private ImageIcon exitIcon;
    private ImageIcon playerIcon;
    private boolean useImages = true;

    MazePanel(GameController gameController) {
        this.gameController = gameController;
        setLayout(new GridLayout(gameController.getMazeSize(), gameController.getMazeSize()));

        initializeIcons();
        createMazeLabels();
    }

    private void initializeIcons() {
        try {
            pathIcon = loadImageFromClasspath("/path.png");
            wallIcon = loadImageFromClasspath("/wall1.jpg");
            treasureIcon = loadImageFromClasspath("/treasure.png");
            exitIcon = loadImageFromClasspath("/exit.png");
            playerIcon = loadImageFromClasspath("/player.png");

            if (pathIcon == null || wallIcon == null || treasureIcon == null ||
                exitIcon == null || playerIcon == null) {
                useImages = false;
                System.out.println("Images not found in resources folder. Using colors instead.");
            }
        } catch (Exception e) {
            useImages = false;
            System.out.println("Error loading images: " + e.getMessage() + ". Using colors instead.");
        }
    }

    private ImageIcon loadImageFromClasspath(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                ImageIcon originalIcon = new ImageIcon(bytes);
                Image scaledImage = originalIcon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + path);
        }
        return null;
    }


    private void createMazeLabels() {
        for (int i = 0; i < gameController.getMazeSize(); i++) {
            for (int j = 0; j < gameController.getMazeSize(); j++) {
                add(createMazeLabel(i, j));
            }
        }
    }

    private JLabel createMazeLabel(int x, int y) {
        JLabel label = new JLabel();
        label.setOpaque(false);
        updateLabelIcon(label, x, y);
        return label;
    }

    private void updateLabelIcon(JLabel label, int x, int y) {
        char cell = gameController.getMazeGrid()[x][y];
        
        if (useImages) {
            switch (cell) {
                case '.':
                    label.setIcon(pathIcon);
                    break;
                case '#':
                    label.setIcon(wallIcon);
                    break;
                case 'T':
                    label.setIcon(treasureIcon);
                    break;
                case 'E':
                    label.setIcon(exitIcon);
                    break;
                case 'S':
                    label.setIcon(playerIcon);
                    break;
            }
        } else {
            switch (cell) {
                case '.':
                    label.setBackground(Color.WHITE);
                    break;
                case '#':
                    label.setBackground(Color.BLACK);
                    break;
                case 'T':
                    label.setBackground(Color.YELLOW);
                    break;
                case 'E':
                    label.setBackground(Color.BLUE);
                    break;
                case 'S':
                    label.setBackground(Color.RED);
                    break;
            }
            label.setIcon(null);
        }
    }

    public void updateIcons() {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                int[] position = getPositionFromLabel(label);
                updateLabelIcon(label, position[0], position[1]);
            }
        }
    }

    private int[] getPositionFromLabel(JLabel label) {
        int index = -1;
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == label) {
                index = i;
                break;
            }
        }
        int size = gameController.getMazeSize();
        return new int[]{index / size, index % size};
    }

    void updatePlayerIcon(int oldX, int oldY, int newX, int newY) {
        Component[] components = getComponents();
        int size = gameController.getMazeSize();
        int oldIndex = oldX * size + oldY;
        int newIndex = newX * size + newY;

        if (oldIndex >= 0 && oldIndex < components.length) {
            JLabel oldLabel = (JLabel) components[oldIndex];
            updateLabelIcon(oldLabel, oldX, oldY);
        }

        if (newIndex >= 0 && newIndex < components.length) {
            JLabel newLabel = (JLabel) components[newIndex];
            updateLabelIcon(newLabel, newX, newY);
        }

        revalidate();
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateIcons();
    }
} 