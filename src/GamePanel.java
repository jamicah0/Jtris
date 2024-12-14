import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
    static int BOARD_HEIGHT = 23;

    Piece currentPiece = new Piece(Shape.randomShape());

    // game board
    Tile[][] gameBoard = new Tile[BOARD_HEIGHT][BOARD_WIDTH];

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
        initialize();
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.black);
        JFrame frame = new JFrame("JTris");

        // center of the screen (relative to the screen size)
        frame.setLocation(
                Toolkit.getDefaultToolkit()
                        .getScreenSize()
                        .width/2 - width/2,
                Toolkit.getDefaultToolkit()
                        .getScreenSize()
                        .height/2 - height/2
        );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(this);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);

    }

    private void initialize() {
        initializeBoard();
        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int column = 0; column < BOARD_WIDTH; column++) {
                gameBoard[row][column] = new Tile(BlockState.EMPTY, Shape.EMPTY);
            }
        }
        currentPiece.insertPieceIntoBoard(gameBoard);
    }


    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();

            if (currentPiece.isLocked) {
                currentPiece = new Piece(Shape.randomShape());
                currentPiece.insertPieceIntoBoard(gameBoard);
            }

            if (up) {

            }
            if (down) {

                // moves every 5 frames
                j++;
                if (j == 6) {
                    tempY += 1;
                    j = 0;
                    currentPiece.moveDown(gameBoard);
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
            } catch (InterruptedException _) {}
            if (i == 50) {
                /*
                tempY += 1;
                 */
                i = 0;

                currentPiece.moveDown(gameBoard);
            }
            i++;
        }
    }

    // renders every tile in the game board
    private void drawBoard(Graphics g) {
        for (int row = 0; row < gameBoard.length; row++) {
            for (int column = 0; column < gameBoard[row].length; column++) {
                if (row < 3 && gameBoard[row][column].state == BlockState.EMPTY)  {
                    continue;
                }
                gameBoard[row][column].setX(BOARD_X+1 + column*TILE_SIZE);
                gameBoard[row][column].setY(BOARD_Y+1 + row*TILE_SIZE);
                gameBoard[row][column].draw(g);
            }
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
        g.drawString("i:" + i, 10, 20);
        // +2 to draw a border around the board
        g.drawRect(BOARD_X-1, BOARD_Y-1, TILE_SIZE*BOARD_WIDTH+2+1, TILE_SIZE*BOARD_HEIGHT+2+1);

        drawBoard(g);
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