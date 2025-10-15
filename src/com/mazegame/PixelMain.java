package com.mazegame;

import javax.swing.*;

public class PixelMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PixelGameGUI game = new PixelGameGUI();
        });
    }
}
