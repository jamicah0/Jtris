public class Run {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "2");
        new GamePanel(
                GamePanel.TILE_SIZE* GamePanel.BOARD_WIDTH+20,
                GamePanel.TILE_SIZE + GamePanel.BOARD_HEIGHT* GamePanel.TILE_SIZE+20
        );
    }
}