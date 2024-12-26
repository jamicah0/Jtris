public class Run {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");
        new GamePanel(
                2*GamePanel.BOARD_X+2 + GamePanel.TILE_SIZE* GamePanel.BOARD_WIDTH,
                GamePanel.TILE_SIZE + GamePanel.BOARD_HEIGHT* GamePanel.TILE_SIZE+GamePanel.BOARD_Y
        );
    }
}
