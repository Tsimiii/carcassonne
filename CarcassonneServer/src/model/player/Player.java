package model.player;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import model.follower.Follower;

//A játékos
public class Player {
    private int point; //pontszám
    private int color; //szín/hanyadik helyen következik
    private Follower[] followers = new Follower[7]; //alattvalók

    public Player(int color) {
        this.point = 0;
        this.color = color;
        initFollowers(color);
    }
    
    //az alattvalók példányainak létrehozása
    private void initFollowers(int color) {
        for(int i=0; i<followers.length; i++) {
            followers[i] = new Follower(color);
        }
    }

    public int getPoint() {
        return point;
    }

    //az új pontszám hozzáadása a meglévő ponthoz
    public void addPoint(int point) {
        this.point += point;
    }

    public int getColor() {
        return color;
    }
    
    //az első szabad alattvalónak beállítja a pozícióját, és hogy hanyadik indexű összefüggő részen áll a területkrátyán
    public void setFollowerLocationAndContPartInd(Point p, int ind) {
        for(Follower f : followers) {
            if(f.getLocation().equals(new Point(-1,-1))) {
                f.setLocation(p);
                f.setContPartInd(ind);
                break;
            }
        }
    }
    
    //visszaadja azt az alattvaló példányt, amelyiknek a pozíciója megegyik a paraméterben megadott ponttal
    public Follower getFollowerByLocation(Point p) {
        for(Follower f : followers) {
            if(f.getLocation().equals(p)) {
                return f;
            }
        }
        return null;
    }
    
    //visszaadja a szabad alattvalók számát
    public int getFreeFollowerNumber() {
        int count = 0;
        for(int i=0; i<followers.length; i++) {
            if(followers[i].getLocation().equals(new Point(-1,-1))) {
                count++;
            }
        }
        return count;
    }
    
    //egy adott pozíciójú alattvalót foglaltról szabaddá tesz
    public void setFollowerFree(Point p) {
        for(int i=0; i<followers.length; i++) {
            if(followers[i].getLocation().x == p.x && followers[i].getLocation().y == p.y) {
                followers[i].setLocation(new Point(-1,-1));
                followers[i].setContPartInd(-1);
                break;
            }
        }
    }
    
    //visszaadja az elhelyezett alattvalók listáját
    public List<Follower> getLocatedFollowers() {
        List<Follower> locatedFollowers = new ArrayList<>();
        for(Follower f : followers) {
            if(!(f.getLocation().x == -1 || f.getLocation().y == -1)) {
                locatedFollowers.add(f);
            }
        }
        return locatedFollowers;
    }
}
