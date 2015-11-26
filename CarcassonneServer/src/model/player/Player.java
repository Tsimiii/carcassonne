package model.player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
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
    
    public void setFollowerLocationAndContPartInd(Point p, int ind) {
        for(Follower f : followers) {
            if(f.getLocation().equals(new Point(-1,-1))) {
                f.setLocation(p);
                f.setContPartInd(ind);
                break;
            }
        }
    }
    
    public Follower getFollowerByLocation(Point p) {
        for(Follower f : followers) {
            if(f.getLocation().equals(p)) {
                return f;
            }
        }
        return null;
    }
    
    public int getFreeFollowerNumber() {
        int count = 0;
        for(int i=0; i<followers.length; i++) {
            if(followers[i].getLocation().equals(new Point(-1,-1))) {
                count++;
            }
        }
        return count;
    }
    
    public void setFollowerFree(Point p) {
        for(int i=0; i<followers.length; i++) {
            if(followers[i].getLocation().x == p.x && followers[i].getLocation().y == p.y) {
                followers[i].setLocation(new Point(-1,-1));
                followers[i].setContPartInd(-1);
                break;
            }
        }
    }
    
    public List<Follower> getLocatedFollowers() {
        List<Follower> locatedFollowers = new ArrayList<>();
        for(Follower f : followers) {
            if(!(f.getLocation().x == -1 || f.getLocation().y == -1)) {
                System.out.println("playersnél: " + f.getLocation());
                locatedFollowers.add(f);
            }
        }
        return locatedFollowers;
    }
}
