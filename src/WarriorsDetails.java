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

    public void clearDecPack()
    {
        this.decPack = null;
    }

    private boolean deadFlag;
    private boolean decisionFlag;
    private DecisionPackage decPack;
    private boolean treasureNoSentFlag;
    private int treasureCollected = 0;

    public void setTreasureNoSentFlag(boolean treasureNoSentFlag) {
        this.treasureNoSentFlag = treasureNoSentFlag;
    }

    public boolean getTreasureNoSentFlag()
    {
        return treasureNoSentFlag;
    }

    public void setTreasureCollected(int treasureCollected)
    {
        this.treasureCollected = treasureCollected;
    }

    public int getTreasureCollected()
    {
        return treasureCollected;
    }


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

public WarriorsDetails() {}
    public WarriorsDetails(AID nAid, Color color) {
        this.aid = nAid;
        this.color = color;
        this.deadFlag = false;
        this.decisionFlag = false;
    }
}
