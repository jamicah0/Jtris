/*
    * Tile class
        * The game board will be made up of a grid of tiles
        * a tile contains a block state and a sprite
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class Tile extends Rectangle2D.Double implements Drawable {
    public final String spritesPath = "src\\Sprites\\";

    BlockState state;
    BufferedImage sprite;


    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public Tile(BlockState state, Shape texture) {
        this.state = state;

        // Set the sprite based on the texture
        switch (texture) {
            case I -> this.sprite = getImage(spritesPath + "cyan_tile.png");
            case J -> this.sprite = getImage(spritesPath + "blue_tile.png");
            case L -> this.sprite = getImage(spritesPath + "orange_tile.png");
            case O -> this.sprite = getImage(spritesPath + "yellow_tile.png");
            case S -> this.sprite = getImage(spritesPath + "green_tile.png");
            case T -> this.sprite = getImage(spritesPath + "purple_tile.png");
            case Z -> this.sprite = getImage(spritesPath + "red_tile.png");
            case EMPTY -> this.sprite = getImage(spritesPath + "empty_tile.png");
        }
    }

    public BufferedImage getImage(String path) {
        BufferedImage img = null;

        URL url = getClass().getClassLoader().getResource(path);
        try {
            assert url != null;
            img = ImageIO.read(new File(path));
        } catch (Exception e) {
            System.out.println("Error loading sprites: " + e.getStackTrace());
        }
        return img;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(sprite, (int) x, (int) y, null);
    }
}

