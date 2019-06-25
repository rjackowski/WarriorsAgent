package src;

import java.io.Serializable;
import java.util.Vector;

public class InformationPackage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Vector<Character> topVisible;

    public Vector<Character> getDownVisible() {
        return downVisible;
    }

    public void setDownVisible(Vector<Character> downVisible) {
        this.downVisible = downVisible;
    }

    private Vector<Character> downVisible;
    private Vector<Character> rightVisible;
    private Vector<Character> leftVisible;

    public Vector<Character> getTopVisible() {
        return topVisible;
    }

    public void setTopVisible(Vector<Character> topVisible) {
        this.topVisible = topVisible;
    }


    public Vector<Character> getRightVisible() {
        return rightVisible;
    }

    public void setRightVisible(Vector<Character> rightVisible) {
        this.rightVisible = rightVisible;
    }

    public Vector<Character> getLeftVisible() {
        return leftVisible;
    }

    public void setLeftVisible(Vector<Character> leftVisible) {
        this.leftVisible = leftVisible;
    }

}

