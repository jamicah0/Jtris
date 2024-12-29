import java.util.*;

public class WallKicks {
    public static class Offset {
        public final int x, y;

        public Offset(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final Map<String, List<Offset>> WALL_KICK_TABLE = new HashMap<>();
    private static final Map<String, List<Offset>> WALL_KICK_TABLE_I = new HashMap<>();

    static {
        // 0 -> R
        WALL_KICK_TABLE.put("0->1", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(-1, -1),
                new Offset(0, 2),
                new Offset(-1, 2)
        ));
        // R -> 0
        WALL_KICK_TABLE.put("1->0", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(1, 1),
                new Offset(0, -2),
                new Offset(1, -2)
        ));
        // R -> 2
        WALL_KICK_TABLE.put("1->2", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(1, 1),
                new Offset(0, -2),
                new Offset(1, -2)
        ));
        // 2 -> R
        WALL_KICK_TABLE.put("2->1", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(-1, -1),
                new Offset(0, 2),
                new Offset(-1, 2)
        ));
        // 2 -> L
        WALL_KICK_TABLE.put("2->3", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(1, -1),
                new Offset(0, 2),
                new Offset(1, 2)
        ));
        // L -> 2
        WALL_KICK_TABLE.put("3->2", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(-1, 1),
                new Offset(0, -2),
                new Offset(-1, -2)
        ));
        // L -> 0
        WALL_KICK_TABLE.put("3->0", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(-1, 1),
                new Offset(0, -2),
                new Offset(-1, -2)
        ));
        // 0 -> L
        WALL_KICK_TABLE.put("0->3", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(1, -1),
                new Offset(0, 2),
                new Offset(1, 2)
        ));
    }

    static {
        // 0 -> R
        WALL_KICK_TABLE_I.put("0->1", Arrays.asList(
                new Offset(0, 0),
                new Offset(-2, 0),
                new Offset(1, 0),
                new Offset(-2, 1),
                new Offset(1, -2)
        ));
        // R -> 0
        WALL_KICK_TABLE_I.put("1->0", Arrays.asList(
                new Offset(0, 0),
                new Offset(2, 0),
                new Offset(-1, 0),
                new Offset(2, -1),
                new Offset(-1, 2)
        ));
        // R -> 2
        WALL_KICK_TABLE_I.put("1->2", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(2, 0),
                new Offset(-1, -2),
                new Offset(2, 1)
        ));
        // 2 -> R
        WALL_KICK_TABLE_I.put("2->1", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(-2, 0),
                new Offset(1, 2),
                new Offset(-2, -1)
        ));
        // 2 -> L
        WALL_KICK_TABLE_I.put("2->3", Arrays.asList(
                new Offset(0, 0),
                new Offset(2, 0),
                new Offset(-1, 0),
                new Offset(2, -1),
                new Offset(-1, 2)
        ));
        // L -> 2
        WALL_KICK_TABLE_I.put("3->2", Arrays.asList(
                new Offset(0, 0),
                new Offset(-2, 0),
                new Offset(1, 0),
                new Offset(-2, 1),
                new Offset(1, -2)
        ));
        // L -> 0
        WALL_KICK_TABLE_I.put("3->0", Arrays.asList(
                new Offset(0, 0),
                new Offset(1, 0),
                new Offset(-2, 0),
                new Offset(1, 2),
                new Offset(-2, -1)
        ));
        // 0 -> L
        WALL_KICK_TABLE_I.put("0->3", Arrays.asList(
                new Offset(0, 0),
                new Offset(-1, 0),
                new Offset(2, 0),
                new Offset(-1, -2),
                new Offset(2, 1)
        ));
    }



    public static String getRotationKey(int currentState, int nextState) {
        return currentState + "->" + nextState;
    }

    public static List<Offset> getWallKicks(int currentState, int nextState, Shape shape) {
        String key = getRotationKey(currentState, nextState);

        return (shape == Shape.I) ? WALL_KICK_TABLE_I.getOrDefault(key, Collections.emptyList()) : WALL_KICK_TABLE.getOrDefault(key, Collections.emptyList());
    }


}
