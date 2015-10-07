package view;

import javafx.scene.control.Button;

public class GameButton extends Button{
    private int xPos, yPos;     // A gomb x és y koordinátája.
    
    public GameButton(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getxPos() {      // A gomb x koordinátájának lekérdezése.
        return xPos;
    }

    public int getyPos() {      // A gomb y koordinátájának lekérdezése.
        return yPos;
    }

    public void setxPos(int xPos) {     // A gomb x koordinátájának megváltoztatása.
        this.xPos = xPos;
    }

    public void setyPos(int yPos) {     // A gomb y koordinátájának megváltoztatása.
        this.yPos = yPos;
    }
}
