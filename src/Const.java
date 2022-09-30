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

    public static String square(Point p){
        int x = p.x, y = p.y;
        String ans = "";
        ans += (char)((int)('a')+x);
        ans += 8-y;
        return ans;
    }

    public static void printBoard(Piece[][] board){
        for (int i = 0; i < 8; i++) {
            System.out.println("---------------------------------");
            for (int j = 0; j < 8; j++) {
                int k;
                if(board[i][j] != null && board[i][j].getColor() == Const.WHITE)
                    k = 1;
                else if(board[i][j] != null && board[i][j].getColor() == Const.BLACK)
                    k = 0;
                else
                    k = -1;
                System.out.print("|" + k);
            }
            System.out.println("|");
            System.out.println("---------------------------------");
        }
    }
}
