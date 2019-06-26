package src;

import jade.core.AID;

import java.awt.*;

public class WarriorsDetails {
    private Color color;
    private AID aid;

    public boolean isDeadFlag() {
        return deadFlag;
    }

    public void setDeadFlag(boolean deadFlag) {
        this.deadFlag = deadFlag;
    }

    public boolean isDecisionFlag() {
        return decisionFlag;
    }

    public void setDecisionFlag(boolean decisionFlag) {
        this.decisionFlag = decisionFlag;
    }

    public DecisionPackage getDecPack() {
        return decPack;
    }

    public void setDecPack(DecisionPackage decPack) {
        this.decPack = decPack;
    }

    private boolean deadFlag;
    private boolean decisionFlag;
    private DecisionPackage decPack;







    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public AID getAid() {
        return aid;
    }

    public void setAid(AID aid) {
        this.aid = aid;
        this.color = color;
    }


    public WarriorsDetails(AID nAid, Color color) {
        this.aid = nAid;
        this.color = color;
        this.deadFlag = false;
        this.decisionFlag = false;
    }
}
