package main.java;/*
    * main.java.Tile class
        * The game board will be made up of a grid of tiles
        * a tile contains a block state and a sprite
 */

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Tile extends Rectangle2D.Double implements Drawable {


    BlockState state;
    BufferedImage sprite;

    public static final int RED = 0;
    public static final int ORANGE = 1;
    public static final int YELLOW = 2;
    public static final int GREEN = 3;
    public static final int CYAN = 4;
    public static final int BLUE = 5;
    public static final int PURPLE = 6;
    public static final int X = 11;



    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public Tile(BlockState state, Shape texture) {
        this.state = state;


        // Set the sprite based on the texture

        try {
//            switch (texture) {
//                case I -> this.sprite = GamePanel.sprites[CYAN];
//                case J -> this.sprite = GamePanel.sprites[BLUE];
//                case L -> this.sprite = GamePanel.sprites[ORANGE];
//                case O -> this.sprite = GamePanel.sprites[YELLOW];
//                case S -> this.sprite = GamePanel.sprites[GREEN];
//                case T -> this.sprite = GamePanel.sprites[PURPLE];
//                case Z -> this.sprite = GamePanel.sprites[RED];
//                case EMPTY -> this.sprite = null;
//                case GARBAGE -> this.sprite = GamePanel.sprites[9];
//            }
            // java 8
            switch (texture) {
                case I:
                    this.sprite = GamePanel.sprites[CYAN];
                    break;
                case J:
                    this.sprite = GamePanel.sprites[BLUE];
                    break;
                case L:
                    this.sprite = GamePanel.sprites[ORANGE];
                    break;
                case O:
                    this.sprite = GamePanel.sprites[YELLOW];
                    break;
                case S:
                    this.sprite = GamePanel.sprites[GREEN];
                    break;
                case T:
                    this.sprite = GamePanel.sprites[PURPLE];
                    break;
                case Z:
                    this.sprite = GamePanel.sprites[RED];
                    break;
                case EMPTY:
                    this.sprite = null;
                    break;
                case GARBAGE:
                    this.sprite = GamePanel.sprites[9];
                    break;
            }
        } catch (NullPointerException e) {
            System.out.println("Error reading sprite sheet, specifically for " + texture);
            throw new NullPointerException();
        }

    }


    @Override
    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, (int) x, (int) y, null);
        }
    }
}

