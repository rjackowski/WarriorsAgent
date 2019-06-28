package src;

import java.io.Serializable;

public class DecisionPackage implements Serializable {
    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public char getDirection() {
        return direction;
    }

    public void setDirection(char direction) {
        this.direction = direction;
    }

    private char type; // A-atttack , M-move
    private char direction; // L-left R , T, D

    private int target;
    private int strength;

    public DecisionPackage() {}
    public DecisionPackage(char type, int nTarget, int nStrength) {
       this.type = type;
        target = nTarget;
        strength = nStrength;
    }

    public DecisionPackage(char type, char direction) {
        this.type = type;
        this.direction = direction;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }


}
