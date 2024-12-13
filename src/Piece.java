import java.util.ArrayList;


/*
   change tiles to a 2D array if the games performance is slow
   and convert it into a 2d arrayList when adding it into the board
 */

// TODO implement adding the piece to the board
public class Piece {
    ArrayList<ArrayList<Tile>> tiles;
    int x, y;

    public Piece(Shape shape) {

        // initialize the piece to the selected shape
        // and set the initial position
        switch (shape) {
            case I -> {
                tiles = convertToTiles(initialI, shape);
                x = 3;
                y = 0;
            }
            case J -> {
                tiles = convertToTiles(initialJ, shape);
                x = 3;
                y = 0;
            }
            case L -> {
                tiles = convertToTiles(initialL, shape);
                x = 3;
                y = 0;
            }
            case O -> {
                tiles = convertToTiles(initialO, shape);
                x = 4;
                y = 0;
            }
            case S -> {
                tiles = convertToTiles(initialS, shape);
                x = 3;
                y = 0;
            }
            case T -> {
                tiles = convertToTiles(initialT, shape);
                x = 3;
                y = 0;
            }
            case Z -> {
                tiles = convertToTiles(initialZ, shape);
                x = 3;
                y = 0;
            }
        }

    }

    public void rotateClockwise() {

    }

    public void rotateCounterClockwise() {

    }

    private ArrayList<ArrayList<Tile>> convertToTiles(int[][] shapeArray, Shape shape) {
        ArrayList<ArrayList<Tile>> tiles = new ArrayList<>();
        for (int[] row : shapeArray) {
            ArrayList<Tile> tileRow = new ArrayList<>();
            for (int cell : row) {
                BlockState state = (cell == 1) ? BlockState.FILLED_SELECTED : BlockState.EMPTY_SELECTED;
                tileRow.add(new Tile(state, (cell == 1) ? shape : Shape.EMPTY));
            }
            tiles.add(tileRow);
        }
        return tiles;
    }

    // TODO implement the rest of the piece shapes

    private final int[][] initialI = {
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    };

    private final int[][] initialJ = {
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
    };

    private final int[][] initialL = {
            {0, 0, 1},
            {1, 1, 1},
            {0, 0, 0}
    };

    private final int[][] initialO = {
            {1, 1},
            {1, 1}
    };

    private final int[][] initialS = {
            {0, 1, 1},
            {1, 1, 0},
            {0, 0, 0}
    };

    private final int[][] initialZ = {
            {1, 1, 0},
            {0, 1, 1},
            {0, 0, 0}
    };

    private final int[][] initialT = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 0, 0}
    };


}
