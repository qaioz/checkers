import java.awt.*;

public class Const {
    public static int FRAME_LENGTH = 1382;
    public static Dimension FRAME_SIZE = new Dimension(1382, 744);
    public static int PANEL_SIZE = 688;
    public static int WHITE = 1;
    public static int BLACK = 0;
    public static int SQUARE_SIZE = 688 / 8;
    public static Rectangle PANEL_BOUNDS = new Rectangle((FRAME_LENGTH - PANEL_SIZE)/2, (744 - 688) / 2 - 22, 688, 688);
    public static int PIECE_SIZE = SQUARE_SIZE - 20;
    public static int POS_SQUARE_X = SQUARE_SIZE / 2 - 10;

}
