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
    EMPTY;

    private static final Random RNG = new Random();

    public static Shape getRandomShape() {
        return values()[RNG.nextInt(values().length - 1)];
    }

    public static int getColor(Shape shape) {
        int index = -1;
        switch (shape) {
            case I -> index = Tile.CYAN;
            case J -> index = Tile.BLUE;
            case L -> index = Tile.ORANGE;
            case O -> index = Tile.YELLOW;
            case S -> index = Tile.GREEN;
            case T -> index = Tile.PURPLE;
            case Z -> index = Tile.RED;
        }

        BufferedImage sprite = GamePanel.sprites[index];

        return sprite.getRGB(GamePanel.TILE_SIZE/2, GamePanel.TILE_SIZE/2);
    }
}
