package model.player;

import model.follower.Follower;

public class Player {
    private int point;
    private int color;
    private boolean turn;
    private Follower[] follower = new Follower[7];

    public Player(int color) {
        this.point = 0;
        this.turn = false;
        this.color = color;
        initFollowers(color);
    }
    
    private void initFollowers(int color) {
        for(int i=0; i<follower.length; i++) {
            follower[i] = new Follower(color);
        }
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

    public int getColor() {
        return color;
    }
}
