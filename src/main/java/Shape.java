package main.java;

import java.awt.image.BufferedImage;
import java.util.Random;

public enum Shape {
    I,
    J,
    L,
    O,
    S,
    T,
    Z,
    GARBAGE,
    EMPTY;

    private static final Random RNG = new Random();

    public static Shape getRandomShape() {
        return values()[RNG.nextInt(values().length - 2)];
    }

    public static int getColor(Shape shape) {
        int index = -1;
        switch (shape) {
            case I:
                index = Tile.CYAN;
                break;
            case J:
                index = Tile.BLUE;
                break;
            case L:
                index = Tile.ORANGE;
                break;
            case O:
                index = Tile.YELLOW;
                break;
            case S:
                index = Tile.GREEN;
                break;
            case T:
                index = Tile.PURPLE;
                break;
            case Z:
                index = Tile.RED;
                break;
        }

        BufferedImage sprite = GamePanel.sprites[index];

        return sprite.getRGB(GamePanel.TILE_SIZE/2, GamePanel.TILE_SIZE/2);
    }
}
