import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BoardComp extends JPanel implements MouseListener {

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
    protected boolean onlyOneCanTake = false;
    protected List<Piece> whoCanTake = new LinkedList<>();

    public BoardComp(MyFrame myFrame){
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
//        pieces.add(new Piece(1,6, Const.BLACK));
//        pieces.add(new Piece(6,1, Const.BLACK));
////        pieces.add(new Piece(2,5, Const.BLACK));
//        pieces.add(new Piece(3,4, Const.WHITE));
//        pieces.forEach(Piece::promote);
        board = new Piece[8][8];
        for (Piece p : pieces){
//            p.promote();
            int x = p.getX(), y = p.getY();
            board[y][x] = p;
        }
        addMouseListener(this);
    }
    @Override
    protected void paintComponent(Graphics g) {
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

    protected List<Move> getMoves(Piece piece){
        if(piece == null) return null;
        Point prevPos = piece.getPosition();
        List<Move> ans = new LinkedList<>();
        int x = piece.getX(); int y = piece.getY();
        int[][] dir = {{1,1},{-1,1},{1,-1},{-1,-1}};
        if (!piece.isQueen()) {
            if(p1 && isAnEmptySquare(x+1, y-1))
                ans.add(new Move(piece, null, false, prevPos, new Point(x+1, y-1), p1, previousTakerCanTake, whites, blacks));
            if(p1 && isAnEmptySquare(x-1, y-1))
                ans.add(new Move(piece, null, false, prevPos, new Point(x-1, y-1), p1, previousTakerCanTake, whites, blacks));
            if(!p1 && isAnEmptySquare(x+1, y+1))
                ans.add(new Move(piece, null, false, prevPos, new Point(x+1, y+1), p1, previousTakerCanTake, whites, blacks));
            if(!p1 && isAnEmptySquare(x-1, y+1))
                ans.add(new Move(piece, null, false, prevPos, new Point(x-1, y+1), p1, previousTakerCanTake, whites, blacks));
        }
        if(piece.isQueen()){
            for(int[] d : dir){
                x = piece.getX() + d[0]; y = piece.getY() + d[1];
                while (isAnEmptySquare(x, y)){
                    ans.add(new Move(piece, null, false, prevPos, new Point(x, y), p1, previousTakerCanTake, whites, blacks));
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

    protected List<Move> getKills(Piece killer){

        List<Move> ans = new LinkedList<>();
        int x = killer.getX(), y = killer.getY(), color = killer.getColor();
        int[][] dir = {{1,1},{1,-1},{-1,1}, {-1,-1}};
        if (!killer.isQueen()){
            for(int[] d : dir){
                Piece killed = getPiece(x + d[0], y + d[1], 1-color);
                if(killed != null && isAnEmptySquare(x + 2 * d[0], y + 2* d[1]))
                    ans.add(new Move(killer, killed, true, killer.getPosition(), new Point(x + 2 * d[0], y + 2* d[1]), p1, previousTakerCanTake, whites, blacks));
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
                        finalSquares.forEach(p -> ans.add(new Move(killer, taken, true, killer.getPosition(), p, p1, previousTakerCanTake, whites , blacks)));
                    }
                    x+=d[0]; y += d[1];
                }
            }
        }
        return ans;
    }

    private void placePiece(Piece piece, int x, int y){
        int prevX = piece.getX(); int prevY = piece.getY();
        piece.setX(x);
        piece.setY(y);
        board[prevY][prevX] = null;
        board[y][x] = piece;
    }
    private void placePiece(Piece piece, Point where){
        int x = where.x;
        int y = where.y;
        int prevX = piece.getX(); int prevY = piece.getY();
        piece.setX(x);
        piece.setY(y);
        board[prevY][prevX] = null;
        board[y][x] = piece;
    }

    private boolean isAnEmptySquare(int x, int y){
        return x >= 0 && x < 8 && y >= 0 && y < 8 && board[y][x] == null;
    }

    private List<Move> moves;
    private void humanMove(int x, int y) {
        Piece clicked = getPiece(x, y);
        whoCanTake = pieces.stream().filter(p -> p.getColor() == Const.WHITE && canTake(p)).collect(Collectors.toList());
        boolean hasToTake = !whoCanTake.isEmpty();
        System.out.println(hasToTake);
        System.out.println(whoCanTake);
        if (selected == null) {
            System.out.println("Selecting");
            if (hasToTake) {
                if (clicked != null && clicked.getColor() == Const.WHITE && whoCanTake.contains(clicked)) {
                    selected = clicked;
                    moves = getKills(clicked);
                    possibleSquares = moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
                    System.out.println("here");
                    System.out.println("selected " + selected);
                }
            } else {
                //selecting, does not have to take
                if (clicked != null && clicked.getColor() == Const.WHITE) {
                    System.out.println("selected " + clicked);
                    selected = clicked;
                    moves = getMoves(selected);
                    possibleSquares = moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
                    System.out.println("selected " + selected);
                    System.out.println("possible moves " + moves);
                }
            }
        } else {
            //selected is not null, time to move
            System.out.println("You have just clicked " + Const.square(new Point(x, y)));
            if (hasToTake) {
                if (previousTakerCanTake || onlyOneCanTake) {
                    if (clicked == null) {
                        boolean moveWasNotFound = true;
                        for (Move move : moves) {
                            if (move.newPosition.x == x && move.newPosition.y == y) {
                                System.out.println("the move you chose is " + move);
                                previousMove.push(move);
                                take(move);
                                if (previousTakerCanTake) {
                                    System.out.println("its your move again");
                                    whoCanTake = List.of(selected);
                                    moves = getKills(selected);
                                    possibleSquares = moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
                                    System.out.println("Moves are " + moves);
                                }
                                previousSelected.push(selected);
                                moveWasNotFound = false;
                                break;
                            }
                        }
                        if (moveWasNotFound) {
                            System.out.println("You cant go there, please click again");
                        }
                    } else
                        System.out.println("You cant go there, please click again");
                } else {
                    //previous taker cant take, more than one can take
                    if (clicked != null && clicked.getColor() == Const.WHITE) {
                        if (canTake(clicked)) {
                            selected = clicked;
                            moves = getKills(selected);
                            possibleSquares = moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
                            System.out.println("Selected another piece " + selected);
                            System.out.println("Moves are " + moves);
                        } else {
                            System.out.println("You have to choose piece that can take: " + whoCanTake);
                        }
                    } else if (clicked != null && clicked.getColor() == Const.BLACK) {
                        selected = null;
                        moves = null;
                        System.out.println("Unselected, moves set to null");
                        possibleSquares = null;
                    } else if (clicked == null) {
                        boolean moveWasNotFound = true;
                        for (Move m : moves) {
                            if (m.newPosition.x == x && m.newPosition.y == y) {
                                System.out.println("The move you chose is " + m);
                                playMove(m);
                                previousMove.push(m);
                                if (previousTakerCanTake) {
                                    previousSelected.push(selected);
                                    System.out.println("its your move again");
                                    whoCanTake = List.of(selected);
                                    moves = getKills(selected);
                                    possibleSquares = moves.stream().map(mv -> mv.newPosition).collect(Collectors.toList());
                                    System.out.println("Moves are " + moves);
                                } else {
                                    whoCanTake = null;
                                    moves = null;
                                    previousSelected.push(selected);
                                    selected = null;
                                    possibleSquares = null;
                                    System.out.println("Move way played, moves set to null, selecting phase");
                                }
                                moveWasNotFound = false;
                                break;
                            }
                        }
                        if (moveWasNotFound) {
                            System.out.println("Unselected");
                            selected = null;
                            moves = null;
                            possibleSquares = null;
                        }
                    } else {
                        System.out.println("Unexpected control flow");
                    }
                }
            } else {
                //does not have to take
                if (clicked != null && clicked.getColor() == Const.BLACK) {
                    System.out.println("Unselected");
                    selected = null;
                } else if (clicked != null && clicked.getColor() == Const.WHITE) {
                    selected = clicked;
                    System.out.println("Selected " + clicked);
                    moves = getMoves(selected);
                    possibleSquares = moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
                    System.out.println("Moves set to " + moves);
                } else if (clicked == null) {
                    boolean moveWasNotFound = true;
                    for (Move m : moves) {
                        if (m.newPosition.x == x && m.newPosition.y == y) {
                            System.out.println("The move you chose is " + m);
                            playMove(m);
                            previousMove.push(m);
                            System.out.println("played and unselected");
                            previousSelected.push(selected);
                            selected = null;
                            moves = null;
                            possibleSquares = null;
                            moveWasNotFound = false;
                            break;
                        }
                    }
                    if (moveWasNotFound) {
                        System.out.println("Unselected");
                        selected = null;
                        moves = null;
                        possibleSquares = null;
                    }
                } else {
                    System.out.println("Unexpected control flow");
                }
            }
        }
        repaint();
    }

    protected List<Move> generateMoves() {
        List<Move> ans = new LinkedList<>();
        int color = p1 ?  Const.WHITE : Const.BLACK;
        List<Piece> whoCanTake = new LinkedList<>();
        if(previousTakerCanTake){
            whoCanTake.add(selected);
        }
        else
            whoCanTake = pieces.stream().filter(p -> p.getColor() == color && canTake(p)).collect(Collectors.toList());
        boolean hasToTake = !whoCanTake.isEmpty();
        if(hasToTake){
            whoCanTake.forEach(p-> ans.addAll(getKills(p)));
        }else {
            pieces.stream().filter(p->p.getColor() == color).forEach(p->ans.addAll(getMoves(p)));
        }
        return ans;
    }

    private int evaluatePosition(){
        boolean isEndgame = true;
        for(Piece p : pieces){
            if (!p.isQueen) {
                isEndgame = false;
                break;
            }
        }
        if(isEndgame)
            return evaluateEndgame();

        int ans = 0;

        for(Piece p : pieces) {
            int c = p.getColor() == 1 ? 1 : -1;
            if (p.isQueen)
                ans += 300 * c;
            else {
                if (p1) {
                    ans += c*((7 - p.getY()) + 100);
                } else
                    ans +=c*((p.getY()) + 100);
            }

        }
        return ans;
    }
    private void take(Move kill) {
        Piece killed = kill.killed;
        Piece killer = kill.piece;
        int x1 = killer.getX(), x2 = killed.getX(), y1 = killer.getY(), y2 = killed.getY();
        board[y1][x1] = null;
        board[y2][x2] = null;
        pieces.remove(killed);
        killer.setX(kill.newPosition.x);
        killer.setY(kill.newPosition.y);
        board[kill.newPosition.y][kill.newPosition.x] = killer;
        killer.prevKillDirection[0] = x2 - x1 > 0 ? 1 : -1;
        killer.prevKillDirection[1] = y2 - y1 > 0 ? 1 : -1;
        if (killer.getColor() == Const.WHITE && killer.getY() == 0 || killer.getColor() == Const.BLACK && kill.newPosition.y == 7)
            killer.promote();
        int killedColor = killed.getColor();
        if (killedColor == Const.BLACK)
            blacks--;
        else
            whites--;
        previousTakerCanTake = canTake(killer);
        if(!previousTakerCanTake) {
            p1 = !p1;
            killer.prevKillDirection = new int[2];
        }else
            selected = killer;
    }

    private void move(Move move){

        Piece piece = move.piece;
        int prevY = piece.getY();
        int prevX = piece.getX();
        board[prevY][prevX] = null;
        piece.setX(move.newPosition.x);
        piece.setY(move.newPosition.y);
        board[move.newPosition.y][move.newPosition.x] = piece;
        p1 = !p1;
        if (piece.getColor() == Const.WHITE && piece.getY() == 0 || piece.getColor() == Const.BLACK && piece.getY() == 7)
            piece.promote();

    }

    private void playMove(Move move){
        if(move.isKill){
            take(move);
        }else {
            move(move);
        }
    }
    public void unmakeMove(Move move){
        p1 = move.p1;
        previousTakerCanTake = move.prevTakerCanTake;
        Piece piece = move.piece;
        placePiece(piece, move.prevPosition);
        piece.isQueen = move.wasQueen;
        if(move.isKill){
            Piece k = move.killed;
            board[k.getY()][k.getX()] = k;
            pieces.add(k);
            whites = move.p1 ? whites : whites + 1;
            blacks = move.p1 ? blacks + 1 : blacks;
        }else {            //move is not a kill
            whites = move.whites;
            blacks = move.blacks;
        }
    }
    private int minimax(int depth, int alpha, int beta) {
        boolean turn = p1;
        List<Move> moves = generateMoves();
        if (depth == 0)
            return evaluatePosition();
        if (moves.size() == 0)
            return turn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        int bestScore;
        if(turn) {
            bestScore = Integer.MIN_VALUE;
            for (Move move : moves) {
                playMove(move);
                int score;
                score = minimax(depth - 1, alpha, beta);
                bestScore = Math.max(score, bestScore);
                alpha = Math.max(alpha, bestScore);
                unmakeMove(move);
                if(beta <= alpha)
                    break;
            }
            return bestScore;
        }else {
            bestScore = Integer.MAX_VALUE;
            for (Move move : moves) {
                playMove(move);
                int score;
                score = minimax(depth - 1, alpha, beta);
                beta = Math.min(beta, score);
                bestScore = Math.min(score, bestScore);
                beta = Math.min(beta,  bestScore);
                unmakeMove(move);
                if(beta <= alpha)
                    break;
            }
            return bestScore;
        }

    }

    private void computerMove() {

        boolean turn = p1;
        List<Move> moves;

        if (previousTakerCanTake)
            moves = getKills(selected);
        else {
            moves = generateMoves();
        }
        Move bestMove = moves.get(0);
        int bestScore = Integer.MAX_VALUE;
        ArrayList<Move> bestMoves = new ArrayList<>();
        for (Move move : moves) {
            playMove(move);
            int score = minimax(8, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (score < bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            }
            if (score == bestScore) {
                bestMoves.add(move);
            }
            System.out.println("After evaluating " + move + " " + score);
            unmakeMove(move);
        }
        bestMove = bestMoves.get((int) (Math.random() * (bestMoves.size() - 1)));
        playMove(bestMove);
        System.out.println("Played " + bestMove);
        previousMove.push(bestMove);
        if (previousTakerCanTake) {
            selected = bestMove.piece;
            computerMove();
        }
        selected = null;
        if (p1) {
            whoCanTake = pieces.stream().filter(p -> p.getColor() == Const.WHITE && canTake(p)).collect(Collectors.toList());
            onlyOneCanTake = whoCanTake.size() == 1;
            if (onlyOneCanTake) {
                selected = whoCanTake.get(0);
                this.moves = getKills(selected);
                System.out.println("i am a computer i auto selected " + selected + "and set moves to " + this.moves);
                possibleSquares = this.moves.stream().map(m -> m.newPosition).collect(Collectors.toList());
            } else
                possibleSquares = null;
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {



    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = e.getX() / Const.SQUARE_SIZE;
        int y = e.getY() / Const.SQUARE_SIZE;
        humanMove(x, y);
        if(!p1)
            computerMove();
        repaint();
        if(whites == 0)
            myFrame.gameOver(blacks);
        if(blacks == 0)
            myFrame.gameOver(whites);
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

    private int evaluateEndgame(){
        int ans = 0;
        boolean onlyOneInCentre = false;

        for (Piece p: pieces){
            int c = p.getColor() == Const.WHITE ? 1 : -1;
            if(p.getY() == 7- p.getX()){
                if(onlyOneInCentre)
                    ans += 900* c;
                else {
                    if (p.getX() == 2 || p.getX() == 5)
                        ans += 1800 * c;
                    else
                        ans += 1500 * c;
                }
                onlyOneInCentre = true;

            }
            else ans += 1000 * c;
        }
        int winning = ans > 0 ? Const.WHITE : Const.BLACK;
        for (Piece p : pieces){
            if(p.getColor() == winning){
                int c = p.getColor() == Const.WHITE ? 1 : -1;
                if(p.getX() == 1 && p.getY() == 4)
                    ans += 100 * c;
                if(p.getX() == 2 && p.getY() == 5)
                    ans += 100 * c;
                if(p.getX() == 4 && p.getY() == 5)
                    ans += 100 * c;
            }
        }
        return ans;

    }
}