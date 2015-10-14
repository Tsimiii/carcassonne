package model.player;

public class Player {
    int point;
    boolean turn;

    public Player() {
        this.point = 0;
        this.turn = false;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }
}
