import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
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
    * Play field at least 10 cells wide  (x)
    * Play field at least 22 cells tall  (x)
    * correct tetromino colors          (x)
    * correct tetromino start locations (x)
    * SRS rotation                      (x)
    * standard controls                 (x)
    * 7 bag system                      (x)
    * hold piece                        (x)
    * ghost piece                       (x)
    * soft drop                         (x)
    * leveling with clearing lines      (x)
    * game over on top out

    TO-DO:
    * create a game loop                (x)
    * render board                      (x)
    * piece renderer                    (x)
    * move piece                        (x)
    * collision detection               (x)
    * rotation                          (x)
    * input handling                    (x)
    * hold piece                        (x)
    * see next pieces                   (x)
    * hard drop                         (x)
    * ghost piece                       (x)
    * show incoming piece               (x)
    * clear lines                       (x)
    * (optional) correct lock delay     (x)
    * wall/floor kicks                  (x)
    * scoring: b2b, t-spin, etc.
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

// FIXME: rotating pieces way too fast sometimes makes the piece draw somewhere else
// FIXME: piece phases through locked tiles when gravity is too high


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

    // ghost piece opacity, 24 ... 255
    public static final int GHOST_OPACITY = 80;
    public static final String SPRITE_SHEET_FILE_PATH = "Sprites\\sprite_sheet.png";

    Shape[] sevenBag;
    Shape[] nextSevenBag;
    ArrayList<Shape> nextQueue;

    int score;
    int back2backLevel;
    int previousAmountCleared;
    final int STARTING_LEVEL = 1;
    int level;
    int totalLinesCleared;

    public static int currentBagIndex;
    Tetromino currentTetromino;
    Tetromino holdTetromino;
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
    boolean space = false;

    // gravity
    double gravityCounter;
    double GRAVITY;
    // standard 30
    int lockDelay = 1000;
    // max movements when tetromino hits the floor
    // standard 14
    int maxMovements = 111111;

    int movementCounter;
    int dasCounter;
    int arrCounter;
    double sdfCounter;
    // standard 9
    final int DAS = 7;
    // standard 3
    final int ARR = 1;
    // standard 6, 2000 for instant drop
    final double SDF = 24;
    double softDropSpeed;

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
        dasCounter = 0;
        arrCounter = 0;
        sdfCounter = 0;

        gravityCounter = 0.0;

        totalLinesCleared = 0;
        score = 0;
        back2backLevel = 0;
        previousAmountCleared = 0;
        level = STARTING_LEVEL;

        GRAVITY = calculateGravityPerFrame(level, 60);
        softDropSpeed = SDF * GRAVITY;

        System.out.println(GRAVITY);


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


        holdTetromino = null;
        currentTetromino = new Tetromino(sevenBag[0]);

        // FIXME temp var
        currentTetromino = new Tetromino(Shape.T);

        currentTetromino.insertPieceIntoBoard(gameBoard);
        currentBagIndex++;

        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }




    private void getNextQueue() {
        nextQueue.clear();
        nextQueue.addAll(Arrays.asList(sevenBag));
        nextQueue.addAll(Arrays.asList(nextSevenBag));
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
        URL url = getClass().getClassLoader().getResource("Sprites\\garbage.png");
        BufferedImage img = null;
        try {
            assert url != null;
            img = ImageIO.read(url);
        } catch (IOException ignored) {}
        if (img == null) {
            return;
        }

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int color = img.getRGB(j, i);

                if (color == 0xff000000) {
                    gameBoard[i][j] = new Tile(BlockState.FILLED_LOCKED, Shape.GARBAGE);
                }
            }
        }


    }


    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();



            if (currentTetromino.isLocked) {
                clearFilledLine();
                insertNextTetromino();
                canHold = true;
            }

            checkInput();
            lockDelayAction();

            gravityCounter += GRAVITY;

            if (gravityCounter >= 1 && !down) {
                int cellsToMove = (int) gravityCounter;

                currentTetromino.moveDown(gameBoard, cellsToMove);

                repaint();
                gravityCounter -= cellsToMove;

            }

