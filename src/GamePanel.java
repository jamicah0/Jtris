import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/*
    * * * * *
    * JTris *
    * * * * *

    Tetris programmed in Java

    Gameplay Features:
    * play infinitely
    * srs rotation system
    * score system
    * show next piece
    * soft drop
    * hard drop
    * hold piece
    * ghost piece
    * wall kicks

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
    * t-spins                           (x)
    * combo score
    * correct b2b scores                (x)
    * main menu                         (x)
    * game over                         (x)
    * classic mode
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

// TODO: implement classic mode: correct locking, gravity; start timer; tooltips
// FIXME: recalculate sdf after changing it

public class GamePanel extends JPanel implements Runnable, KeyListener {
    enum Direction {
        LEFT,
        RIGHT,
        NONE
    }
    final static int TILE_SIZE = 30;
    final static int BOARD_X = 150+24;
    final static int BOARD_Y = 34;
    final static int BOARD_WIDTH = 10;
    final static int BOARD_HEIGHT = 23;
    final static int HOLD_X = 32;
    final static int HOLD_Y = 153;
    final static int NEXT_QUEUE_X = 526;
    final static int NEXT_QUEUE_Y = 168;

    // ghost piece opacity, 24 ... 255
    public static final int GHOST_OPACITY = 80;
    public static final String SPRITE_SHEET_FILE_PATH = "Sprites\\sprite_sheet.png";

    Shape[] bag;
    Shape[] nextBag;
    ArrayList<Shape> nextQueue;

    ArrayList<PlayerScore> leaderboard;
    PlayerScore currentPlayer;

    static boolean classicMode;

    int score;
    int back2backLevel;
    final int STARTING_LEVEL = 1;
    int level;
    int totalLinesCleared;
    boolean isB2BEligible;

    public static int currentBagIndex;
    Tetromino currentTetromino;
    Tetromino holdTetromino;
    int AMOUNT_NEXT_PIECES;

    public static BufferedImage[] sprites;
    public static BufferedImage board;
    public static BufferedImage menu;
    public static BufferedImage holdDisabled = null;

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
    boolean esc = false;

    // gravity
    double gravityCounter;
    double GRAVITY;
    double classicGravity = 0;
    // standard 30
    int LOCK_DELAY = 30;
    // max movements when tetromino hits the floor
    // standard 14
    int MAX_MOVEMENTS = 14;

    int movementCounter;
    int dasCounter;
    int arrCounter;
    double sdfCounter;
    // standard 9
    static int DAS = 9;
    // standard 3
    static int ARR = 3;
    // standard 12, 2000 for instant drop
    static double SDF = 12;

    // TODO: saving and loading settings

    final static int STANDARD_DAS = 9;
    final static int STANDARD_ARR = 3;
    final static double STANDARD_SDF = 12;

    double softDropSpeed;

    boolean isGameRunning;
    boolean isPaused;

    long delta = 0;
    long last = 0;
    long fps = 0;


    private final JButton marathonButton;
    private JButton classicButton = null;
    private JButton continueButton = null;
    private JButton standardButton = null;

    private final JTextField DASField;
    private final JTextField ARRField;
    private final JTextField SDFField;

    JFrame frame;

    int textFieldX = 280;
    int textFieldY = 315;
    int textFieldWidth = 70;
    int textFieldHeight = 22;
    int textFieldOffset = 25;

    public GamePanel(int width, int height) {
        leaderboard = PlayerScore.readScores();
        isGameRunning = false;
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.GRAY);
        this.setLayout(null);





        DASField = new JTextField("" + DAS);
        DASField.setHorizontalAlignment(JTextField.RIGHT);
        DASField.setFont(new Font("Verdana", Font.BOLD, 18));
        DASField.setBounds(textFieldX, textFieldY+= textFieldOffset, textFieldWidth, textFieldHeight);
        DASField.setEditable(true);
        DASField.setBackground(Color.WHITE);
        DASField.setVisible(false);
        this.add(DASField);

        ARRField = new JTextField("" + ARR);
        ARRField.setHorizontalAlignment(JTextField.RIGHT);
        ARRField.setFont(new Font("Verdana", Font.BOLD, 18));
        ARRField.setBounds(textFieldX, textFieldY+= textFieldOffset, textFieldWidth, textFieldHeight);
        ARRField.setEditable(true);
        ARRField.setBackground(Color.WHITE);
        ARRField.setVisible(false);
        this.add(ARRField);

        SDFField = new JTextField("" + SDF);
        SDFField.setHorizontalAlignment(JTextField.RIGHT);
        SDFField.setFont(new Font("Verdana", Font.BOLD, 18));
        SDFField.setBounds(textFieldX, textFieldY+= textFieldOffset, textFieldWidth, textFieldHeight);
        SDFField.setEditable(true);
        SDFField.setBackground(Color.WHITE);
        SDFField.setVisible(false);
        this.add(SDFField);





        standardButton = new JButton("Standardwerte");
        standardButton.setFont(new Font("Verdana", Font.BOLD, 18));
        standardButton.setForeground(Color.BLACK);
        standardButton.setBackground(Color.WHITE);
        standardButton.setBounds(225, 415, 200, 24);
        standardButton.setFocusable(false);
        standardButton.addActionListener(e -> {
            DAS = STANDARD_DAS;
            ARR = STANDARD_ARR;
            SDF = STANDARD_SDF;
            DASField.setText("" + DAS);
            ARRField.setText("" + ARR);
            SDFField.setText("" + SDF);
        });
        standardButton.setVisible(false);
        this.add(standardButton);

        continueButton = new JButton("Fortsetzen");
        continueButton.setFont(new Font("Verdana", Font.BOLD, 18));
        continueButton.setForeground(Color.BLACK);
        continueButton.setBackground(Color.WHITE);
        continueButton.setBounds(225, 445, 200, 30);
        continueButton.setFocusable(false);
        continueButton.addActionListener(e -> {
            isPaused = !isPaused;
            setTextFieldsVisibility(isPaused);
            useNewValues();
            frame.requestFocus();
        });
        continueButton.setVisible(false);
        this.add(continueButton);


        marathonButton = new JButton("Marathon-Modus");
        marathonButton.setToolTipText(
                "Marathon-Modus: Spiele so lange wie möglich und erreiche so viele Punkte wie möglich. Moderne Standard-Guidelines werden benutzt.");
        marathonButton.setFont(new Font("Verdana", Font.BOLD, 18));
        marathonButton.setForeground(Color.BLACK);
        marathonButton.setBackground(Color.WHITE);
        marathonButton.setBounds(210, 345, 230, 50);
        marathonButton.setFocusable(false);
        marathonButton.addActionListener(e -> {
            if (!isGameRunning) {
                classicMode = false;
                initialize();
                classicButton.setVisible(false);
                marathonButton.setVisible(false);
            }
        });
        this.add(marathonButton);

        classicButton = new JButton("Classic-Modus");
        classicButton.setToolTipText("Classic-Modus: Marathon-Modus von älteren Tetris-Spielen mit den früheren Guidelines. Kein Hold, komplett zufällige Tetrominos, klassische Gravitation, 1 Next Piece, usw.");
        classicButton.setFont(new Font("Verdana", Font.BOLD, 18));
        classicButton.setForeground(Color.BLACK);
        classicButton.setBackground(Color.WHITE);
        classicButton.setBounds(210, 415, 230, 50);
        classicButton.setFocusable(false);
        classicButton.addActionListener(e -> {
            if (!isGameRunning) {
                classicMode = true;
                initialize();
                marathonButton.setVisible(false);
                classicButton.setVisible(false);
            }
        });
        this.add(classicButton);

        frame = new JFrame("JTris");
        frame.setLocation(
                Toolkit.getDefaultToolkit()
                        .getScreenSize()
                        .width / 2 - width / 2,
                0
        );

        try {
            frame.setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("Sprites\\icon.png"))));
        } catch (IOException e) {
            System.out.println("Error reading icon image");
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(this);
        frame.add(this);
        frame.pack();
        frame.setResizable(false);
        try {
            board = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("Sprites\\board.png")));
            menu = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("Sprites\\menu.png")));
        } catch (IOException e) {
            System.out.println("Error reading board image");
        }
        frame.setVisible(true);
        menuLoop();
    }

    private void useNewValues() {
        try {
            DAS = Integer.parseInt(DASField.getText());
            if (DAS < 0 || DAS == 0) {
                DAS = 1;
            }
            ARR = Integer.parseInt(ARRField.getText());
            if (ARR < 0 || ARR == 0) {
                ARR = 1;
            }
            SDF = Double.parseDouble(SDFField.getText());
            if (SDF < 0 || SDF == 0) {
                SDF = 1;
            }
            softDropSpeed = SDF * GRAVITY;
        } catch (NumberFormatException e) {
            DAS = STANDARD_DAS;
            ARR = STANDARD_ARR;
            SDF = STANDARD_SDF;
        }
        dasCounter = 0;
        arrCounter = 0;
        sdfCounter = 0;
    }

    public void setButtonVisibility(boolean isVisible) {
        marathonButton.setVisible(isVisible);
        classicButton.setVisible(isVisible);
    }

    public void setTextFieldsVisibility(boolean isVisible) {
        DASField.setVisible(isVisible);
        ARRField.setVisible(isVisible);
        SDFField.setVisible(isVisible);
        continueButton.setVisible(isVisible);
        standardButton.setVisible(isVisible);
    }

    private void menuLoop() {
        while (!isGameRunning) {
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized void playSound(final String url) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            Objects.requireNonNull(getClass().getClassLoader().getResource("SFX\\" + url)));
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    private void printCurrentBag() {
        for (Shape shape : bag) {
            System.out.print(shape + " ");
        }
        System.out.println();
        for (Shape shape : nextBag) {
            System.out.print(shape + " ");
        }
        System.out.println();
        System.out.println("-------------------------");
    }

    void printBoard() {
        for (int i = 0; i < gameBoard.length; i++) {
            for (int j = 0; j < gameBoard[0].length; j++) {
                switch (gameBoard[i][j].state) {
                    case EMPTY -> System.out.print("[ ]");
                    case FILLED_LOCKED -> System.out.print("[L]");
                    case FILLED_SELECTED -> System.out.print("[S]");
                }
            }
            System.out.println();
        }
    }

    private void initialize() {
        if (classicMode) {

            AMOUNT_NEXT_PIECES = 1;
            try {
                holdDisabled = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("Sprites\\holdDisabled.png")));
            } catch (IOException e) {
                System.out.println("Error reading holdDisabled image");
            }
            classicGravity = NESLeveling.LEVEL_GRAVITY_TABLE.get(STARTING_LEVEL);
            SDF = classicGravity/8;

        } else {
            AMOUNT_NEXT_PIECES = 5;
        }
        dasCounter = 0;
        arrCounter = 0;
        sdfCounter = 0;

        gravityCounter = 0.0;

        totalLinesCleared = 0;
        score = 0;
        back2backLevel = 0;
        isB2BEligible = false;
        level = STARTING_LEVEL;

        GRAVITY = calculateGravityPerFrame(level, 60);
        softDropSpeed = SDF * GRAVITY;

        loadSprites();




        currentBagIndex = 0;
        nextQueue = new ArrayList<>();
        Shape[] shapeBag = Shape.values();
        bag = new Shape[7];
        nextBag = new Shape[7];

        System.arraycopy(shapeBag, 0, bag, 0, 7);
        System.arraycopy(shapeBag, 0, nextBag, 0, 7);
        shuffleBag(bag);
        shuffleBag(nextBag);

        getNextQueue();


        holdTetromino = null;
        currentTetromino = new Tetromino(bag[0]);

        initializeBoard();

        currentTetromino.insertPieceIntoBoard(gameBoard);
        currentBagIndex++;

        isPaused = false;

        isGameRunning = true;
        last = System.nanoTime();
        Thread t = new Thread(this);
        t.start();
    }

    private void getNextQueue() {
        nextQueue.clear();
        nextQueue.addAll(Arrays.asList(bag));
        nextQueue.addAll(Arrays.asList(nextBag));
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
        if (classicMode) {
            for (int i = 0; i < bag.length; i++) {
                bag[i] = Shape.getRandomShape();
            }
        } else {
            Collections.shuffle(Arrays.asList(bag));
        }

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

    boolean decrementTextOpacity = false;
    int textShownCounter = 0;
    int textShownLength = 180;
    @Override
    public void run() {
        while (isGameRunning) {
            calculateDelta();
            repaint();

            if (isPaused) {
                checkInput();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                continue;
            }



            if (currentTetromino.isLocked) {
                playSound("droplock.wav");
                clearFilledLine();
                insertNextTetromino();
                canHold = true;
            }

            if (wasLineCleared) {
                lineClearTextDuration = 255;
                decrementTextOpacity = true;
                wasLineCleared = false;
            }
            if (decrementTextOpacity) {
                if (textShownCounter == textShownLength) {
                    if (!(lineClearTextDuration <= 0)) {
                        lineClearTextDuration -= 10;
                        if (lineClearTextDuration < 0) {
                            lineClearTextDuration = 0;
                        }
                    } else {
                        textShownCounter = 0;
                        decrementTextOpacity = false;
                    }
                } else {
                    textShownCounter++;
                }
            }

            checkInput();
            lockDelayAction();


            if (!classicMode) {
                gravityCounter += GRAVITY;

                if (gravityCounter >= 1 && !down) {
                    int cellsToMove = (int) gravityCounter;

                    currentTetromino.moveDown(gameBoard, cellsToMove);

                    repaint();
                    gravityCounter -= cellsToMove;
                } else if (down) {
                    gravityCounter = 0;
                }
            } else {
                gravityCounter++;

                if (gravityCounter >= classicGravity && !down) {
                    if (currentTetromino.isCurrentlyOnFloor) {
                        currentTetromino.lockPiece(gameBoard);
                        playSound("droplock.wav");
                    }
                    currentTetromino.moveDown(gameBoard, 1);
                    gravityCounter = 0;
                }

            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        menuLoop();
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

        if (!classicMode && (currentTetromino.lockState > LOCK_DELAY)) {
            currentTetromino.hardDrop(gameBoard);
            movementCounter = 0;
        }

        // if more than 15 inputs after the mino has hit
        // the floor were pressed, lock the piece
        // (to prevent infinite movement)
        if (movementCounter > MAX_MOVEMENTS) {
            currentTetromino.hardDrop(gameBoard);
            movementCounter = 0;
        }
    }

    boolean isCurrentlyClearingLines = false;

    private void clearFilledLine() {
        ArrayList<Integer> linesToClear = getLinesToClear();
        if (linesToClear.isEmpty()) {
            if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN) {
                score += 400 * level;
            } else if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN_MINI) {
                score += 100 * level;
            }
            wasLineCleared = false;
            return;
        }
        if (linesToClear.size() <= 3) {
            playSound("clearnormal.wav");
        } else {
            playSound("cleartetris.wav");
        }
        isCurrentlyClearingLines = true;
        for (int i = 0; i < gameBoard[0].length; i++) {
            for (Integer integer : linesToClear) {
                gameBoard[integer][i].state = BlockState.EMPTY;
                gameBoard[integer][i].sprite = null;
            }
            repaint();
            try {
                Thread.sleep(classicMode ? 25 : 17);
            } catch (InterruptedException e) {
            }

        }

        for (int i = 0; i < linesToClear.size(); i++) {
            moveTilesDown(Collections.max(linesToClear));
        }





//        for (int clearIndex : linesToClear) {
//            for (Tile tile : gameBoard[clearIndex]) {
//                tile.state = BlockState.EMPTY;
//                tile.sprite = null;
//                repaint();
//                try {
//                    Thread.sleep(15);
//                } catch (InterruptedException e) {
//                }
//            }
//            moveTilesDown(clearIndex);
//        }

        int amountLinesCleared = linesToClear.size();
        totalLinesCleared += amountLinesCleared;
        boolean isAnyB2BMove = false;
        int scoreToBeAdded = 0;
        switch (amountLinesCleared) {
            case 1 -> {
                if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN) {
                    scoreToBeAdded = 800 * level;
                    isAnyB2BMove = true;
                    clearType = "T-SPIN SINGLE";
                    break;
                } else if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN_MINI) {
                    scoreToBeAdded = 200 * level;
                    isAnyB2BMove = true;
                    clearType = "Mini T-SPIN single";
                    break;
                }
                scoreToBeAdded = 100 * level;
                clearType = "SINGLE";
            }
            case 2 -> {
                if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN) {
                    scoreToBeAdded = 1200 * level;
                    isAnyB2BMove = true;
                    clearType = "T-SPIN DOUBLE";
                    break;
                } else if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN_MINI) {
                    scoreToBeAdded = 400 * level;
                    isAnyB2BMove = true;
                    clearType = "Mini T-SPIN DOUBLE";
                    break;
                }
                scoreToBeAdded = 300 * level;
                clearType = "DOUBLE";
            }
            case 3 -> {
                if (currentTetromino.spinType == Tetromino.TSPIN.T_SPIN) {
                    scoreToBeAdded = 1600 * level;
                    isAnyB2BMove = true;
                    clearType = "T-SPIN TRIPLE";
                    break;
                }
                scoreToBeAdded = 500 * level;
                clearType = "TRIPLE";
            }
            case 4 -> {
                scoreToBeAdded = 800 * level;
                isAnyB2BMove = true;
                clearType = "TETRIS";
            }
        }
        score += (int) (scoreToBeAdded * (back2backLevel != 0 ? 1.5 : 1));

        if (isAnyB2BMove && isB2BEligible) {
            back2backLevel++;
        } else {
            back2backLevel = 0;
        }
        isB2BEligible = isAnyB2BMove;

        int level = (totalLinesCleared/10) + 1;


        if (!classicMode && (level > this.level)) {
            playSound("levelup.wav");
            this.level = level;
            GRAVITY = calculateGravityPerFrame(level, (int) fps);
            softDropSpeed = SDF * GRAVITY;
            gravityCounter = 0;
        } else if (classicMode) {
            this.level = level;
            if (level > 29) {
                level = 29;
            }
            SDF = classicGravity/4;
            classicGravity = NESLeveling.LEVEL_GRAVITY_TABLE.get(level);
            gravityCounter = 0;
        }
        wasLineCleared = true;
        isCurrentlyClearingLines = false;
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
        // gravity cap at 1G when classic mode is enabled
        if (classicMode) {
            if (cellsPerFrame > 1) {
                cellsPerFrame = 1;
            }
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
        currentTetromino = new Tetromino(bag[currentBagIndex]);
        if (!(currentTetromino.insertPieceIntoBoard(gameBoard))) {
            playSound("gameover.wav");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            gameOver();
        }

        currentBagIndex++;

        if (currentBagIndex > 6) {
            System.arraycopy(nextBag, 0, bag, 0, 7);
            shuffleBag(nextBag);
            currentBagIndex = 0;
            getNextQueue();
        }
    }

    private void gameOver() {
        fillBoardGameOver();
        String name = JOptionPane.showInputDialog(null, "Gib deine Initialen ein (3 Buchstaben):", "Game Over!", JOptionPane.INFORMATION_MESSAGE);
        if (name == null) {
            name = "";
        }
        if (name.length() >= 3) {
            name = name.substring(0, 3).toUpperCase();
        } else {
            name = name.toUpperCase();
        }


        leaderboard.add(new PlayerScore(name, score));
        leaderboard.sort((o1, o2) -> o2.score - o1.score);
        PlayerScore.saveScore(leaderboard);
        leaderboard = PlayerScore.readScores();
        isGameRunning = false;
        hold = null;
        setButtonVisibility(true);
    }

    private void fillBoardGameOver() {
        // bottom to top
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                gameBoard[i][j].state = BlockState.FILLED_LOCKED;
                Random random = new Random();
                gameBoard[i][j].sprite = sprites[random.nextInt(7)];
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            repaint();
        }
    }


    boolean pressedOnceUp = false;
    boolean pressedOnceCtrl = false;
    boolean pressedOnceLeft = false;
    boolean pressedOnceRight = false;
    boolean pressedOnceShift = false;
    boolean pressedOnceSpace = false;
    boolean pressedOnceEsc = false;

    boolean canHold = true;

    Direction dasDirection = Direction.NONE;
    private void checkInput() {

        if (esc && !pressedOnceEsc) {
            isPaused = !isPaused;
            setTextFieldsVisibility(isPaused);
            pressedOnceEsc = true;
        } else if (!esc) {
            pressedOnceEsc = false;
        }

        if (isPaused) {
            return;
        }


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
                playSound("move.wav");
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
                        playSound("move.wav");
                        resetLockState();
                    }
                    currentTetromino.moveRight(gameBoard);
                    repaint();
                    arrCounter = 0;
                }
            }
        } else if (left) {
            if (!pressedOnceLeft) {
                playSound("move.wav");
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
                        playSound("move.wav");
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
            if (!classicMode) {
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
            }
            else {
                sdfCounter++;

                if (sdfCounter > SDF) {
                    if (currentTetromino.isCurrentlyOnFloor) {
                        currentTetromino.lockPiece(gameBoard);
                        playSound("droplock.wav");
                    }
                    currentTetromino.moveDown(gameBoard, 1);
                    sdfCounter = 0;
                }
            }

        } else {
            sdfCounter = 0;
        }

        if (!classicMode && shift && !pressedOnceShift && canHold) {
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

        if (!classicMode && space && !pressedOnceSpace) {
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
        if (gameBoard == null) {
            return;
        }
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

    String clearType;


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        g2d.drawImage(board, 0, 0, this);
        if (!isGameRunning) {
            g2d.drawImage(menu, 0, 0, this);
        }


        Font font = new Font("Verdana", Font.BOLD, 10);

        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        int x = 21;
        int y = 270;
        // g2d.drawString("FPS: " + fps, 2, 11);
        font = new Font("Verdana", Font.BOLD, 20);
        g2d.setFont(font);
        g2d.drawString("LEVEL " + level, x, y+=20);

        g2d.drawString("SCORE ", x, y+=40);
        g2d.drawString(String.format("%08d" , score), x, y+=20);

        g2d.drawString("LINES ", x, y+=40);
        g2d.drawString(String.format("%08d" , totalLinesCleared), x, y+=20);

        if (back2backLevel >= 1) {
            g2d.drawString("B2B x" + back2backLevel, 527, 670);
        }



        if (!isGameRunning) {
            x = BOARD_X + 57;
            y = BOARD_Y+117;
            font = new Font("Verdana", Font.BOLD, 25);
            g.setFont(font);
            g2d.drawString("BESTENLISTE", x, y);

            x = BOARD_X + 12;
            y += 32;
            leaderboard.sort((o1, o2) -> o2.score - o1.score);
            for (int i = 0; i < 5; i++) {
                String name = i >= leaderboard.size() ? "---" : leaderboard.get(i).name;

                g2d.drawString((i+1) + ".  " + name, x, y);
                g2d.drawString(i >= leaderboard.size() ? "00000000" : leaderboard.get(i).getScoreAsString(), x + 135, y);
                y += 32;
            }

            return;
        }

        if (classicMode) {
            g2d.drawImage(holdDisabled, 0, 0, this);
        }

        if (clearType != null) {

            g2d.setColor(new Color(255, 255, 255, lineClearTextDuration));
            if (clearType.contains(" ")) {
                g2d.setColor(new Color(243, 163, 255, lineClearTextDuration));
                g2d.drawString(clearType.substring(0, 6), 510, 700);
                g2d.drawString(clearType.substring(7), 510, 720);
            } else {
                g2d.drawString(clearType, 527, 700);
            }

        }

        if (hold != null) {
            drawHold(g2d);
        }

        if (nextQueue != null) {
            drawNextQueue(g2d);
            if (!classicMode) drawIncomingPiece(g2d);
        }


        if (!isCurrentlyClearingLines && !classicMode) {
            drawGhostPiece(g2d);
        }
        drawBoard(g2d);
        if (currentTetromino.isCurrentlyOnFloor && !classicMode) {
            drawLockOverlay(g2d);
        }
        if (isPaused) {
            x = BOARD_X + 78;
            y = BOARD_Y+117;
            font = new Font("Verdana", Font.BOLD, 25);
            g2d.setColor(Color.WHITE);
            g2d.setFont(font);
            g2d.drawImage(menu, 0, 0, this);
            g2d.drawString("PAUSIERT", x, y);
            font = new Font("Verdana", Font.BOLD, 17);
            g2d.setFont(font);
            // DAS, same height as the other text fields
            textFieldY = 357;
            g2d.drawString("DAS: ", textFieldX - 56, textFieldY);
            g2d.drawString("Frames", textFieldX + textFieldWidth + 3, textFieldY);
            g2d.drawString("ARR: ", textFieldX - 56, textFieldY + textFieldOffset);
            g2d.drawString("Frames", textFieldX + textFieldWidth + 3, textFieldY + textFieldOffset);
            g2d.drawString("SDF: ", textFieldX - 56, textFieldY + textFieldOffset * 2);
            g2d.drawString(classicMode ? "Frames" : "x", textFieldX + textFieldWidth + 3, textFieldY + textFieldOffset * 2);

        }

    }
    int lineClearTextDuration = 255;
    boolean wasLineCleared = false;


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
                    xPos = 19 + j * TILE_SIZE;
                    yPos = 138 + i * TILE_SIZE;
                } else if (holdTetromino.shape == Shape.O) {
                    xPos = 49 + j * TILE_SIZE;
                    yPos = 153 + i * TILE_SIZE;
                }

                try {
                    hold[i][j].setX(xPos);
                    hold[i][j].setY(yPos);
                    hold[i][j].draw(g);
                } catch (NullPointerException ignored) {}
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
                } catch (NullPointerException ignored) {}
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
            case KeyEvent.VK_ESCAPE -> esc = true;

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
            case KeyEvent.VK_ESCAPE -> esc = false;
        }


    }
}