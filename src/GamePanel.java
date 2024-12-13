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

/*
    * How the board is rendered:
     * The board is made up of a grid of tiles
     * each tile contains a block state and a sprite
     * every frame, each tile is drawn
     * to add a piece to the board, we set the block state of the tile to FILLED using addPieceToBoard()
     * to remove a piece from the board, we set the block state of the tile to EMPTY using removePieceFromBoard()


     * if this rendering method is too slow, we can try to only render the tiles that are FILLED
     * and draw a background image once
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
    // but this is how we will move the piece
    int tempX = 0;
    int tempY = 0;
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
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            ArrayList<Tile> row = new ArrayList<>();
            for (int j = 0; j < BOARD_WIDTH; j++) {
                row.add(new Tile(BlockState.EMPTY, Shape.EMPTY));
            }
            gameBoard.add(row);
        }
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
                    tempY += 1;
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
                tempY += 1;
                i = 0;
            }
            i++;
        }
    }

    // renders every tile in the game board
    private void drawBoard(Graphics g) {
        for (int row = 0; row < gameBoard.size(); row++) {
            for (int column = 0; column < gameBoard.get(row).size(); column++) {
                gameBoard.get(row).get(column).setX(BOARD_X+1 + column*TILE_SIZE);
                gameBoard.get(row).get(column).setY(BOARD_Y+1 + row*TILE_SIZE);
                gameBoard.get(row).get(column).draw(g);
            }
        }

    }

    private void calculateDelta() {
        delta = System.nanoTime() - last;
        last = System.nanoTime();
        fps = ((long) 1e9 )/ delta;
    }


    // set a tile to EMPTY
    public void deleteTileOnBoard(int x, int y) {
        gameBoard.get(y).set(x, new Tile(BlockState.EMPTY, Shape.EMPTY));
    }
    public void setTileOnBoard(Tile tile, int x, int y) {
        gameBoard.get(y).set(x, tile);
    }

    // FIXME temporary code to test the piece
    Tile testTile = new Tile(BlockState.FILLED_SELECTED, Shape.Z);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + fps, 10, 10);
        // +2 to draw a border around the board
        g.fillRect(BOARD_X-1, BOARD_Y-1, TILE_SIZE*BOARD_WIDTH+2+1, TILE_SIZE*BOARD_HEIGHT+2+1);

        drawBoard(g);

        // FIXME temporary code to test the piece
        setTileOnBoard(testTile, tempX, tempY);
        if (tempY > 0) {
            deleteTileOnBoard(tempX, tempY-1);
        }

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