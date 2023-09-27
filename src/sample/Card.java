package sample;

import javafx.scene.image.Image;

import java.util.ArrayList;

public class Card {
    private String[] attributes = new String[3];
    private Image showImage;
    private int row;
    private int col;


    public Card(String a, Image i) {
        for (int k = 0; k < 3; k++) {
            attributes[k] = a.substring(k, k + 1);
        }
        showImage = i;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public Image getCardImage() {
        return showImage;
    }

    public void setRow(int r) {
        row = r;
    }

    public void setColumn(int c) {
        col = c;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }
}

