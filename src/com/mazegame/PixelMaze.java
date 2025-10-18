package com.mazegame;

import java.io.Serializable;
import java.util.*;

public class PixelMaze implements Serializable {
    private static final long serialVersionUID = 1L;

    private char[][] grid;
    private List<int[]> treasurePositions;
    private List<int[]> lifePotionPositions;
    private int exitX, exitY;
    private int startX, startY;
    private int width, height;
    private transient Random random; // Make Random transient
    private static final int TILE_SIZE = 64;
    private int lifePotionCount = 3; // Default number of life potions
    private int difficulty; // Add difficulty field

    public PixelMaze(int width, int height) {
        this(width, height, 1); // Default to medium difficulty
    }

    public PixelMaze(int width, int height, int difficulty) {
        this.width = width;
        this.height = height;
        this.difficulty = difficulty;
        this.random = new Random();
        this.grid = new char[height][width];
        this.treasurePositions = new ArrayList<>();
        this.lifePotionPositions = new ArrayList<>();

        // Apply difficulty settings before generation
        applyDifficultySettings();
        generateMaze();
    }

    private void applyDifficultySettings() {
        switch (difficulty) {
            case 0: // EASY
                this.lifePotionCount = 5; // More life potions
                System.out.println("Easy maze settings: " + lifePotionCount + " life potions");
                break;
            case 1: // MEDIUM
                this.lifePotionCount = 3; // Standard life potions
                System.out.println("Medium maze settings: " + lifePotionCount + " life potions");
                break;
            case 2: // HARD
                this.lifePotionCount = 2; // Fewer life potions
                System.out.println("Hard maze settings: " + lifePotionCount + " life potions");
                break;
        }
    }

    public void generateMaze() {
        // Initialize everything as walls
        for (int i = 0; i < height; i++) {
            Arrays.fill(grid[i], '#');
        }

        // Use recursive backtracking for perfect maze
        carvePassages(1, 1);

        // Apply maze complexity based on difficulty
        applyMazeComplexity();

        // Ensure start position is clear and track it
        startX = 1;
        startY = 1;
        grid[startY][startX] = 'S';

        // Place exit at farthest point
        placeExit();

        // Place treasures and life potions
        placeTreasures();
        placeLifePotions();

        // Debug: print maze to console
        printMazeToConsole();

        System.out.println("Maze generated: " + width + "x" + height + " (Difficulty: " + difficulty + ")");
        System.out.println("Start position: (" + startX + ", " + startY + ")");
        System.out.println("Exit position: (" + exitX + ", " + exitY + ")");
    }

    private void carvePassages(int x, int y) {
        grid[y][x] = '.';

        int[][] directions = {{-2,0}, {2,0}, {0,-2}, {0,2}};
        shuffleDirections(directions);

        for (int[] dir : directions) {
            int nextX = x + dir[0];
            int nextY = y + dir[1];

            if (nextX > 0 && nextX < width-1 && nextY > 0 && nextY < height-1 &&
                    grid[nextY][nextX] == '#') {
                // Carve passage between cells
                grid[y + dir[1]/2][x + dir[0]/2] = '.';
                carvePassages(nextX, nextY);
            }
        }
    }

