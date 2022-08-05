public class Piece {
    private int x, y;
    private int color;
    private boolean isQueen;
    public int[] prevKillDirection;

    public Piece(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.isQueen = false;
        prevKillDirection = new int[]{0,0};
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isQueen() {
        return isQueen;
    }

    public void promote(){
        isQueen = true;
    }

    @Override
    public String toString() {
        String ans = "";
        if (isQueen) ans += "Queen ";
        ans += (char)((int)('a')+x);
        ans += 8-y;
        return ans;
    }

}
