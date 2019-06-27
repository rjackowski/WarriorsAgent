package src;

import java.io.Serializable;

public class Treasure implements Serializable {
    private int addHp;
    private int addStrength;

    public Treasure(int hp, int strength) {
        addHp = hp;
        addStrength = strength;
    }

    public Treasure()
    {
        addHp = 50;
        addStrength = 5;
    }


    public int getAddHp() {
        return addHp;
    }

    public int getAddStrength() {
        return addStrength;
    }
}