    private void shuffleDirections(int[][] directions) {
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }
    }

    private void applyMazeComplexity() {
        switch (difficulty) {
            case 0: // EASY - simpler maze, fewer dead ends
                simplifyMaze();
                break;
            case 1: // MEDIUM - standard maze
                // No changes, keep as generated
                break;
            case 2: // HARD - more complex maze with more dead ends
                complexifyMaze();
                break;
        }
    }

    private void simplifyMaze() {
        System.out.println("Simplifying maze for easy difficulty...");
        // Remove some walls to create more open paths
        int wallsRemoved = 0;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == '#' && random.nextFloat() < 0.15f) { // 15% chance to remove wall
                    // Check if removing this wall creates a valid path
                    if (isIsolatedWall(x, y)) {
                        grid[y][x] = '.';
                        wallsRemoved++;
                    }
                }
            }
        }
        System.out.println("Removed " + wallsRemoved + " walls for simpler maze");
    }

    private void complexifyMaze() {
        System.out.println("Making maze more complex for hard difficulty...");

        // First, make a copy of the grid to test changes
        char[][] originalGrid = copyGrid();

        int wallsAdded = 0;
        List<int[]> addedWalls = new ArrayList<>();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == '.' && random.nextFloat() < 0.08f) { // Reduced from 0.1 to 0.08
                    // Only add wall if it doesn't block critical path
                    if (!wouldBlockCriticalPath(x, y)) {
                        grid[y][x] = '#';
                        addedWalls.add(new int[]{x, y});
                        wallsAdded++;
                    }
                }
            }
        }

        // Verify that exit is still reachable
        if (!isExitReachable()) {
            System.out.println("Exit became unreachable after complexification, reverting some walls...");
            revertSomeWalls(addedWalls);
        }

        System.out.println("Added " + wallsAdded + " walls for more complex maze");
    }

    private char[][] copyGrid() {
        char[][] copy = new char[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(grid[y], 0, copy[y], 0, width);
        }
        return copy;
    }

    private boolean isExitReachable() {
        boolean[][] visited = new boolean[height][width];
        return canReachExit(startX, startY, visited);
    }

    private boolean canReachExit(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= width || y < 0 || y >= height || visited[y][x] || grid[y][x] == '#') {
            return false;
        }

        if (grid[y][x] == 'E') {
            return true;
        }

        visited[y][x] = true;

        // Check all four directions
        return canReachExit(x + 1, y, visited) ||
                canReachExit(x - 1, y, visited) ||
                canReachExit(x, y + 1, visited) ||
                canReachExit(x, y - 1, visited);
    }

    private void revertSomeWalls(List<int[]> addedWalls) {
        // Revert about half of the added walls to ensure path exists
        int wallsToRevert = addedWalls.size() / 2;
        Collections.shuffle(addedWalls, random);

        for (int i = 0; i < wallsToRevert && i < addedWalls.size(); i++) {
            int[] wall = addedWalls.get(i);
            grid[wall[1]][wall[0]] = '.';
        }
        System.out.println("Reverted " + wallsToRevert + " walls to maintain exit path");
    }
    private boolean isIsolatedWall(int x, int y) {
        // Count path neighbors - if surrounded by walls, it's isolated and safe to remove
        int pathNeighbors = 0;
        int[][] neighbors = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] dir : neighbors) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && grid[ny][nx] == '.') {
                pathNeighbors++;
            }
        }
        return pathNeighbors <= 1; // Safe to remove if not many path connections
    }

    private boolean wouldBlockCriticalPath(int x, int y) {
        // Test if adding this wall would make exit unreachable
        char original = grid[y][x];
        grid[y][x] = '#'; // Temporarily add the wall

        boolean exitReachable = isExitReachable();

        grid[y][x] = original; // Restore original

        return !exitReachable;
    }
    private int countDeadEndsAround(int x, int y) {
        int deadEnds = 0;
        int[][] neighbors = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] dir : neighbors) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && grid[ny][nx] == '.') {
                if (countPathNeighbors(nx, ny) <= 1) {
                    deadEnds++;
                }
            }
        }
        return deadEnds;
    }



    private void placeExit() {
        // Find farthest reachable point from start using BFS
        boolean[][] visited = new boolean[height][width];
        int[][] distance = new int[height][width];
        Queue<int[]> queue = new LinkedList<>();

        queue.offer(new int[]{startX, startY});
        visited[startY][startX] = true;
        distance[startY][startX] = 0;

        List<int[]> candidatePositions = new ArrayList<>();
        int maxDistance = -1;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0], y = current[1];

            // Add all reachable positions as candidates
            candidatePositions.add(new int[]{x, y, distance[y][x]});

            if (distance[y][x] > maxDistance) {
                maxDistance = distance[y][x];
            }

            int[][] neighbors = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            for (int[] dir : neighbors) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                        !visited[ny][nx] && grid[ny][nx] != '#') {
                    visited[ny][nx] = true;
                    distance[ny][nx] = distance[y][x] + 1;
                    queue.offer(new int[]{nx, ny});
                }
            }
        }

        // Filter to only good exit candidates that are actually reachable
        List<int[]> goodCandidates = new ArrayList<>();
        for (int[] candidate : candidatePositions) {
            int x = candidate[0], y = candidate[1], dist = candidate[2];
            if (isGoodExitCandidate(x, y, dist) && dist >= maxDistance * 0.7) { // At least 70% of max distance
                goodCandidates.add(candidate);
            }
        }

        if (!goodCandidates.isEmpty()) {
            // Sort by distance (farthest first)
            goodCandidates.sort((a, b) -> Integer.compare(b[2], a[2]));

            // Take one of the top candidates
            int index = Math.min(2, goodCandidates.size() - 1);
            int[] bestExit = goodCandidates.get(index);

            exitX = bestExit[0];
            exitY = bestExit[1];
        } else {
            // Fallback to any farthest reachable point
            for (int[] candidate : candidatePositions) {
                if (candidate[2] == maxDistance) {
                    exitX = candidate[0];
                    exitY = candidate[1];
                    break;
                }
            }
        }

        grid[exitY][exitX] = 'E';
        System.out.println("Exit placed at: (" + exitX + ", " + exitY + ") - Distance from start: " +
                (exitX != -1 ? calculateDistanceFromStart(exitX, exitY) : "unknown"));
    }

    private boolean isGoodExitCandidate(int x, int y, int distanceFromStart) {
        // Must be a path tile
        if (grid[y][x] != '.') return false;

        // Must be sufficiently far from start (at least 1/3 of maze size)
        int minDistance = Math.min(width, height) / 3;
        if (distanceFromStart < minDistance) return false;

        // Should be in a corner or edge area for more challenge
        boolean isNearEdge = (x <= 2 || x >= width - 3 || y <= 2 || y >= height - 3);

        // Should have limited neighbors (dead end or corridor end is good for exit)
        int pathNeighbors = countPathNeighbors(x, y);
        boolean isDeadEnd = pathNeighbors <= 1;

        // Good candidates are near edges or in dead ends
        return isNearEdge || isDeadEnd;
    }

    private void findBetterExitPosition() {
        // Try to find a better exit position manually
        List<int[]> edgePositions = new ArrayList<>();

        // Look for path tiles near the edges
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (grid[y][x] == '.' && isGoodExitCandidate(x, y, calculateDistanceFromStart(x, y))) {
                    edgePositions.add(new int[]{x, y, calculateDistanceFromStart(x, y)});
                }
            }
        }

        if (!edgePositions.isEmpty()) {
            // Sort by distance from start (farthest first)
            edgePositions.sort((a, b) -> Integer.compare(b[2], a[2]));

            // Remove old exit
            grid[exitY][exitX] = '.';

            // Place new exit
            int[] bestPosition = edgePositions.get(0);
            exitX = bestPosition[0];
            exitY = bestPosition[1];
            grid[exitY][exitX] = 'E';

            System.out.println("Better exit found at: (" + exitX + ", " + exitY + ") - Distance: " + bestPosition[2]);
        }
    }

    private int calculateDistanceFromStart(int x, int y) {
        return Math.abs(x - startX) + Math.abs(y - startY);
    }

    private int countPathNeighbors(int x, int y) {
        int count = 0;
        int[][] neighbors = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] dir : neighbors) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && grid[ny][nx] == '.') {
                count++;
            }
        }
        return count;
    }
    private void placeTreasures() {
        int numTreasures = Math.max(1, (width * height) / 30);

        for (int i = 0; i < numTreasures; i++) {
            int attempts = 0;
            while (attempts < 100) {
                int x = 1 + random.nextInt(width - 2);
                int y = 1 + random.nextInt(height - 2);

                if (grid[y][x] == '.' && !isNearImportantLocation(x, y)) {
                    grid[y][x] = 'T';
                    treasurePositions.add(new int[]{x, y});
                    break;
                }
                attempts++;
            }
        }
        System.out.println("Placed " + numTreasures + " treasures");
    }

    private void placeLifePotions() {
        int potionsPlaced = 0;
        int attempts = 0;

        while (potionsPlaced < lifePotionCount && attempts < 200) {
            int x = 1 + random.nextInt(width - 2);
            int y = 1 + random.nextInt(height - 2);

            // Check if this is a valid position (path tile, not start/exit, not treasure, not already a potion)
            if (grid[y][x] == '.' &&
                    !isStartTile(x, y) &&
                    !isExitTile(x, y) &&
                    !isTreasureTile(x, y) &&
                    !isLifePotionTile(x, y) &&
                    !isNearImportantLocation(x, y)) {

                grid[y][x] = 'L'; // 'L' for Life Potion
                lifePotionPositions.add(new int[]{x, y});
                potionsPlaced++;
                System.out.println("Life potion placed at: (" + x + ", " + y + ")");
            }
            attempts++;
        }
        System.out.println("Placed " + potionsPlaced + " life potions");
    }

    private boolean isStartTile(int x, int y) {
        return x == startX && y == startY;
    }

    private boolean isExitTile(int x, int y) {
        return x == exitX && y == exitY;
    }

    private boolean isTreasureTile(int x, int y) {
        return grid[y][x] == 'T';
    }

    public boolean isLifePotionTile(int x, int y) {
        return grid[y][x] == 'L';
    }

    private boolean isNearImportantLocation(int x, int y) {
        int startDist = Math.abs(x - startX) + Math.abs(y - startY);
        int exitDist = Math.abs(x - exitX) + Math.abs(y - exitY);
        return startDist < 4 || exitDist < 4;
    }

    // PIXEL-BASED COLLISION DETECTION
    public boolean isWallAtPixel(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        // Convert pixel coordinates to grid cells the player overlaps with
        // Add a small margin to prevent getting stuck on edges
        float margin = 2.0f;
        int leftCell = (int)((pixelX + margin) / TILE_SIZE);
        int rightCell = (int)((pixelX + playerWidth - margin) / TILE_SIZE);
        int topCell = (int)((pixelY + margin) / TILE_SIZE);
        int bottomCell = (int)((pixelY + playerHeight - margin) / TILE_SIZE);

        // Check all overlapping cells for walls
        for (int x = leftCell; x <= rightCell; x++) {
            for (int y = topCell; y <= bottomCell; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (grid[y][x] == '#') {
                        return true;
                    }
                } else {
                    // Out of bounds counts as wall
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTreasureAtPixel(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        int centerCellX = (int)((pixelX + playerWidth / 2) / TILE_SIZE);
        int centerCellY = (int)((pixelY + playerHeight / 2) / TILE_SIZE);

        if (centerCellX >= 0 && centerCellX < width && centerCellY >= 0 && centerCellY < height) {
            return grid[centerCellY][centerCellX] == 'T';
        }
        return false;
    }

    public boolean isLifePotionAtPixel(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        int centerCellX = (int)((pixelX + playerWidth / 2) / TILE_SIZE);
        int centerCellY = (int)((pixelY + playerHeight / 2) / TILE_SIZE);

        if (centerCellX >= 0 && centerCellX < width && centerCellY >= 0 && centerCellY < height) {
            return grid[centerCellY][centerCellX] == 'L';
        }
        return false;
    }

    public boolean isExitAtPixel(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        int centerCellX = (int)((pixelX + playerWidth / 2) / TILE_SIZE);
        int centerCellY = (int)((pixelY + playerHeight / 2) / TILE_SIZE);

        if (centerCellX >= 0 && centerCellX < width && centerCellY >= 0 && centerCellY < height) {
            return grid[centerCellY][centerCellX] == 'E';
        }
        return false;
    }

    public void collectTreasureAt(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        int centerCellX = (int)((pixelX + playerWidth / 2) / TILE_SIZE);
        int centerCellY = (int)((pixelY + playerHeight / 2) / TILE_SIZE);

        if (centerCellX >= 0 && centerCellX < width && centerCellY >= 0 && centerCellY < height &&
                grid[centerCellY][centerCellX] == 'T') {
            grid[centerCellY][centerCellX] = '.';
            // Remove from treasure positions
            for (int i = 0; i < treasurePositions.size(); i++) {
                int[] pos = treasurePositions.get(i);
                if (pos[0] == centerCellX && pos[1] == centerCellY) {
                    treasurePositions.remove(i);
                    break;
                }
            }
        }
    }

    public void collectLifePotionAt(float pixelX, float pixelY, int playerWidth, int playerHeight) {
        int centerCellX = (int)((pixelX + playerWidth / 2) / TILE_SIZE);
        int centerCellY = (int)((pixelY + playerHeight / 2) / TILE_SIZE);

        if (centerCellX >= 0 && centerCellX < width && centerCellY >= 0 && centerCellY < height &&
                grid[centerCellY][centerCellX] == 'L') {
            grid[centerCellY][centerCellX] = '.';
            // Remove from life potion positions
            for (int i = 0; i < lifePotionPositions.size(); i++) {
                int[] pos = lifePotionPositions.get(i);
                if (pos[0] == centerCellX && pos[1] == centerCellY) {
                    lifePotionPositions.remove(i);
                    System.out.println("Life potion collected at: (" + pos[0] + ", " + pos[1] + ")");
                    break;
                }
            }
        }
    }

    // Get start position in pixels (centered in the start cell for 48x64 player)
    public float getStartPixelX() {
        return (startX + 0.5f) * TILE_SIZE - 24; // Center 48px wide player in 64px tile
    }

    public float getStartPixelY() {
        return (startY + 0.5f) * TILE_SIZE - 32; // Center 64px tall player in 64px tile
    }

    // Debug method to print maze to console
    private void printMazeToConsole() {
        System.out.println("=== MAZE LAYOUT ===");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x] + " ");
            }
            System.out.println();
        }
        System.out.println("===================");
    }

    // Debug method to analyze tile distribution
    public void debugMazeTiles() {
        System.out.println("=== MAZE TILE ANALYSIS ===");
        int pathCount = 0;
        int wallCount = 0;
        int treasureCount = 0;
        int lifePotionCount = 0;
        int startCount = 0;
        int exitCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
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

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public char[][] getGrid() { return grid; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public List<int[]> getLifePotionPositions() { return lifePotionPositions; }
    public int getDifficulty() { return difficulty; }

    // Custom serialization to handle transient fields
    private void readObject(java.io.ObjectInputStream ois)
            throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Reinitialize transient fields after deserialization
        this.random = new Random();
        System.out.println("PixelMaze transient fields reinitialized after loading");
    }
}