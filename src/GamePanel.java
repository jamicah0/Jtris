import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

    Tetris standardized Guidelines:
    * Playfield atleast 10 cells wide   (x)
    * Playfield atleast 22 cells tall   (x)
    * correct tetromino colors          (x)
    * correct tetromino start locations (x)
    * SRS rotation                      (x)
    * standard controls
    * 7 bag system                      (x)
    * hold piece                        (x)
    * ghost piece
    * soft drop                         (x)
    * leveling with clearing lines
    * game over on top out

    TO-DO:
    * create a game loop    (x)
    * render board          (x)
    * piece renderer        (x)
    * move piece            (x)
    * collision detection   (x)
    * rotation              (x)
    * input handling        (x)
    * hold piece            (x)
    * see next pieces       (x)
    * hard drop
    * ghost piece
    * show incoming piece
    * clear lines
    * (optional) correct lock delay
    * scoring
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
    enum Direction {
        LEFT,
        RIGHT,
        NONE
    }
    final static int TILE_SIZE = 30;
    final static int BOARD_X = 150;
    final static int BOARD_Y = 34;
    final static int BOARD_WIDTH = 10;
    final static int BOARD_HEIGHT = 23;
    final static int HOLD_X = 30;
    final static int HOLD_Y = 153;
    final static int NEXT_QUEUE_X = 482;
    final static int NEXT_QUEUE_Y = 168;
    public static final String SPRITE_SHEET_FILE_PATH = "Sprites\\sprite_sheet.png";

    Shape[] sevenBag;
    Shape[] nextSevenBag;
    ArrayList<Shape> nextQueue;

    int score;

    int currentBagIndex;
    Tetromino currentTetrimino;
    Tetromino holdTetrimino;
    int AMOUNT_NEXT_PIECES = 5;

    public static BufferedImage[] sprites;

    // game board
    Tile[][] gameBoard;
    Tile[][] hold;

    boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;
    boolean ctrl = false;
    boolean shift = false;

    // gravity
    int gravityCounter = 0;
    int GRAVITY = 80;

    int dasCounter = 0;
    int arrCounter = 0;
    int sdfCounter = 0;
    int DAS = 17;
    int ARR = 5;
    int SDF = GRAVITY / 20;




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
        for (Shape shape : nextSevenBag) {
            System.out.print(shape + " ");
        }
        System.out.println();
        System.out.println("-------------------------");
    }

    private void initialize() {
        loadSprites();

        initializeBoard();

        currentBagIndex = 0;
        nextQueue = new ArrayList<>();
        Shape[] shapeBag = Shape.values();
        sevenBag = new Shape[7];
        nextSevenBag = new Shape[7];

        System.arraycopy(shapeBag, 0, sevenBag, 0, 7);
        System.arraycopy(shapeBag, 0, nextSevenBag, 0, 7);
        shuffleBag(sevenBag);
        shuffleBag(nextSevenBag);

        getNextQueue();

        printCurrentBag();

        holdTetrimino = null;
        currentTetrimino = new Tetromino(sevenBag[0]);
        currentTetrimino.insertPieceIntoBoard(gameBoard);
        currentBagIndex++;

        score = 0;

        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }


    private void getNextQueue() {
        nextQueue.clear();
        nextQueue.addAll(Arrays.asList(sevenBag));
        nextQueue.addAll(Arrays.asList(nextSevenBag));
        System.out.println(nextQueue);
    }

    private void loadSprites() {
        BufferedImage spriteSheet;

        URL url = getClass().getClassLoader().getResource(SPRITE_SHEET_FILE_PATH);
        try {
            assert url != null;
            spriteSheet = ImageIO.read(url);

            sprites = new BufferedImage[12];

            // put sprites into an array
            for (int i = 0; i < 12; i++) {
                sprites[i] = spriteSheet.getSubimage(
                        i*GamePanel.TILE_SIZE + i,
                        0,
                        GamePanel.TILE_SIZE,
                        GamePanel.TILE_SIZE
                );
            }
        } catch (IOException e) {
            System.out.println("Image couldn't be read! Path: " + url);
        }
    }

    private void shuffleBag(Shape[] bag) {
        Collections.shuffle(Arrays.asList(bag));
    }

    private void initializeBoard() {
        gameBoard = new Tile[BOARD_HEIGHT][BOARD_WIDTH];
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int column = 0; column < BOARD_WIDTH; column++) {
                gameBoard[row][column] = new Tile(BlockState.EMPTY, Shape.EMPTY);
            }
        }
    }


    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException _) {}

            if (currentTetrimino.isLocked) {
                insertNextTetrimino();
                canHold = true;
            }

            checkInput();

            if (gravityCounter == GRAVITY) {
                gravityCounter = 0;
                currentTetrimino.moveDown(gameBoard);
                repaint();
            }
            gravityCounter++;
        }
    }

    private void insertNextTetrimino() {
        currentTetrimino = new Tetromino(sevenBag[currentBagIndex]);
        currentTetrimino.insertPieceIntoBoard(gameBoard);
        currentBagIndex++;

        if (currentBagIndex > 6) {
            System.arraycopy(nextSevenBag, 0, sevenBag, 0, 7);
            shuffleBag(nextSevenBag);
            currentBagIndex = 0;
            getNextQueue();
            printCurrentBag();
        }
    }


    boolean pressedOnceUp = false;
    boolean pressedOnceCtrl = false;
    boolean pressedOnceLeft = false;
    boolean pressedOnceRight = false;
    boolean pressedOnceShift = false;

    boolean canHold = true;

    Direction dasDirection = Direction.NONE;
    private void checkInput() {
        // rotation

        // make sure that the key is only pressed once
        if (up && !pressedOnceUp) {
            currentTetrimino.rotateClockwise(gameBoard);
            repaint();
            pressedOnceUp = true;
        } else if (!up) {
            pressedOnceUp = false;
        }


        if (ctrl && !pressedOnceCtrl) {
            currentTetrimino.rotateCounterClockwise(gameBoard);
            repaint();
            pressedOnceCtrl = true;
        } else if (!ctrl) {
            pressedOnceCtrl = false;
        }



        // vertical & horizontal movement

        /*
        if (right) {
            if (dasCounter == DAS) {
                if (arrCounter == ARR) {
                    currentPiece.moveRight(gameBoard);
                    repaint();
                    arrCounter = 0;
                } else {
                    arrCounter++;
                }
            } else {
                dasCounter++;
            }
        } else {
            dasCounter = 0;
            arrCounter = 0;
        }
        */

        if (right) {
            if (!pressedOnceRight) {
                currentTetrimino.moveRight(gameBoard);
                repaint();
                pressedOnceRight = true;
            }
            if (dasDirection != Direction.RIGHT) {
                dasCounter = 0;
                dasDirection = Direction.RIGHT;
            } else {
                if (dasCounter < DAS) {
                    dasCounter++;
                } else if (arrCounter < ARR) {
                    arrCounter++;
                } else {
                    currentTetrimino.moveRight(gameBoard);
                    repaint();
                    arrCounter = 0;
                }
            }
        } else if (left) {
            if (!pressedOnceLeft) {
                currentTetrimino.moveLeft(gameBoard);
                repaint();
                pressedOnceLeft = true;
            }
            if (dasDirection != Direction.LEFT) {
                dasCounter = 0;
                dasDirection = Direction.LEFT;
            } else {
                if (dasCounter < DAS) {
                    dasCounter++;
                } else if (arrCounter < ARR) {
                    arrCounter++;
                } else {
                    currentTetrimino.moveLeft(gameBoard);
                    repaint();
                    arrCounter = 0;
                }
            }
        } else {
            pressedOnceLeft = false;
            pressedOnceRight = false;
            dasCounter = 0;
            arrCounter = 0;
            dasDirection = Direction.NONE;
        }

        if (down) {
            sdfCounter++;
            if (sdfCounter == SDF) {
                sdfCounter = 0;
                currentTetrimino.moveDown(gameBoard);
                repaint();
                gravityCounter = 0;
            }
        } else {
            sdfCounter = 0;
        }

        if (shift && !pressedOnceShift && canHold) {
            if (holdTetrimino == null) {
                canHold = false;
                currentTetrimino.deleteCurrentTetrimino(gameBoard);

                copyToDisplayedHold();


                holdTetrimino = currentTetrimino;
                insertNextTetrimino();
            } else {
                canHold = false;
                currentTetrimino.deleteCurrentTetrimino(gameBoard);

                copyToDisplayedHold();

                swapHold();
                insertHoldTetrimino();
            }



            repaint();

            pressedOnceShift = true;
        } else if (!shift) {
            pressedOnceShift = false;
        }

    }

    private void copyToDisplayedHold() {
        int[][] temporary = RotationSRS.getRotation(currentTetrimino.shape, 0);
        assert temporary != null;
        hold = new Tile[temporary.length][temporary[0].length];
        hold = convertToTiles(temporary, currentTetrimino.shape);
    }

    private void drawNextQueue(Graphics g) {
        int xPos = 0, yPos = 0;
        for (int i = currentBagIndex; i < currentBagIndex + AMOUNT_NEXT_PIECES; i++) {
            int[][] temp = RotationSRS.getRotation(nextQueue.get(i), 0);
            assert temp != null;
            Tile[][] piece = convertToTiles(temp, nextQueue.get(i));

            for (int y = 0; y < piece.length; y++) {
                for (int x = 0; x < piece[y].length; x++) {
                    if (nextQueue.get(i) == Shape.I) {
                        xPos = NEXT_QUEUE_X-(TILE_SIZE/2) + x * TILE_SIZE;
                        yPos = ((i - currentBagIndex) * TILE_SIZE * 3) - (TILE_SIZE/2)  + NEXT_QUEUE_Y + y * TILE_SIZE;
                    } else if (nextQueue.get(i) == Shape.O) {
                        xPos = NEXT_QUEUE_X+(TILE_SIZE/2) + x * TILE_SIZE;
                        yPos = ((i - currentBagIndex) * TILE_SIZE * 3) + NEXT_QUEUE_Y + y * TILE_SIZE;
                    } else {
                        xPos = NEXT_QUEUE_X + x * TILE_SIZE;
                        yPos = ((i - currentBagIndex) * TILE_SIZE * 3) + NEXT_QUEUE_Y + y * TILE_SIZE;
                    }

                    if (piece[y][x].state == BlockState.FILLED_LOCKED) {
                        piece[y][x].setX(xPos);
                        piece[y][x].setY(yPos);
                        piece[y][x].draw(g);
                    }
                }
            }
        }
    }

    private void drawHold(Graphics g) {

        for (int i = 0; i < hold.length; i++) {
            for (int j = 0; j < hold[i].length; j++) {
                int xPos = HOLD_X + j * TILE_SIZE;
                int yPos = HOLD_Y + i * TILE_SIZE;

                if (holdTetrimino.shape == Shape.I) {
                    xPos = 17 + j * TILE_SIZE;
                    yPos = 138 + i * TILE_SIZE;
                } else if (holdTetrimino.shape == Shape.O) {
                    xPos = 47 + j * TILE_SIZE;
                    yPos = 153 + i * TILE_SIZE;
                }

                try {
                    hold[i][j].setX(xPos);
                    hold[i][j].setY(yPos);
                    hold[i][j].draw(g);
                } catch (NullPointerException _) {}
            }
        }
    }

    private Tile[][] convertToTiles(int[][] temp, Shape shape) {
        Tile[][] newTiles = new Tile[temp.length][temp[0].length];
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[0].length; j++) {
                if (temp[i][j] == 1) {
                    newTiles[i][j] = new Tile(BlockState.FILLED_LOCKED, shape);
                } else {
                    newTiles[i][j] = new Tile(BlockState.EMPTY, Shape.EMPTY);
                    newTiles[i][j].sprite = null;
                }
            }
        }
        return newTiles;
    }

    private void insertHoldTetrimino() {
        currentTetrimino.insertPieceIntoBoard(gameBoard);
    }

    private void swapHold() {
        Tetromino temp = currentTetrimino;
        currentTetrimino = new Tetromino(holdTetrimino.shape);
        holdTetrimino = new Tetromino(temp.shape);
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
        URL url = getClass()
                .getClassLoader()
                .getResource(
                        SPRITE_SHEET_FILE_PATH
                                .replace(
                                        "sprite_sheet.png",
                                        "board.png")
                );
        try {
            assert url != null;
            g.drawImage(ImageIO.read(url), 0, 0, this);
        } catch (IOException e) {
            System.out.println("Error trying to read board.png");
        }
        Font font = new Font("Verdana", Font.BOLD, 15);

        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + fps, 10, 10);
//        g.drawString("dasCounter: " + dasCounter, 10, 20);
//        g.drawString("arrCounter: " + arrCounter, 10, 30);
//        g.drawString("Score: " + score, TILE_SIZE*BOARD_WIDTH+2+1 + 20,(TILE_SIZE*BOARD_HEIGHT+2+1)/2 );
        // +2 to draw a border around the board
//        g.drawRect(BOARD_X-1, BOARD_Y-1, TILE_SIZE*BOARD_WIDTH+2+1, TILE_SIZE*BOARD_HEIGHT+2+1);
        if (hold != null) {
            drawHold(g);
        }
        if (nextQueue != null) {
            drawNextQueue(g);
        }
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
            case KeyEvent.VK_SHIFT -> shift = true;
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
            case KeyEvent.VK_SHIFT -> shift = false;
        }


    }
}