//            if (gravityCounter == GRAVITY) {
//                gravityCounter = 0;
//                currentTetromino.moveDown(gameBoard);
//                repaint();
//            }
//            gravityCounter++;

            try {
                Thread.sleep(10);
            } catch (InterruptedException _) {}
        }
    }

    // modern tetromino locking
    // the tetromino locks after <lockDelay> frames
    // if any movement is made after the tetromino has hit the floor
    // the lock delay is reset (but only for <maxMovements> times)
    private void lockDelayAction() {
        if (currentTetromino.isAtTheBottom(gameBoard, 1)) {
            currentTetromino.hasHitFloorOnce = true;
            currentTetromino.isCurrentlyOnFloor = true;
        } else {
            currentTetromino.isCurrentlyOnFloor = false;
        }

        if (currentTetromino.isCurrentlyOnFloor) {
            currentTetromino.lockState++;
        } else {
            currentTetromino.lockState = 0;
        }

        if (currentTetromino.lockState > lockDelay) {
            currentTetromino.hardDrop(gameBoard);
            movementCounter = 0;
        }
    }

    private void clearFilledLine() {
        ArrayList<Integer> linesToClear = getLinesToClear();
        if (linesToClear.isEmpty()) {
            return;
        }


        for (int clearIndex : linesToClear) {
            for (Tile tile : gameBoard[clearIndex]) {
                tile.state = BlockState.EMPTY;
                tile.sprite = null;
            }
            moveTilesDown(clearIndex);
        }

        int amountLinesCleared = linesToClear.size();
        totalLinesCleared += amountLinesCleared;

        if (amountLinesCleared == previousAmountCleared) {
            back2backLevel++;
        } else {
            back2backLevel = 0;
        }

        previousAmountCleared = amountLinesCleared;

        switch (amountLinesCleared) {
            case 1 -> score += 100 * level;
            case 2 -> score += 300 * level;
            case 3 -> score += 500 * level;
            case 4 -> score += 800 * level;
        }

        int level = (totalLinesCleared/10) + 1;

        if (level > this.level) {
            this.level = level;
            GRAVITY = calculateGravityPerFrame(level, (int) fps);
            softDropSpeed = SDF * GRAVITY;
            gravityCounter = 0;
        }
    }

    public double calculateGravity(int level) {
        double time = Math.pow(0.8 - ((level - 1) * 0.007), level - 1);
        return 1 / time;  // Gravity in cells per second
    }

    public double calculateGravityPerFrame(int level, int fps) {
        double gravityPerSecond = calculateGravity(level);
        double cellsPerFrame = gravityPerSecond / fps;
        // gravity cap at 20G
        if (cellsPerFrame > 20) {
            cellsPerFrame = 20;
        }
        return cellsPerFrame;
    }

    private void moveTilesDown(int clearedIndex) {
        for (int i = clearedIndex; i > 0; i--) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                gameBoard[i][j].state = gameBoard[i - 1][j].state;
                gameBoard[i][j].sprite = gameBoard[i - 1][j].sprite;
            }
        }
        for (int j = 0; j < BOARD_WIDTH; j++) {
            gameBoard[0][j].state = BlockState.EMPTY;
            gameBoard[0][j].sprite = null;
        }
    }

    // gets every index from the play field which is filled
    private ArrayList<Integer> getLinesToClear() {
        ArrayList<Integer> linesToClear = new ArrayList<>();

        for (int i = 0; i < gameBoard.length; i++) {

            boolean isFilled = true;
            for (int j = 0; j < gameBoard[i].length; j++) {
                if (!(gameBoard[i][j].state == BlockState.FILLED_LOCKED)) {
                    isFilled = false;
                    break;
                }
            }
            if (isFilled) {
                linesToClear.add(i);
            }
        }
        return linesToClear;
    }

    private void insertNextTetromino() {
        currentTetromino = new Tetromino(sevenBag[currentBagIndex]);
        if (!(currentTetromino.insertPieceIntoBoard(gameBoard))) {
            System.out.println("Game Over!");
            isGameRunning = false;
        }

        currentBagIndex++;

        if (currentBagIndex > 6) {
            System.arraycopy(nextSevenBag, 0, sevenBag, 0, 7);
            shuffleBag(nextSevenBag);
            currentBagIndex = 0;
            getNextQueue();
        }
    }


    boolean pressedOnceUp = false;
    boolean pressedOnceCtrl = false;
    boolean pressedOnceLeft = false;
    boolean pressedOnceRight = false;
    boolean pressedOnceShift = false;
    boolean pressedOnceSpace = false;

    boolean canHold = true;

    Direction dasDirection = Direction.NONE;
    private void checkInput() {
        // rotation

        // make sure that the key is only pressed once
        if (up && !pressedOnceUp) {
            resetLockState();
            currentTetromino.rotateClockwise(gameBoard);
            repaint();
            pressedOnceUp = true;
        } else if (!up) {
            pressedOnceUp = false;
        }


        if (ctrl && !pressedOnceCtrl) {
            resetLockState();
            currentTetromino.rotateCounterClockwise(gameBoard);
            repaint();
            pressedOnceCtrl = true;
        } else if (!ctrl) {
            pressedOnceCtrl = false;
        }


        if (right) {
            if (!pressedOnceRight) {
                resetLockState();
                currentTetromino.moveRight(gameBoard);
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
                    if (!currentTetromino.hasHitRightWall(gameBoard)) {
                        resetLockState();
                    }
                    currentTetromino.moveRight(gameBoard);
                    repaint();
                    arrCounter = 0;
                }
            }
        } else if (left) {
            if (!pressedOnceLeft) {
                resetLockState();
                currentTetromino.moveLeft(gameBoard);
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
                    if (!currentTetromino.hasHitLeftWall(gameBoard)) {
                        resetLockState();
                    }
                    currentTetromino.moveLeft(gameBoard);
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
            if (currentTetromino.isCurrentlyOnFloor) {
                currentTetromino.lockState++;
            }


            sdfCounter += softDropSpeed;

            if (sdfCounter >= 1) {
                int cellsToMove = (int) sdfCounter;

                currentTetromino.moveDown(gameBoard, cellsToMove);

                repaint();
                score++;
                sdfCounter -= cellsToMove;
                gravityCounter = 0;
            }

        } else {
            sdfCounter = 0;
        }

        if (shift && !pressedOnceShift && canHold) {
            if (holdTetromino == null) {
                canHold = false;
                currentTetromino.deleteCurrentTetromino(gameBoard);

                copyToDisplayedHold();


                holdTetromino = currentTetromino;
                insertNextTetromino();
            } else {
                canHold = false;
                currentTetromino.deleteCurrentTetromino(gameBoard);

                copyToDisplayedHold();

                swapHold();
                insertHoldTetromino();
            }



            repaint();

            pressedOnceShift = true;
        } else if (!shift) {
            pressedOnceShift = false;
        }

        if (space && !pressedOnceSpace) {
            currentTetromino.hardDrop(gameBoard);
            score += 2;
            pressedOnceSpace = true;
        } else if (!space) {
            pressedOnceSpace = false;
        }

    }

    // resets the lock delay counter to 0
    private void resetLockState() {
        if (currentTetromino.isCurrentlyOnFloor) {
            currentTetromino.lockState = 0;
            movementCounter++;
        } else if (currentTetromino.hasHitFloorOnce) {
            movementCounter++;
        }

        // if more than 15 inputs after the mino has hit
        // the floor were pressed, lock the piece
        // (to prevent infinite movement)
        if (movementCounter > maxMovements) {
            currentTetromino.hardDrop(gameBoard);
            movementCounter = 0;
        }
    }

    private void copyToDisplayedHold() {
        int[][] temporary = RotationSRS.getRotation(currentTetromino.shape, 0);
        assert temporary != null;
        hold = new Tile[temporary.length][temporary[0].length];
        hold = convertToTiles(temporary, currentTetromino.shape);
    }



    private void swapSprite(Tile[][] tetromino, int spriteIndex) {
        for (Tile[] tiles : tetromino) {
            for (int j = 0; j < tetromino[0].length; j++) {
                if (tiles[j].state == BlockState.FILLED_SELECTED || tiles[j].state == BlockState.FILLED_LOCKED) {
                    tiles[j].sprite = sprites[spriteIndex];
                }
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

    private void insertHoldTetromino() {
        currentTetromino.insertPieceIntoBoard(gameBoard);
    }

    private void swapHold() {
        Tetromino temp = currentTetromino;
        currentTetromino = new Tetromino(holdTetromino.shape);
        holdTetromino = new Tetromino(temp.shape);
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

        int x = 10;
        int y = 12;
        g.drawString("FPS: " + fps, x, y);
        g.drawString("Level: " + level, x, y+=20);
        g.drawString("Score: " + score, x, y+=20);
        g.drawString("Lines Cleared: " + totalLinesCleared, x, y+=20);
        g.drawString("B2B: x" + back2backLevel, x, y+=20);

//        g.drawString("Movement Counter: " + movementCounter, x, y+=20);

//        g.drawString("Gravity: " + GRAVITY, x, y+=20);
//        g.drawString("Gravity Counter: " + gravityCounter, x, y+=20);


        if (hold != null) {
            drawHold(g);

        }
        if (nextQueue != null) {
            drawNextQueue(g);
            drawIncomingPiece(g);
        }
        drawGhostPiece(g);
        drawBoard(g);
        if (currentTetromino.isCurrentlyOnFloor) {
            drawLockOverlay(g);
        }


    }


    private void drawNextQueue(Graphics g) {
        int xPos, yPos;
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

                if (holdTetromino.shape == Shape.I) {
                    xPos = 17 + j * TILE_SIZE;
                    yPos = 138 + i * TILE_SIZE;
                } else if (holdTetromino.shape == Shape.O) {
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

    private void drawGhostPiece(Graphics g) {
        int ghostY = currentTetromino.calculateGhostY(gameBoard);
        for (int i = 0; i < currentTetromino.tiles.length; i++) {
            for (int j = 0; j < currentTetromino.tiles[0].length; j++) {
                if (currentTetromino.tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    int xPos = BOARD_X + (currentTetromino.x + j) * TILE_SIZE;
                    int yPos = BOARD_Y + (ghostY + i) * TILE_SIZE;

                    int color = Shape.getColor(currentTetromino.shape) & 0x00FFFFFF | (GHOST_OPACITY << 24);

                    g.setColor(new Color(color, true));
                    g.fillRect(xPos+1, yPos+1, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawIncomingPiece(Graphics g) {
        int[][] nextTetrominoInt = RotationSRS.getRotation(nextQueue.get(currentBagIndex), 0);
        assert nextTetrominoInt != null;
        Tile[][] nextTetromino = convertToTiles(nextTetrominoInt, nextQueue.get(currentBagIndex));
        swapSprite(nextTetromino, Tile.X);

        for (int i = 0; i < nextTetromino.length; i++) {
            for (int j = 0; j < nextTetromino[i].length; j++) {

                int xPos = BOARD_X + ((nextQueue.get(currentBagIndex) == Shape.O) ? Tetromino.INITIAL_X + 1 : Tetromino.INITIAL_X) * TILE_SIZE + j * TILE_SIZE;
                int yPos = BOARD_Y + Tetromino.INITIAL_Y * TILE_SIZE + i * TILE_SIZE;

                try {
                    nextTetromino[i][j].setX(xPos+1);
                    nextTetromino[i][j].setY(yPos+1);
                    nextTetromino[i][j].draw(g);
                } catch (NullPointerException _) {}
            }
        }
    }

    private void drawLockOverlay(Graphics g) {
        for (int row = 0; row < gameBoard.length; row++) {
            for (int column = 0; column < gameBoard[row].length; column++) {
                if (row < 3 && gameBoard[row][column].state == BlockState.EMPTY)  {
                    continue;
                }
                if (gameBoard[row][column].state == BlockState.FILLED_SELECTED) {
                    int xPos = (int) gameBoard[row][column].x;
                    int yPos = (int) gameBoard[row][column].y;

                    Color color = null;
                    try {
                        color = new Color(0, 0, 0, 5*currentTetromino.lockState);
                    } catch (Exception e) {
                        color = new Color(0, 0, 0);
                    }

                    g.setColor(color);
                    g.fillRect(xPos, yPos, TILE_SIZE, TILE_SIZE);
                }
            }
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
    public void keyTyped(KeyEvent e) {}

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
            case KeyEvent.VK_SPACE -> space = true;
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
            case KeyEvent.VK_SPACE -> space = false;
        }


    }
}