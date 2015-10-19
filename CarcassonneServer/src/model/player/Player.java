package model.player;

import java.awt.Point;
import model.follower.Follower;

public class Player {
    private int point;
    private int color;
    private boolean turn;
    private Follower[] followers = new Follower[7];

    public Player(int color) {
        this.point = 0;
        this.turn = false;
        this.color = color;
        initFollowers(color);
    }
    
    private void initFollowers(int color) {
        for(int i=0; i<followers.length; i++) {
            followers[i] = new Follower(color);
        }
    }

    public int getPoint() {
        return point;
    }

    public void addPoint(int point) {
        this.point += point;
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
    
    public void setFollowerLocation(Point p) {
        for(Follower f : followers) {
            if(f.getLocation().equals(new Point(-1,-1))) {
                f.setLocation(p);
                break;
            }
        }
    }
    
    public int getFreeFollowerNumber() {
        for(int i=0; i<followers.length; i++) {
            if(followers[i].getLocation().equals(new Point(-1,-1))) {
                return 7-i;
            }
        }
        return 0;
    }
}
