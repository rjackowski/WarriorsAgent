package src;

import java.io.Serializable;

public class AttackPackage implements Serializable {
    private char target;
    private int strength;


    public AttackPackage(Character nTarget, int nStrength) {
        target = nTarget;
        strength = nStrength;
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
