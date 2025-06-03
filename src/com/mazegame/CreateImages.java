package com.mazegame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CreateImages {
    public static void main(String[] args) {
        try {
            File resourcesDir = new File("src/resources");
            if (!resourcesDir.exists()) {
                resourcesDir.mkdirs();
            }


            createImage(80, 80, Color.WHITE, "src/resources/path.png");

            createImage(80, 80, Color.BLACK, "src/resources/wall1.jpg");

            createImage(80, 80, Color.YELLOW, "src/resources/treasure.png");

            createImage(80, 80, Color.GREEN, "src/resources/exit.png");

            createImage(80, 80, Color.RED, "src/resources/player.png");

            System.out.println("Images created successfully in src/resources/");
        } catch (IOException e) {
            System.out.println("Error creating images: " + e.getMessage());
        }
    }

    private static void createImage(int width, int height, Color color, String path) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        ImageIO.write(image, "jpg", new File(path));
    }
}