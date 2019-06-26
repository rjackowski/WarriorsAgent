package src;

import java.io.Serializable;

public class DecisionPackage implements Serializable {
   private char type; // A-atttack , M-move
    private char direction; // L-left R , T, D

    private char target;
    private int strength;

    public DecisionPackage() {}
    public DecisionPackage(char type, Character nTarget, int nStrength) {
       this.type = type;
        target = nTarget;
        strength = nStrength;
    }

    public DecisionPackage(char type, char direction) {
        this.type = type;
        this.direction = direction;
    }

    public char getTarget() {
        return target;
    }

    public void setTarget(char target) {
        this.target = target;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }


}
