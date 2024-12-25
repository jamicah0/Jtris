import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

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
    * piece renderer        (x)
    * move piece            (x)
    * input handling
    * collision detection   (x)
    * rotation              (x)
    * hold piece
    * see next pieces
    * game over
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
    static int TILE_SIZE = 30;
    static int BOARD_X = 10;
    static int BOARD_Y = 24;
    static int BOARD_WIDTH = 10;
    static int BOARD_HEIGHT = 23;
    public static final String SPRITE_SHEET_FILE_PATH = "Sprites\\sprite_sheet.png";

    Shape[] sevenBag;

    int score;

    int currentBagIndex = 0;
    Piece currentPiece;

    public static BufferedImage[] sprites;

    // game board
    Tile[][] gameBoard = new Tile[BOARD_HEIGHT][BOARD_WIDTH];

    boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;
    boolean ctrl = false;

    // gravity
    int frameCounterAutomatic = 0;

    int frameCounterMoveDown = 0;
    int frameCounterMoveRight = 0;
    int frameCounterMoveLeft = 0;

    int DOWNWARDS_SPEED = 3;
    int ARR = 5;
    int GRAVITY = 50;


    boolean isGameRunning = true;

    long delta = 0;
    long last = 0;
    long fps = 0;


    public GamePanel(int width, int height) {
        initialize();
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.BLACK);
        JFrame frame = new JFrame("JTris");

        // center of the screen (relative to the screen size)
        frame.setLocation(
                Toolkit.getDefaultToolkit()
                        .getScreenSize()
                        .width/2 - width/2,
                0
        );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(this);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);

    }

    private void printCurrentBag() {
        for (Shape shape : sevenBag) {
            System.out.print(shape + " ");
        }
        System.out.println();
    }

    private void initialize() {
        loadSprites();

        initializeBoard();

        Shape[] shapeBag = Shape.values();
        sevenBag = new Shape[7];

        System.arraycopy(shapeBag, 0, sevenBag, 0, 7);
        shuffleBag();

        printCurrentBag();


        currentPiece = new Piece(sevenBag[0]);
        currentPiece.insertPieceIntoBoard(gameBoard);
        currentBagIndex++;

        score = 0;

        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }

    private void loadSprites() {
        BufferedImage spriteSheet;

        URL url = getClass().getClassLoader().getResource(SPRITE_SHEET_FILE_PATH);
        try {
            assert url != null;
            spriteSheet = ImageIO.read(url);

            sprites = new BufferedImage[12];

            // put sprites into an array
            for (int i = 0; i < 7; i++) {
                sprites[i] = spriteSheet.getSubimage(
                        i*GamePanel.TILE_SIZE + i,
                        0,
                        GamePanel.TILE_SIZE,
                        GamePanel.TILE_SIZE
                );
            }
            for (int i = 8; i < 12; i++) {
                sprites[i] = spriteSheet.getSubimage(
                        i*GamePanel.TILE_SIZE + i + 2,
                        0,
                        GamePanel.TILE_SIZE,
                        GamePanel.TILE_SIZE
                );
            }
        } catch (Exception e) {
            System.out.println("Image couldn't be read! Path: " + url);
        }
    }

    private void shuffleBag() {
        Collections.shuffle(Arrays.asList(sevenBag));
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int column = 0; column < BOARD_WIDTH; column++) {
                gameBoard[row][column] = new Tile(BlockState.EMPTY, Shape.EMPTY);
            }
        }
    }


    // TODO: implement DAS (delayed auto shift)
    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();

            if (currentPiece.isLocked) {
                currentPiece = new Piece(sevenBag[currentBagIndex]);
                currentPiece.insertPieceIntoBoard(gameBoard);
                currentBagIndex++;

                if (currentBagIndex > 6) {
                    shuffleBag();
                    currentBagIndex = 0;
                    printCurrentBag();
                }
            }

            checkKeys();

            if (frameCounterAutomatic == GRAVITY) {
                frameCounterAutomatic = 0;

                currentPiece.moveDown(gameBoard);
            }
            frameCounterAutomatic++;

            try {
                Thread.sleep(10);
            } catch (InterruptedException _) {}

        }
    }

    boolean pressedPrevious = false;

    private void checkKeys() {
        // rotation

        // make sure that the key is only pressed once
        if (up && !pressedPrevious) {
            currentPiece.rotateClockwise(gameBoard);
            repaint();
            pressedPrevious = true;
        } else if (!up) {
            pressedPrevious = false;
        }


        if (ctrl) {
            currentPiece.rotateCounterClockwise(gameBoard);
        }


        // vertical & horizontal movement

        if (down) {

            // moves every 5 frames
            frameCounterMoveDown++;
            if (frameCounterMoveDown == DOWNWARDS_SPEED) {
                score++;
                frameCounterMoveDown = 0;
                currentPiece.moveDown(gameBoard);
                repaint();
                // reset i to 0 so that it stops for a moment
                frameCounterAutomatic = 0;
            }

        } else {
            frameCounterMoveDown = 0;
        }

        if (left && !right) {
            frameCounterMoveLeft++;
            // TODO: Delayed auto shift
            if (frameCounterMoveLeft == ARR) {
                frameCounterMoveLeft = 0;
                currentPiece.moveLeft(gameBoard);
                repaint();
                // reset i to 0 so that it stops for a moment
            }
            repaint();
        } else {
            frameCounterMoveLeft = 0;
        }

        if (right && !left) {
            frameCounterMoveRight++;
            if (frameCounterMoveRight == ARR) {
                frameCounterMoveRight = 0;
                currentPiece.moveRight(gameBoard);
                repaint();

            }
            repaint();
        } else {
            frameCounterMoveRight = 0;
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
        g.drawString("j:" + frameCounterMoveDown, 10, 20);
        g.drawString("Score: " + score, TILE_SIZE*BOARD_WIDTH+2+1 + 20,(TILE_SIZE*BOARD_HEIGHT+2+1)/2 );
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
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = true;
            case KeyEvent.VK_DOWN -> down = true;
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
            case KeyEvent.VK_CONTROL -> ctrl = true;
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
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = false;
            case KeyEvent.VK_DOWN -> down = false;
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
            case KeyEvent.VK_CONTROL -> ctrl = false;
        }


    }
}