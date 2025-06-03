package com.mazegame;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class GameController implements Serializable {
    Player player;
    Maze maze;
    boolean gameOngoing;

    GameController() {
        player = new Player();
        gameOngoing = false;
    }

    GameController(int size) {
        player = new Player();
        gameOngoing = false;
        maze = new Maze(size);
    }

    public Player getPlayer() {
        return player;
    }

    public int getMazeSize() {
        return maze.size;
    }

    public char[][] getMazeGrid() {
        return maze.grid;
    }

    void startGame() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the size of the maze: ");

        int mazeSize = scanner.nextInt();
        maze = new Maze(mazeSize);
        maze.printMaze();
        player.x = 0;
        player.y = 0;
        player.treasuresCollected = 0;
        player.points = 0;

        gameOngoing = true;

        System.out.println("Begin!");
    }

    void startGame1() {
        maze = new Maze(maze.size);

        player.x = 0;
        player.y = 0;
        player.treasuresCollected = 0;
        player.points = 0;

        gameOngoing = true;

        System.out.println("Loja filloi!");
    }

    void playTurn(Direction direction) {
        if (gameOngoing) {
            player.move(direction,maze);
            if (maze.isTreasure(player.x, player.y)) {
                player.collectTreasure();
                System.out.println("You found a treasure!Treasures found: " + player.treasuresCollected);
                maze.grid[player.x][player.y] = '.'; // Remove the treasure from the maze
            } else if (maze.isExit(player.x, player.y)) {
                endGame();
            }
            maze.printMaze();
        } else {
            System.out.println("The game has ended. Start a new game to play again.");
        }
    }

    public void playTurn(int x, int y, MazePanel mazePanel) {
        if (gameOngoing) {
            maze.grid[player.x][player.y]='.';
            player.move(x, y);

            if (maze.isTreasure(player.x, player.y)) {
                player.collectTreasure();
                JOptionPane.showMessageDialog(null, "You found a treasure! Current treasures: " + player.treasuresCollected);
                maze.grid[player.x][player.y] = '.'; // Heq thesarin nga labirinti
            } else if (maze.isExit(player.x, player.y)) {
                endGame();
            }
            maze.grid[player.x][player.y]='S';
            mazePanel.updatePlayerIcon(x, y, player.x, player.y);
            mazePanel.repaint(); // rifresko panelin e labirintit
        } else {
            JOptionPane.showMessageDialog(null, "Loja ka perfunduar. Fillo nje loje te re per te luajtur serish.");
        }
    }

    void endGame() {
        gameOngoing = false;
        System.out.println("Urime! Arritet daljen me " + player.treasuresCollected +
                " thesare dhe keni fituar " + player.points + " pike.");
    }

    public void saveGame(String fileName) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
            outputStream.writeObject(this);
            System.out.println("Loja u ruajt me sukses.");
        } catch (IOException e) {
            System.err.println("Error ne ruajtjen e lojes: " + e.getMessage());
        }
    }

    public void saveGame1(String fileName) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
            outputStream.writeObject(this);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error,couldn't save the game!");
        }
    }

    public void loadGame(String fileName) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            GameController loadedGame = (GameController) inputStream.readObject();
            this.player = loadedGame.player;
            this.maze = loadedGame.maze;
            this.gameOngoing = loadedGame.gameOngoing;
            System.out.println("Game loaded successfully .");
            this.maze.printMaze();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void loadGame1(String fileName) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            GameController loadedGame = (GameController) inputStream.readObject();
            this.player = loadedGame.player;
            this.maze = loadedGame.maze;
            this.gameOngoing = loadedGame.gameOngoing;
            JOptionPane.showMessageDialog(null, "Game loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Game loaded failure: " + e.getMessage());
        }
    }

    public boolean isValidMove(int x, int y) {
        if(x>player.x+1 || x<player.x-1 || y>player.y+1 || y<player.y-1)
            return false;
        return x >= 0 && x < maze.grid.length && y >= 0 && y < maze.grid[x].length && maze.grid[x][y] != '#';
    }

    int[] getPlayerPosition() {
        return maze.getPlayerPosition();
    }
} 