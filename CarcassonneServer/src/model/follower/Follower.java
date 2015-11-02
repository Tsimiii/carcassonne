package model.follower;

import java.awt.Point;

public class Follower {
    private Point location;
    private int color;
    private int contPartInd;

    public Follower(int color) {
        this.location = new Point(-1, -1);
        this.color = color;
        this.setContPartInd(-1);
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public int getColor() {
        return color;
    }

    public int getContPartInd() {
        return contPartInd;
    }

    public void setContPartInd(int contPartInd) {
        this.contPartInd = contPartInd;
    }
}
