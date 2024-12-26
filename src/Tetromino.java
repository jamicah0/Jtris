/*
   change tiles to a 2D array if the games performance is slow
   and convert it into a 2d arrayList when adding it into the board
 */

public class Tetromino {
    Tile[][] tiles;
    int x, y;
    int rotationIndex;
    boolean isLocked;
    Shape shape;

    public Tetromino(Shape shape) {
        this.shape = shape;
        rotationIndex = 0;
        isLocked = false;
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

    public void insertPieceIntoBoard(Tile[][] gameBoard) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    gameBoard[y + i][x + j] = tiles[i][j];
                }
            }
        }
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

    // moves the Piece down, returns if the piece should be locked
    public void moveDown(Tile[][] gameBoard) {
        // check if the piece can move down
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (tiles[i][j].state == BlockState.FILLED_SELECTED) {
                    // check if the piece is at the bottom of the board
                    // or if there is a locked piece below it
                    if (y + i + 1 >= gameBoard.length || gameBoard[y + i + 1][x + j].state == BlockState.FILLED_LOCKED) {
                        lockPiece();
                        return;
                    }
                }
            }
        }




        // delete the previous piece from the board
        deleteCurrentTetrimino(gameBoard);

        // move the piece down
        y++;

        // insert the piece into the board
        insertPieceIntoBoard(gameBoard);
    }

    public void deleteCurrentTetrimino(Tile[][] gameBoard) {
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
        deleteCurrentTetrimino(gameBoard);

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
        deleteCurrentTetrimino(gameBoard);

        x--;

        insertPieceIntoBoard(gameBoard);

    }


    private boolean canRotate(Tile[][] gameBoard, int[][] nextRotation) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                if (nextRotation[i][j] == 1) {
                    int newX = x + j;
                    int newY = y + i;
                    if (isNotOutOfBounds(gameBoard, newX, newY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isNotOutOfBounds(Tile[][] gameBoard, int newX, int newY) {
        return newX < 0 || newX >= gameBoard[0].length || newY < 0 || newY >= gameBoard.length || gameBoard[newY][newX].state == BlockState.FILLED_LOCKED;
    }

    public void rotateClockwise(Tile[][] gameBoard) {
        if (shape == Shape.O) {
            return;
        }




        rotationIndex++;

        if (rotationIndex > 3) {
            rotationIndex = 0;
        }

        int[][] nextRotation = RotationSRS.getRotation(shape, rotationIndex);

        if (!canRotate(gameBoard, nextRotation)) {
            rotationIndex--;
            return;
        }
        
        // delete the previous piece from the board
        deleteCurrentTetrimino(gameBoard);

        assert nextRotation != null;
        tiles = convertToTiles(nextRotation, shape);


        insertPieceIntoBoard(gameBoard);

    }


    public void rotateCounterClockwise(Tile[][] gameBoard) {
        if (shape == Shape.O) {
            return;
        }



        rotationIndex--;

        if (rotationIndex < 0) {
            rotationIndex = 3;
        }

        int[][] nextRotation = RotationSRS.getRotation(shape, rotationIndex);

        if (!canRotate(gameBoard, nextRotation)) {
            rotationIndex++;
            return;
        }


        // delete the previous piece from the board
        deleteCurrentTetrimino(gameBoard);

        assert nextRotation != null;
        tiles = convertToTiles(nextRotation, shape);



        insertPieceIntoBoard(gameBoard);
    }

}
