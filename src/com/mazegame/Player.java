package com.mazegame;

import java.io.Serializable;

public class Player implements Serializable {
    int x, y;
    int treasuresCollected;
    int points;

    public int getTreasuresCollected() {
        return treasuresCollected;
    }

    public int getPoints() {
        return points;
    }

  
    void move(Direction direction, Maze maze) {
        switch (direction) {
            case UP:
                int x1=x-1;
                if (checkWall(x1--, y,maze))
                   x--;
                break;
            case DOWN:
                int x2=x+1;
                if (checkWall(x2, y, maze))
                x++;
                break;
            case LEFT:
                int y1=y+1;
                if (checkWall(x, y1, maze))
                y++;
                break;
            case RIGHT:
                int y2=y-1;
                if (checkWall(x, y2, maze))
                y--;
                break;
        }
    }

    void move(int x, int y) {
        this.x=x;
        this.y=y;
    }
    
    void collectTreasure() {
        treasuresCollected++;
        points += 10;
    }

    public boolean checkWall (int x, int y, Maze maze){
        if (maze.isWall(x, y)) {
            System.out.println("U perplaset me nje mur. Zgjidhni nje levizje tjeter.");
            return false;
        }
        else
            return true;
    }
} 