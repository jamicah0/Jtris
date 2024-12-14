import java.util.Random;

// TODO 7 bag randomizer

public enum Shape {
    I,
    J,
    L,
    O,
    S,
    T,
    Z,
    EMPTY;

    private static final Random PRNG = new Random();

    public static Shape randomShape() {
        Shape[] shapes = values();
        return shapes[PRNG.nextInt(shapes.length-1)];
    }
}
