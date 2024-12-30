/*
   change tiles to a 2D array if the games performance is slow
   and convert it into a 2d arrayList when adding it into the board
 */

import java.util.List;

public class Tetromino {
    private static final int TOTAL_ROTATION_STATES = 4;
    Tile[][] tiles;
    int x, y;
    int rotationIndex;
    boolean isLocked;
    // if the tetromino has hit the floor at least once
    boolean hasHitFloorOnce;
    // if the tetromino is currently on the floor
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
        // initialize the piece to the selected shape
        // and set the initial position
        switch (shape) {
            case I -> {
                tiles = convertToTiles(RotationSRS.IRotation[0], shape);
                x = 3;
                y = 0;
            }
            case J -> {

                tiles = convertToTiles(RotationSRS.JRotation[0], shape);
                x = 3;
                y = 0;
            }
            case L -> {
                tiles = convertToTiles(RotationSRS.LRotation[0], shape);
                x = 3;
                y = 0;
            }
            case O -> {
                int[][] initialO = {
                        {1, 1},
                        {1, 1}
                };
                tiles = convertToTiles(initialO, shape);
                x = 4;
                y = 0;
            }
            case S -> {
                tiles = convertToTiles(RotationSRS.SRotation[0], shape);
                x = 3;
                y = 0;
            }
            case T -> {
                tiles = convertToTiles(RotationSRS.TRotation[0], shape);
                x = 3;
                y = 0;
            }
            case Z -> {
                tiles = convertToTiles(RotationSRS.ZRotation[0], shape);
                x = 3;
                y = 0;
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
        while (!isAtTheBottom(gameBoard)) {
            moveDown(gameBoard);
        }
        lockPiece();
    }

    // moves the Piece down, returns if the piece should be locked
    public void moveDown(Tile[][] gameBoard) {
        // check if the piece can move down
        if (isAtTheBottom(gameBoard)) {
            return;
        }


        // delete the previous piece from the board
        deleteCurrentTetromino(gameBoard);

        // move the piece down
        y++;

        // insert the piece into the board
        insertPieceIntoBoard(gameBoard);
    }

    public boolean isAtTheBottom(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the bottom of the board
                    // or if there is a locked piece below it
                    if (y + i + 1 >= gameBoard.length || gameBoard[y + i + 1][x + j].state == BlockState.FILLED_LOCKED) {
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
                        if (newY >= gameBoard.length || gameBoard[newY][x + j].state == BlockState.FILLED_LOCKED) {
                            return ghostY;
                        }
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
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the right wall of the board
                    // or if there is a piece next right to it
                    if (x + j + 1 >= gameBoard[0].length || gameBoard[y + i][x + j+1].state == BlockState.FILLED_LOCKED) {
                        return;
                    }
                }
            }
        }

        // delete the previous piece from the board
        deleteCurrentTetromino(gameBoard);

        x++;

        insertPieceIntoBoard(gameBoard);

    }

    public void moveLeft(Tile[][] gameBoard) {
        // check if there is a wall
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the right wall of the board
                    // or if there is a piece next right to it
                    try {
                        if (x + j - 1 >= gameBoard[0].length || gameBoard[y + i][x + j - 1].state == BlockState.FILLED_LOCKED) {
                            return;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return;
                    }

                }
            }
        }

        // delete the previous piece from the board
        deleteCurrentTetromino(gameBoard);

        x--;

        insertPieceIntoBoard(gameBoard);

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
        }
    }

}
