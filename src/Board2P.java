import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Board2P extends JPanel implements MouseListener {

    private MyFrame myFrame;
    private List<Piece> pieces;
    private Piece[][] board;
    protected List<Point> possibleSquares;
    protected Piece selected;
    private boolean p1 = true; //player one turn
    private boolean previousTakerCanTake;
    private int whites = 12;
    private int blacks = 12;
    protected final Stack<Move> previousMove;
    protected Stack<Piece> previousSelected;

    public Board2P(MyFrame myFrame){
        super();
        previousSelected = new Stack<>();
        previousMove = new Stack<>();
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
    @Override
    protected void paintComponent(Graphics g) {
        addMouseListener(this);
        Graphics2D gg = (Graphics2D) g;
        int s = Const.SQUARE_SIZE;
        boolean white = true;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (white) {
                    gg.setColor(Color.gray.brighter().brighter());
                } else {
                    gg.setColor(Color.GREEN.darker());
                }
                gg.fillRect(i * s, j * s, s, s);
                white = !white;
            }
            white = !white;
        }
        paintPieces(gg);
        paintPossibleSquares(gg);
    }

    private void paintPieces(Graphics g) {
        Graphics2D gg = (Graphics2D)(g);
        for (Piece piece : pieces) {
            if (piece.getColor() == Const.BLACK) gg.setColor(Color.black);
            else gg.setColor(Color.RED.darker());
            int x = piece.getX() * Const.SQUARE_SIZE + 10;
            int y = piece.getY() * Const.SQUARE_SIZE + 10;
            gg.fillOval(x, y, Const.PIECE_SIZE, Const.PIECE_SIZE);
            if(piece.isQueen()){
                gg.setStroke(new BasicStroke(10));
                gg.setColor(Color.white);
                gg.drawOval(x, y, Const.PIECE_SIZE, Const.PIECE_SIZE);
                gg.setStroke(new BasicStroke());
            }
        }
    }

    private void paintPossibleSquares(Graphics g) {
        g.setColor(Color.WHITE);
        if (possibleSquares == null) return;
        for (Point p : possibleSquares) {
            int j = p.x; int i = p.y;
            g.fillOval(j * Const.SQUARE_SIZE + Const.POS_SQUARE_X, i * Const.SQUARE_SIZE + Const.POS_SQUARE_X, 20, 20);
        }
    }

    private void move(Piece piece, int x, int y){
        int prevY = piece.getY();int prevX = piece.getX();
        board[prevY][prevX] = null;
        piece.setX(x);
        piece.setY(y);
        board[y][x] = piece;
        possibleSquares = null;
        p1 = !p1;
        if(piece.getColor() == Const.WHITE && piece.getY() == 0 || piece.getColor() == Const.BLACK && piece.getY() == 7)
            piece.promote();
        selected = null;
    }
    private void take(Kill kill) {
        Piece killed = kill.getKilled();
        Piece killer = kill.getKiller();
        int x1 = killer.getX(), x2 = killed.getX(), y1 = killer.getY(), y2 = killed.getY();
        board[y1][x1] = null;
        board[y2][x2] = null;
        pieces.remove(killed);
        killer.setX(kill.getX());
        killer.setY(kill.getY());
        board[kill.getY()][kill.getX()] = killer;
        killer.prevKillDirection[0] = x2 - x1 > 0 ? 1 : -1;
        killer.prevKillDirection[1] = y2 - y1 > 0 ? 1 : -1;
        if (killer.getColor() == Const.WHITE && killer.getY() == 0 || killer.getColor() == Const.BLACK && kill.getY() == 7)
            killer.promote();
        int killedColor = killed.getColor();
        if (killedColor == Const.BLACK)
            blacks--;
        else
            whites--;
    }
    protected List<Point> getMoves(Piece piece){
        if(piece == null) return null;
        List<Point> ans = new LinkedList<>();
        int x = piece.getX(); int y = piece.getY();
        int[][] dir = {{1,1},{-1,1},{1,-1},{-1,-1}};
        if (!piece.isQueen()) {
            if(p1 && isAnEmptySquare(x+1, y-1))
                ans.add(new Point(x+1, y-1));
            if(p1 && isAnEmptySquare(x-1, y-1))
                ans.add(new Point(x-1, y-1));
            if(!p1 && isAnEmptySquare(x+1, y+1))
                ans.add(new Point(x+1, y+1));
            if(!p1 && isAnEmptySquare(x-1, y+1))
                ans.add(new Point(x-1,y+1));
        }
        if(piece.isQueen()){
            for(int[] d : dir){
                x = piece.getX() + d[0]; y = piece.getY() + d[1];
                while (isAnEmptySquare(x, y)){
                    ans.add(new Point(x, y));
                    x += d[0]; y += d[1];
                }
            }
        }
        return ans;
    }
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

    protected List<Kill> getKills(Piece killer){

        List<Kill> ans = new LinkedList<>();
        int x = killer.getX(), y = killer.getY(), color = killer.getColor();
        int[][] dir = {{1,1},{1,-1},{-1,1}, {-1,-1}};
        if (!killer.isQueen()){
            for(int[] d : dir){
                Piece killed = getPiece(x + d[0], y + d[1], 1-color);
                if(killed != null && isAnEmptySquare(x + 2 * d[0], y + 2* d[1]))
                    ans.add(new Kill(killer, killed,x + 2 * d[0], y + 2* d[1]));
            }
        }
        if(killer.isQueen()) {
            for (int[] d : dir) {
                x = killer.getX() + d[0]; y = killer.getY() + d[1];
                while (x > -1 && x < 8 && y > -1 && y < 8){
                    Piece taken = getPiece(x,y);
                    if(taken == null){
                        x+=d[0]; y += d[1];
                        continue;
                    }
                    if(taken.getColor() == color){
                        break;
                    }
                    if(taken.getColor() == 1-color){
                        if (!isAnEmptySquare(x + d[0], y + d[1]))
                            break;
                        int[] realPrevKillDirection = {killer.prevKillDirection[0], killer.prevKillDirection[1]};
                        Point realPosition = new Point(killer.getX(), killer.getY());
                        killer.prevKillDirection = Arrays.copyOf(d, 2);
                        List<Point> compulsorySquares = new LinkedList<>();
                        List<Point> allSquares = new LinkedList<>();
                        x += d[0];y+=d[1];
                        while (isAnEmptySquare(x, y)){
                            placePiece(killer,x,y);
                            if(canTake(killer)){
                                compulsorySquares.add(new Point(x, y));
                            }
                            allSquares.add(new Point(x,y));
                            x+=d[0];y+=d[1];
                        }
                        List<Point> finalSquares = compulsorySquares.isEmpty() ? allSquares : compulsorySquares;
                        killer.prevKillDirection = realPrevKillDirection;
                        placePiece(killer, realPosition.x, realPosition.y);
                        finalSquares.forEach(p -> ans.add(new Kill(killer, taken, p.x, p.y)));
                    }
                    x+=d[0]; y += d[1];
                }
            }
        }
        return ans;
    }

    private void placePiece(Piece piece, int x, int y){
        if(!pieces.contains(piece))
            pieces.add(piece);
        int prevX = piece.getX(); int prevY = piece.getY();
        piece.setX(x);
        piece.setY(y);
        board[prevY][prevX] = null;
        board[y][x] = piece;
    }
    private void placePiece(Piece piece, Point where){
        int x = where.x;
        int y = where.y;
        if(!pieces.contains(piece))
            pieces.add(piece);
        int prevX = piece.getX(); int prevY = piece.getY();
        piece.setX(x);
        piece.setY(y);
        board[prevY][prevX] = null;
        board[y][x] = piece;
    }

    private boolean isAnEmptySquare(int x, int y){
        return x >= 0 && x < 8 && y >= 0 && y < 8 && board[y][x] == null;
    }

    private void humanMove(int x, int y){
        int color = p1? Const.WHITE : Const.BLACK;
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
                        previousMove.push(new Move(kill.getKiller(), kill.getKilled(), true, kill.getInitialSquare(), kill.getDestination(), p1, previousTakerCanTake, whites, blacks));
                        take(kill);
                        if(canTake(kill.getKiller())) {
                            previousTakerCanTake = true;
                            previousSelected.push(selected);
                        }
                        else {
                            previousTakerCanTake = false;
                            p1 = !p1;
                            selected.prevKillDirection = new int[2];
                            previousSelected.push(selected);
                            selected = null;
                        }
                    }
                }else if(clicked != null && clicked.getColor() == 1-color) {
                    selected = null;
                }else if(clicked != null && clicked.getColor() == color && canTake(clicked)){
                    selected = clicked;
                }else if(possibleSquares.stream().anyMatch(p->p.x == x && p.y == y)){
                    Kill kill = kills.stream().filter(k->k.getY() == y && k.getX() == x).findFirst().get();
                    previousMove.push(new Move(kill.getKiller(), kill.getKilled(), true, kill.getInitialSquare(), kill.getDestination(), p1, previousTakerCanTake, whites, blacks));
                    take(kill);
                    if(canTake(kill.getKiller())) {
                        previousTakerCanTake = true;
                        previousSelected.push(selected);
                    }
                    else {
                        selected.prevKillDirection = new int[2];
                        previousTakerCanTake = false;
                        p1 = !p1;
                        previousSelected.push(selected);
                        selected = null;
                    }
                }

            }else {    //does not have to take
                if(clicked != null && clicked.getColor() == color){
                    selected = clicked;
                }else if(possibleSquares.stream().anyMatch(p-> p.x == x && p.y == y)){
                    previousMove.push(new Move(selected, null, false, selected.getPosition(), new Point(x, y), p1, previousTakerCanTake, whites, blacks));
                    previousSelected.push(selected);
                    move(selected, x, y);
                }else {
                    selected = null;
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

    private List<Move> generateMoves(){
        Board2P thisBoard = this;
        List<Move> ans = new LinkedList<>();
        int color = p1 ?  Const.WHITE : Const.BLACK;
        List<Piece> whoCanTake;
        if(previousTakerCanTake)
            whoCanTake = List.of(selected);
        else
            whoCanTake = pieces.stream().filter(p->p.getColor() == color && canTake(p)).toList();
        boolean hasToTake = !whoCanTake.isEmpty();
        if(hasToTake){
            for(Piece killer : whoCanTake ){
                List<Kill> kills = getKills(killer);
                for (Kill kill : kills){
                    ans.add(new Move(killer, kill.getKilled(), true, kill.getInitialSquare(), kill.getDestination(), p1, previousTakerCanTake, whites, blacks));
                }
            }
        }else {
            pieces.stream().filter(p->p.getColor() == color).forEach(new Consumer<Piece>() {
                @Override
                public void accept(Piece piece) {
                    List<Point> moves = getMoves(piece);
                    if(!moves.isEmpty()){
                        for(Point p : moves){
                            ans.add(new Move(piece, null, false, piece.getPosition(), p, p1, previousTakerCanTake, whites, blacks));
                        }
                    }
                }
            });
        }
        return ans;
    }

    private int evaluatePosition(){
        return p1 ? whites : blacks;
    }

    public void setPossibleSquares() {

    }

    private void playMove(Move move){
        Piece piece = move.piece;
        placePiece(piece, move.newPosition);
        if(move.isKill){
            whites = p1 ? whites : whites -1;
            blacks = p1 ? blacks -1 : whites;
            pieces.remove(move.killed);
            previousTakerCanTake = canTake(piece);
            if(!previousTakerCanTake)
                p1 = !p1;
        }else {
            //not a kill
            previousTakerCanTake = false;
            p1 = !p1;
        }
    }
    public void unmakeMove(Move move){
        p1 = move.p1;
        previousTakerCanTake = move.prevTakerCanTake;
        Piece piece = move.piece;
        placePiece(piece, move.prevPosition);
        if(move.isKill){
            placePiece(move.killed, move.killed.getPosition());
            whites = move.p1 ? whites : whites + 1;
            blacks = move.p1 ? blacks + 1 : blacks;
        }else {
            //move is not a kill
            whites = move.whites;
            blacks = move.blacks;
        }
    }

    private void computerMove(){

    }
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / Const.SQUARE_SIZE;
        int y = e.getY() / Const.SQUARE_SIZE;
        humanMove(x, y);
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

    @Override
    public String toString() {
        return "board 2p";
    }
}