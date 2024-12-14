import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class Sprite extends Rectangle2D.Double implements Drawable {
    long delay;

    GamePanel parent;
    BufferedImage[] images;
    int currentFrame = 0;

    public Sprite(BufferedImage[] images, double x, double y, long delay, GamePanel panel) {
        this.images = images;
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.width = images[0].getWidth();
        this.height = images[0].getHeight();
        this.parent = panel;



    }

    public abstract void draw(Graphics g);
}
