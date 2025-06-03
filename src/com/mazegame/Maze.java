package com.mazegame;

import java.io.Serializable;
import java.util.*;

public class Maze implements Serializable {
    char[][] grid;
    int exitX, exitY;
    int size;

    Maze(int size) {
        this.size = size;
        grid = new char[size][size];
        generateMaze();
    }

    //metoda qe krijon labirint dinamik
    void generateMaze() {
        // inicializon te gjithin me mure
        for (int i = 0; i < size; i++) {
            Arrays.fill(grid[i], '#');
        }

        Random random = new Random();
        Stack<int[]> stack = new Stack<>();
        int[] start = {0, 0};
        stack.push(start);

        // zgjidhje per rruge nga start ne end
        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];
            grid[x][y] = '.';

            int[][] neighbors = {{x - 2, y}, {x + 2, y}, {x, y - 2}, {x, y + 2}};
            Collections.shuffle(Arrays.asList(neighbors), random);

            for (int[] neighbor : neighbors) {
                int nx = neighbor[0];
                int ny = neighbor[1];

                if (nx >= 0 && nx < size && ny >= 0 && ny < size && grid[nx][ny] == '#') {
                    // lidh qelizat me rruge
                    grid[(x + nx) / 2][(y + ny) / 2] = '.';
                    stack.push(new int[]{nx, ny});
                }
            }
        }

        // vendos start
        grid[0][0] = 'S';

        // vendos daljen ne kufinj te labirintit
        int side;
        do {
            side = random.nextInt(4);
            switch (side) {
                case 0:
                    exitX = 0;
                    exitY = random.nextInt(size);
                    break;
                case 1:
                    exitX = random.nextInt(size);
                    exitY = size - 1;
                    break;
                case 2:
                    exitX = size - 1;
                    exitY = random.nextInt(size);
                    break;
                case 3:
                    exitX = random.nextInt(size);
                    exitY = 0;
                    break;
            }
        } while (exitX == 0 && exitY == 0);  // siguron qe dalja nuk eshte ne te njejtin pozicion me hyrjen

        grid[exitX][exitY] = 'E';

        // Adjusted number of treasures to be placed
        int numTreasures = size * size / 8;  // You can adjust this ratio as needed

        // vendos thesare ne pozicione random
        for (int i = 0; i < numTreasures; i++) {
            int treasureX, treasureY;
            do {
                treasureX = random.nextInt(size);
                treasureY = random.nextInt(size);
            } while (grid[treasureX][treasureY] != '.');

            grid[treasureX][treasureY] = 'T';
        }

        // Adjusted number of extra walls to be placed
        int numExtraWalls = size * size / 16;  // You can adjust this ratio as needed

        // shton me shume mure per te krijuar path pa dalje
        for (int i = 0; i < numExtraWalls; i++) {
            int wallX, wallY;
            do {
                wallX = random.nextInt(size);
                wallY = random.nextInt(size);
            } while (grid[wallX][wallY] != '.');

            grid[wallX][wallY] = '#';
        }
    }

    //kontrollon nese nje pozicion specifik eshte mur
    boolean isWall(int x, int y) {
        return x < 0 || y < 0 || x >= size || y >= size || grid[x][y] == '#';
    }

    //kontrollon nese nje pozicion specifik eshte thesar
    boolean isTreasure(int x, int y) {
        return grid[x][y] == 'T';
    }

    //kontrollon nese nje pozicion specifik eshte dalja
    boolean isExit(int x, int y) {
        return x == exitX && y == exitY;
    }

    int[] getPlayerPosition() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == 'S') {
                    return new int[]{i, j};
                }
            }
        }
        return null;  // Player not found (should not happen if maze is properly generated)
    }

    void printMaze() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
} 