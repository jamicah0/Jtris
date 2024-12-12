import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

/*
    * * * * *
    * JTris *
    * * * * *

    Tetris programmed in Tetris

    Gameplay Features:
    * play infinitely
    * srs rotation system
    * score system
    * show next piece
    * soft drop
    * (maybe) hard drop
    * (maybe) hold piece
    * (maybe) ghost piece
    * (maybe) wall kick

    TO-DO:
    * create a game loop    (x)
    * render board          (x)
    * piece renderer
    * move piece
    * input handling
    * collision detection
    * rotation
 */

public class GamePanel extends JPanel implements Runnable, KeyListener {
    static int TILE_SIZE = 20;
    static int BOARD_X = 10;
    static int BOARD_Y = 24;
    static int BOARD_WIDTH = 10;
    static int BOARD_HEIGHT = 24;

    // game board
    ArrayList<ArrayList<Tile>> gameBoard = new ArrayList<>();

    boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;


    // TODO these are temporary variables
    // TODO but this is how we will move the piece
    int tempX = BOARD_X;
    int tempY = BOARD_Y;
    int i = 0;
    int j = 0;


    boolean isGameRunning = true;

    long delta = 0;
    long last = 0;
    long fps = 0;

    public static void main(String[] args) {
        new GamePanel(TILE_SIZE*BOARD_WIDTH+20, TILE_SIZE + BOARD_HEIGHT*TILE_SIZE+20);
    }

    public GamePanel(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.black);
        JFrame frame = new JFrame("JTris");
        frame.setLocation(100, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(this);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        initialize();
    }

    private void initialize() {
        initializeBoard();
        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }

    private void initializeBoard() {
        // TODO initialize the game board
    }

    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();
            if (up) {

            }
            if (down) {
                // moves every 5 frames
                j++;
                if (j == 5) {
                    tempY += TILE_SIZE;
                    j = 0;
                    // reset i to 0 so that it stops for a moment
                    i = 0;
                }
            }
            if (left) {

            }
            if (right) {

            }



            try {
                Thread.sleep(10);

            } catch (InterruptedException _) {
            }
            if (i == 50) {
                tempY += TILE_SIZE;
                i = 0;
            }
            i++;
        }
    }

    private void calculateDelta() {
        delta = System.nanoTime() - last;
        last = System.nanoTime();
        fps = ((long) 1e9 )/ delta;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + fps, 10, 10);
        // +2 to draw a border around the board
        g.drawRect(BOARD_X-1, BOARD_Y-1, TILE_SIZE*BOARD_WIDTH+2, TILE_SIZE*BOARD_HEIGHT+2);

        g.setColor(Color.RED);
        g.drawRect(tempX, tempY, TILE_SIZE, TILE_SIZE);
    }

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = true;
        }
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = false;
        }
    }
}