package main.java;

import java.util.HashMap;
import java.util.Map;

public class NESLeveling {
    public static final Map<Integer, Integer> LEVEL_GRAVITY_TABLE = new HashMap<>();
    static {
        LEVEL_GRAVITY_TABLE.put(0, 48);
        LEVEL_GRAVITY_TABLE.put(1, 43);
        LEVEL_GRAVITY_TABLE.put(2, 38);
        LEVEL_GRAVITY_TABLE.put(3, 33);
        LEVEL_GRAVITY_TABLE.put(4, 28);
        LEVEL_GRAVITY_TABLE.put(5, 23);
        LEVEL_GRAVITY_TABLE.put(6, 18);
        LEVEL_GRAVITY_TABLE.put(7, 13);
        LEVEL_GRAVITY_TABLE.put(8, 8);
        LEVEL_GRAVITY_TABLE.put(9, 6);
        LEVEL_GRAVITY_TABLE.put(10, 5);
        LEVEL_GRAVITY_TABLE.put(11, 5);
        LEVEL_GRAVITY_TABLE.put(12, 4);
        LEVEL_GRAVITY_TABLE.put(13, 4);
        LEVEL_GRAVITY_TABLE.put(14, 3);
        LEVEL_GRAVITY_TABLE.put(15, 3);
        LEVEL_GRAVITY_TABLE.put(16, 2);
        LEVEL_GRAVITY_TABLE.put(17, 2);
        LEVEL_GRAVITY_TABLE.put(18, 2);
        LEVEL_GRAVITY_TABLE.put(19, 2);
        LEVEL_GRAVITY_TABLE.put(20, 2);
        LEVEL_GRAVITY_TABLE.put(21, 2);
        LEVEL_GRAVITY_TABLE.put(22, 2);
        LEVEL_GRAVITY_TABLE.put(23, 2);
        LEVEL_GRAVITY_TABLE.put(24, 2);
        LEVEL_GRAVITY_TABLE.put(25, 2);
        LEVEL_GRAVITY_TABLE.put(26, 2);
        LEVEL_GRAVITY_TABLE.put(27, 2);
        LEVEL_GRAVITY_TABLE.put(28, 2);
        LEVEL_GRAVITY_TABLE.put(29, 1);
    }
}
