import java.awt.*;

public class Kill {
    private final Piece killer;
    private final Piece killed;
    private final Point destination;

    public Kill(Piece killer, Piece killed, int desX, int desY) {
        this.killer = killer;
        this.killed = killed;
        this.destination = new Point(desX, desY);
    }

    public int getX(){
        return destination.x;
    }

    public int getY(){
        return destination.y;
    }

    public Piece getKiller() {
        return killer;
    }

    public Piece getKilled() {
        return killed;
    }
}
