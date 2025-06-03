package com.mazegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class MazeGUI extends JFrame {
    private GameController gameController;
    private MazePanel mazePanel;

    public MazeGUI() {
        super("Maze Game");
        initializeMainFrame();
    }


    private void initializeMainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton newGameButton = new JButton("Start New Game");
        JButton loadGameButton = new JButton("Load Existing Game");
        JButton saveGameButton = new JButton("Save Game");

        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        loadGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLoadGameFrame();
            }
        });

        saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGame();
            }
        });

        buttonPanel.add(newGameButton);
        buttonPanel.add(loadGameButton);
        buttonPanel.add(saveGameButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addKeyListener(new MazeKeyListener());
        setFocusable(true);
        requestFocusInWindow();

        mainPanel.add(BorderLayout.CENTER, new JPanel());
        getContentPane().add(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        promptForGameChoice();
    }

    private void promptForGameChoice() {
        int choice = JOptionPane.showOptionDialog(this,
                "Start a new game or continue an existing game?",
                "Choice",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"New Game", "Load Game"},
                "New Game");

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            openLoadGameFrame();
        }
    }

    private void startNewGame() {
        int mazeSize = promptForMazeSize();
        gameController = new GameController(mazeSize);
        requestFocusInWindow();
        initializeMazePanel();
    }

    private int promptForMazeSize() {
        String input = JOptionPane.showInputDialog("Maze size:");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            showErrorDialog("Incorrect input ,default value is 5!");
            return 5;
        }
    }

    private void openLoadGameFrame() {
        GameController loadedGame = new GameController();
        loadedGame.loadGame1("savedGame.ser");

        if (loadedGame != null) {
            gameController = loadedGame;
            initializeMazePanel1();
        } else {
            showErrorDialog("Couldn't load game!Try again.");
        }
    }

    private void saveGame() {
        if (gameController != null) {
            gameController.saveGame1("savedGame.ser");
            showInfoDialog("Game saved successfully", "Save Game");
        } else {
            showErrorDialog("There are no saved games.");
        }
    }

    private void initializeMazePanel() {
        mazePanel = new MazePanel(gameController);
        gameController.startGame1();

        JPanel mainPanel = (JPanel) getContentPane().getComponent(0);
        mainPanel.remove(1);
        mainPanel.add(BorderLayout.CENTER, mazePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeMazePanel1() {
        if (gameController != null) {
            mazePanel = new MazePanel(gameController);

            JPanel mainPanel = (JPanel) getContentPane().getComponent(0);
            mainPanel.remove(1); // Remove the placeholder panel
            mainPanel.add(BorderLayout.CENTER, mazePanel);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        } else {
            showErrorDialog("Failed to load the game. Please try again.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeGUI mazeGUI = new MazeGUI();
            mazeGUI.pack();
        });
    }

    private class MazeKeyListener extends KeyAdapter {
        private boolean keyPressed = false;

        @Override
        public void keyPressed(KeyEvent e) {
            keyPressed = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (keyPressed) {
                if (gameController != null && gameController.gameOngoing) {
                    int[] playerPosition = gameController.getPlayerPosition();
                    int newX = playerPosition[0];
                    int newY = playerPosition[1];

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                            newX--;
                            break;
                        case KeyEvent.VK_DOWN:
                            newX++;
                            break;
                        case KeyEvent.VK_LEFT:
                            newY--;
                            break;
                        case KeyEvent.VK_RIGHT:
                            newY++;
                            break;
                    }

                    if (gameController.isValidMove(newX, newY)) {
                        gameController.playTurn(newX, newY, mazePanel);
                        mazePanel.repaint();
                        checkGameStatus();
                    } else {
                        showErrorDialog("Incorrect move!Try again!");
                    }
                }

                keyPressed = false;
            }
        }
    }

    private void checkGameStatus() {
        if (!gameController.gameOngoing) {
            showInfoDialog("CONGRATULATIONS .You found the exit with " +
                    gameController.getPlayer().getTreasuresCollected() + " trasures.\nTotal points: " +
                    gameController.getPlayer().getPoints(), "Game finished!");

            promptForGameChoice();
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfoDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
} 