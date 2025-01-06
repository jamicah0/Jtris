import java.util.List;

public class Tetromino {
    private static final int TOTAL_ROTATION_STATES = 4;
    // tetr.io = 0; jstris = 2; tetris.com = 3
    public static final int INITIAL_Y = 0;
    public static final int INITIAL_X = 3;
    Tile[][] tiles;
    int x, y;
    int rotationIndex;
    boolean isLocked;
    boolean hasHitFloorOnce;
    boolean isCurrentlyOnFloor;
    int lockState;
    Shape shape;

    public Tetromino(Shape shape) {
        this.shape = shape;
        rotationIndex = 0;
        isLocked = false;
        hasHitFloorOnce = false;
        isCurrentlyOnFloor = false;
        lockState = 0;
        switch (shape) {
            case I -> {
                tiles = convertToTiles(RotationSRS.IRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
            case J -> {
                tiles = convertToTiles(RotationSRS.JRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
            case L -> {
                tiles = convertToTiles(RotationSRS.LRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
            case O -> {
                int[][] initialO = {
                        {1, 1},
                        {1, 1}
                };
                tiles = convertToTiles(initialO, shape);
                x = INITIAL_X + 1;
                y = INITIAL_Y;
            }
            case S -> {
                tiles = convertToTiles(RotationSRS.SRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
            case T -> {
                tiles = convertToTiles(RotationSRS.TRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
            case Z -> {
                tiles = convertToTiles(RotationSRS.ZRotation[0], shape);
                x = INITIAL_X;
                y = INITIAL_Y;
            }
        }

    }

    // 2d integer array to 2d Tile array
    // 0 -> EMPTY; 1 -> FILLED_SELECTED
    private Tile[][] convertToTiles(int[][] initial, Shape shape) {
        Tile[][] tiles = new Tile[initial.length][initial[0].length];
        for (int i = 0; i < initial.length; i++) {
            for (int j = 0; j < initial[0].length; j++) {
                if (initial[i][j] == 1) {
                    tiles[i][j] = new Tile(BlockState.FILLED_SELECTED, shape);
                } else {
                    tiles[i][j] = new Tile(BlockState.EMPTY, Shape.EMPTY);
                }
            }
        }
        return tiles;
    }

    public boolean insertPieceIntoBoard(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    if (gameBoard[y + i][x + j].state == BlockState.FILLED_LOCKED) {
                        return false;
                    } else {
                        gameBoard[y + i][x + j] = tiles[i][j];
                    }

                }
            }
        }
        return true;
    }

    // set a tile to EMPTY
    public void deleteTileOnBoard(int x, int y, Tile[][] gameBoard) {
        gameBoard[y][x] = new Tile(BlockState.EMPTY, Shape.EMPTY);
    }

    public void lockPiece() {
        for (Tile[] tile : tiles) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tile[j].state == BlockState.FILLED_SELECTED) {
                    tile[j].state = BlockState.FILLED_LOCKED;
                }
            }
        }
        isLocked = true;
    }

    public void hardDrop(Tile[][] gameBoard) {
        int amount = 1;
        while (!isAtTheBottom(gameBoard, amount)) {
            amount++;
        }
        moveDown(gameBoard, amount);
        lockPiece();
    }

    public void moveDown(Tile[][] gameBoard, int amount) {

        int foo = 1;
        while (true) {
            if (isAtTheBottom(gameBoard, foo)) {
                amount = foo-1;
                break;
            }
            if (foo == amount) {
                break;
            }
            foo++;
        }

        deleteCurrentTetromino(gameBoard);

        y += amount;

        insertPieceIntoBoard(gameBoard);
    }

    public boolean isAtTheBottom(Tile[][] gameBoard, int amount) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the bottom of the board
                    // or if there is a locked piece below it
                    if (y + i + amount >= gameBoard.length || gameBoard[y + i + amount][x + j].state == BlockState.FILLED_LOCKED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int calculateGhostY(Tile[][] gameBoard) {
        int ghostY = y;
        while (true) {
            for (int i = 0; i < tiles.length; i++) {
                for (int j = 0; j < tiles[0].length; j++) {
                    if (tiles[i][j].state == BlockState.FILLED_SELECTED || tiles[i][j].state == BlockState.FILLED_LOCKED) {
                        int newY = ghostY + i + 1;
                        // make sure gameBoard[newY][x + j] is not out of bounds

                        try {
                            if (newY >= gameBoard.length || gameBoard[newY][x + j].state == BlockState.FILLED_LOCKED) {
                                return ghostY;
                            }
                        } catch (ArrayIndexOutOfBoundsException ignored) {}
                    }
                }
            }
            ghostY++;
        }
    }

    public void deleteCurrentTetromino(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    deleteTileOnBoard(x + j, y + i, gameBoard);
                }
            }
        }
    }

    public void moveRight(Tile[][] gameBoard) {

        // check if there is a wall
        if (hasHitRightWall(gameBoard)) return;

        // delete the previous piece from the board
        deleteCurrentTetromino(gameBoard);

        x++;

        insertPieceIntoBoard(gameBoard);

    }

    public boolean hasHitRightWall(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the right wall of the board
                    // or if there is a piece next right to it
                    if (x + j + 1 >= gameBoard[0].length || gameBoard[y + i][x + j+1].state == BlockState.FILLED_LOCKED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void moveLeft(Tile[][] gameBoard) {
        // check if there is a wall
        if (hasHitLeftWall(gameBoard)) return;

        // delete the previous piece from the board
        deleteCurrentTetromino(gameBoard);

        x--;

        insertPieceIntoBoard(gameBoard);

    }

    public boolean hasHitLeftWall(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the right wall of the board
                    // or if there is a piece next right to it
                    try {
                        if (x + j - 1 >= gameBoard[0].length || gameBoard[y + i][x + j - 1].state == BlockState.FILLED_LOCKED) {
                            return true;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return true;
                    }

                }
            }
        }
        return false;
    }


    private boolean canRotateOrKick(Tile[][] gameBoard, int[][] nextRotation, boolean clockwise) {
        int nextState = (rotationIndex + (clockwise ? 1 : -1) + TOTAL_ROTATION_STATES) % TOTAL_ROTATION_STATES;
        List<WallKicks.Offset> kicks = WallKicks.getWallKicks(rotationIndex, nextState, shape);

        for (WallKicks.Offset kick : kicks) {
            int testX = x + kick.x;
            int testY = y + kick.y;
            if (canPlacePiece(gameBoard, nextRotation, testX, testY)) {
                deleteCurrentTetromino(gameBoard);
                x = testX;
                y = testY;
                return true;
            }
        }
        return false;
    }

    private boolean canPlacePiece(Tile[][] gameBoard, int[][] piece, int testX, int testY) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[0].length; j++) {
                if (piece[i][j] == 1) {
                    int newX = testX + j;
                    int newY = testY + i;
                    if (isOutOfBounds(gameBoard, newX, newY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isOutOfBounds(Tile[][] gameBoard, int newX, int newY) {
        return newX < 0 || newX >= gameBoard[0].length || newY < 0 || newY >= gameBoard.length || gameBoard[newY][newX].state == BlockState.FILLED_LOCKED;
    }

    public void rotateClockwise(Tile[][] gameBoard) {
        if (shape == Shape.O) {
            return;
        }

        int nextRotationIndex = (rotationIndex + 1) % TOTAL_ROTATION_STATES;
        int[][] nextRotation = RotationSRS.getRotation(shape, nextRotationIndex);

        if (canRotateOrKick(gameBoard, nextRotation, true)) {
            rotationIndex = nextRotationIndex;
            assert nextRotation != null;
            tiles = convertToTiles(nextRotation, shape);
            insertPieceIntoBoard(gameBoard);
            checkTSpin(gameBoard);
        }

    }

    private void checkTSpin(Tile[][] gameBoard) {
        if (shape != Shape.T) {
            return;
        }

        int tilesAround = 0;
        for (int i = 0; i < 3; i+=2) {
            for (int j = 0; j < 3; j+=2) {
                int xPos = x + j;
                int yPos = y + i;

                // TODO

                // should be t spin mini
                if (xPos < 0 || yPos < 0 || xPos > gameBoard[0].length-1 || yPos > gameBoard.length-1) {
                    tilesAround++;
                    continue;
                }

                if (gameBoard[yPos][xPos].state == BlockState.FILLED_LOCKED) {
                    tilesAround++;
                }
            }
        }
        if (tilesAround >= 3) {
            System.out.println("T-SPIN");
        }


    }


    public void rotateCounterClockwise(Tile[][] gameBoard) {
        if (shape == Shape.O) {
            return;
        }

        int nextRotationIndex = (rotationIndex - 1 + TOTAL_ROTATION_STATES) % TOTAL_ROTATION_STATES;
        int[][] nextRotation = RotationSRS.getRotation(shape, nextRotationIndex);

        if (canRotateOrKick(gameBoard, nextRotation, false)) {
            rotationIndex = nextRotationIndex;
            assert nextRotation != null;
            tiles = convertToTiles(nextRotation, shape);
            insertPieceIntoBoard(gameBoard);
            checkTSpin(gameBoard);
        }
    }

}
