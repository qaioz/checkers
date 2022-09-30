import java.awt.*;

public class Move {
    public Piece piece;
    public Piece killed;
    public boolean isKill;
    public Point prevPosition;
    public Point newPosition;
    public boolean p1;
    public boolean prevTakerCanTake;
    public int whites;
    public int blacks;
    public boolean wasQueen;

    public Move(Piece piece, Piece killed, boolean isKill, Point prevPosition, Point newPosition, boolean p1, boolean prevTakerCanTake, int whites, int blacks) {

        this.piece = piece;
        this.killed = killed;
        this.isKill = isKill;
        this.prevPosition = prevPosition;
        this.newPosition = newPosition;
        this.p1 = p1;
        this.prevTakerCanTake = prevTakerCanTake;
        this.whites = whites;
        this.blacks = blacks;
        this.wasQueen = piece.isQueen();
    }

    @Override
    public String toString() {
        String killorMove = isKill ? "kill " : "move ";
        return killorMove + Const.square(prevPosition) + "-" + Const.square(newPosition);
    }
}
