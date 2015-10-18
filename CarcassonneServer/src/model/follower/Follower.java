package model.follower;

import java.awt.Point;

public class Follower {
    private Point location;
    private int color;

    public Follower(int color) {
        this.location = new Point(-1, -1);
        this.color = color;
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
}
