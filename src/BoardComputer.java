import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

public class BoardComputer extends JPanel implements MouseListener {
    private MyFrame myFrame;
    private List<Piece> pieces;
    private Piece[][] board;
    private List<Point> possibleSquares;
    private Piece selected;
    private boolean player = true; //player to move
    private boolean previousTakerCanTake;
    private int whites = 12;
    private int blacks = 12;

    public BoardComputer(MyFrame myFrame){
        super();
        this.myFrame = myFrame;
        setBounds(Const.PANEL_BOUNDS);
        pieces = new LinkedList<>();
        for(int i = 0; i < 8; i++){
            if (i % 2 == 0){
                pieces.add(new Piece(i, 1, Const.BLACK));
                pieces.add(new Piece(i, 5, Const.WHITE));
                pieces.add(new Piece(i, 7, Const.WHITE ));
            }else {
                pieces.add(new Piece(i, 0,  Const.BLACK));
                pieces.add(new Piece(i, 2, Const.BLACK));
                pieces.add(new Piece(i, 6, Const.WHITE));
            }
        }
//        pieces.forEach(Piece::promote);
        board = new Piece[8][8];
        for (Piece p : pieces){
            int x = p.getX(), y = p.getY();
            board[y][x] = p;
        }

    }

    private int search(){
        return 0;
    }

    private int eval(){
        if(player)
            return whites;
        else
            return blacks;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / Const.SQUARE_SIZE;
        int y = e.getY() / Const.SQUARE_SIZE;
        int color = Const.WHITE;
        boolean hasToTake = false;
        Piece clicked = getPiece(x,y);
        boolean onlyOneCanTake = false;
        long count = pieces.stream().filter(p -> p.getColor() == color && canTake(p)).count();
        if(count> 0L){
            hasToTake = true;
            if(count == 1L)
                onlyOneCanTake = true;
        }

        if(selected == null){
            if(hasToTake){
                if(onlyOneCanTake){
                    selected = pieces.stream().filter(p -> p.getColor() == color && canTake(p)).findFirst().get();
                }else
                if(clicked != null && clicked.getColor() == color && canTake(clicked))
                    selected = clicked;
            }else {
                if(clicked != null && clicked.getColor() == color)
                    selected = clicked;
            }
        }
        else //selected is not null
        {

            if(hasToTake){
                List<Kill> kills = getKills(selected);

                if(previousTakerCanTake || onlyOneCanTake){
                    if(possibleSquares.stream().anyMatch(p-> p.x == x && p.y == y)){
                        Kill kill = kills.stream().filter(k->k.getY() == y && k.getX() == x).findFirst().get();
                        take(kill);
                        if(canTake(kill.getKiller()))
                            previousTakerCanTake = true;
                        else {
                            previousTakerCanTake = false;
                            p1 = !p1;
                            selected.prevKillDirection = new int[2];
                            selected = null;
                        }
                    }
                }else if(clicked != null && clicked.getColor() == 1-color) {
                    selected = null;
                }else if(clicked != null && clicked.getColor() == color && canTake(clicked)){
                    selected = clicked;
                }else if(possibleSquares.stream().anyMatch(p->p.x == x && p.y == y)){
                    Kill kill = kills.stream().filter(k->k.getY() == y && k.getX() == x).findFirst().get();
                    take(kill);
                    if(canTake(kill.getKiller()))
                        previousTakerCanTake = true;
                    else {
                        selected.prevKillDirection = new int[2];
                        previousTakerCanTake = false;
                        p1 = !p1;
                        selected = null;
                    }
                }

            }else {    //does not have to take
                if(clicked != null && clicked.getColor() == color){
                    selected = clicked;
                }else if(possibleSquares.stream().anyMatch(p-> p.x == x && p.y == y)){
                    move(selected, x, y);
                }
            }
        }

        if(selected == null)
            possibleSquares = null;
        else {
            if(hasToTake) {
                possibleSquares = getKills(selected).stream().map(k-> new Point(k.getX(), k.getY())).toList();
            }else {
                possibleSquares = getMoves(selected);
            }
        }
        repaint();
        if(blacks == 0)
            myFrame.gameOver(Const.WHITE);
        if(whites == 0)
            myFrame.gameOver(Const.BLACK);
    }

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
    //////helper functions
    private Piece getPiece(int x, int y){
        if (x<0 || x > 7 || y < 0 || y > 7 ) return null;
        return board[y][x];
    }
    private Piece getPiece(int x, int y, int color){
        Piece ans =  getPiece(x, y);
        if (ans != null && ans.getColor() == color)
            return ans;
        return null;
    }
    private boolean canTake(Piece killer){
        int x = killer.getX(), y = killer.getY(), color = killer.getColor();
        int[][] dir = {{1,1},{1,-1},{-1,1}, {-1,-1}};
        if (!killer.isQueen()){
            for(int[] d : dir){
                Piece killed = getPiece(x + d[0], y + d[1], 1-color);
                if(killed != null && isAnEmptySquare(x + 2 * d[0], y + 2* d[1]))
                    return true;
            }
        }
        if(killer.isQueen()){
            for(int[] d : dir){
                if(d[0] == -killer.prevKillDirection[0] && d[1] == -killer.prevKillDirection[1]) {
                    continue;
                }
                x = killer.getX() + d[0]; y = killer.getY() + d[1];
                while (x >= 0 && x < 8 && y >= 0 && y < 8){
                    Piece piece = getPiece(x,y);
                    x += d[0];
                    y += d[1];
                    if (piece == null) continue;
                    if(piece.getColor() == color){ break;}

                    if(isAnEmptySquare(x, y))
                        return true;
                    else
                        break;
                }
            }
        }
        return false;
    }
    private boolean isAnEmptySquare(int x, int y){
        return x >= 0 && x < 8 && y >= 0 && y < 8 && board[y][x] == null;
    }

}
