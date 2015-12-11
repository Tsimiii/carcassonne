package model.follower;

import java.awt.Point;

// Alattvalót implementáló osztály
public class Follower {
    private Point location; // Az alattvaló helye a táblán
    private int color; // Az alattvaló színe
    private int contPartInd; //Melyik indexű összefüggő részen helyezkedik el

    public Follower(int color) {
        this.location = new Point(-1, -1); // Ha szabad, akkor az elhelyezés negatív
        this.color = color; // A színe megegyezik az őt birtokló játékos színével
        this.setContPartInd(-1); // Ha szabad, akkor az összefüggő rész indexének értéke (melyen áll) negatív
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
