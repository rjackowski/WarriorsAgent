package src;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;
import java.awt.*;
import java.io.Console;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.List;

public class WarriorAgent extends Agent {


    private int live;
    private int strength;
   // private

    public int getCoinAmount() {
        return coinAmount;
    }

    private int coinAmount;
    private Color color = Color.RED;
    private boolean existOnMap = false;
    private boolean startFlag = false;
    private AID myMapAgent;
    private RegisterOnMap reg;

    public Color getColor() {
        return color;
    }



    private WarriorAgentGui myGui;
    private WarriorAgentStateGui myStateGui;


    protected void setup() {
        System.out.println("Warrior created");

        myGui = new WarriorAgentGui(this);
        myStateGui = new WarriorAgentStateGui(this);
        myGui.showGui();
        //addBehaviour(new AskForLocation());

        //addBehaviour(new RegisterOnMap());
        reg = new RegisterOnMap();

        addBehaviour(new GetAction());


    }

    public void start(final int nLive, final int nStrength) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                live = nLive;
                strength = nStrength;
                startFlag = true;
            }
        });
    }

    public int getLive() {
        return this.live;
    }


    private class SubstrLive extends OneShotBehaviour {
       private int value;

        SubstrLive(int value) {
           this.value = value;
       }

        public void action() {
            live = live - value;
        }
    }

    private class GetAction extends CyclicBehaviour {
        InformationPackage infoPack;
        public void action() {
            if(startFlag) {
                if (existOnMap == false)
                    myAgent.addBehaviour(reg);
                // Wojownik znajduje się na mapie
                else {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.POSITION);
                    ACLMessage msg = myAgent.receive(mt);
                    if (msg != null) {
                        try {
                            infoPack = (InformationPackage) msg.getContentObject();
                            addBehaviour(new MakeMoveDecision(infoPack));
                            System.out.println("Otrzymano możliwe ruchy");
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    else {
                        block();
                    }

                    myAgent.addBehaviour(new SubstrLive(1));
                    myStateGui.refreshGui();
                }
            }
        }

    }


    private class MakeMoveDecision extends OneShotBehaviour {
        InformationPackage infPack;
        float pointForLeft;
        float pointForRight;
        float pointForDown;
        float pointForTop;
        List<Character> warriorsList;



        MakeMoveDecision(InformationPackage nInfPack) {
            infPack = nInfPack;
            warriorsList = List.of('1','2','3','4','5','6','7','8','9');

        }

        public void action() {
            char decision;
            float maxPoint;


           // temp = findPosition('s',infPack.getBottomVisible());

            //Szukanie skarbu i przeciwnika
            pointForLeft += countProfit(infPack.getLeftVisible());
            pointForRight += countProfit(infPack.getRightVisible());
            pointForDown += countProfit(infPack.getDownVisible());
            pointForTop += countProfit(infPack.getTopVisible());

            decision = 'L';
            maxPoint = pointForLeft;
            if (pointForRight > maxPoint) {
                maxPoint = pointForRight;
                decision = 'R';
            }
            if (pointForDown > maxPoint) {
                maxPoint = pointForDown;
                decision = 'D';
            }
            if (pointForTop > maxPoint) {
                maxPoint = pointForTop;
                decision = 'T';
            }

            sendDecision(decision);

        }

        public void sendDecision(char decision) {
            Character position = ' ';
            switch(decision) {
                case 'L':
                    position = infPack.getLeftVisible().firstElement();
                case 'R':
                    position = infPack.getRightVisible().firstElement();
                case 'D':
                    position = infPack.getDownVisible().firstElement();
                case 'T':
                    position = infPack.getTopVisible().firstElement();
            }

            if(warriorsList.contains(position)){
                sendAttack(position);
            }
            else
                sendMove(decision);


        }

        public void sendAttack(char target){
            DecisionPackage decPack = new DecisionPackage('A',target,strength);
            ACLMessage msg = new ACLMessage(ActionCode.DECISION);
            System.out.println("Wysłano decyzje ataku na " + target);
            msg.addReceiver(myMapAgent);
            try{
            msg.setContentObject(decPack);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }

            msg.setConversationId("attack_send");
            msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
            myAgent.send(msg);
        }

        public void sendMove(char decision){
            DecisionPackage decPack = new DecisionPackage('M',decision);

            ACLMessage msg = new ACLMessage(ActionCode.DECISION);
            System.out.println("Wysłano decyzje ruchu w " + decision);
            msg.addReceiver(myMapAgent);
            try{
                msg.setContentObject(decPack);}
            catch(Exception ex) {ex.printStackTrace();}
            msg.setConversationId("move_send");
            msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
            myAgent.send(msg);
        }


        public int findPosition( char toFind, Vector<Character> vectorToLook) {
            for(int i=0; i< vectorToLook.size(); i++) {
                if (vectorToLook.get(i) == toFind)
                    return i;
            }
            return 0;
        }


        public float countProfit( Vector<Character> vectorToLook) {
            float generalResult, resultPositive = 0, resultNegative = 0, temp; // positive- find a gold, negative - alien warrior

            temp = findPosition('s',vectorToLook);
            if (temp != 0)
                resultPositive =  12 - 2 * temp; //

            for(Character ch: warriorsList) {
                temp = findPosition(ch ,vectorToLook);
                if(temp != 0)
                    resultNegative +=  -12 - 2 * temp;
            }
            generalResult = resultPositive + resultNegative;
            return generalResult;
        }
    }



    private class RegisterOnMap extends Behaviour {
        private int step = 0;
        private MessageTemplate mt; // The template to receive replies

        public void action() {
            switch (step) {
                case 0:
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("map");
                    System.out.println("Look for map");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        for (int i = 0; i < result.length; ++i) {
                            myMapAgent = result[i].getName();
                            System.out.println(myMapAgent.getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    if (myMapAgent != null)
                        step++;

                break;
                // Wysłanie zapytania o rejestracje na mapę
                case 1:
                    ACLMessage cfp = new ACLMessage(ActionCode.REGISTER);
                    cfp.addReceiver(myMapAgent);
                    // cfp.setContent(targetBookTitle);
                    cfp.setConversationId("register");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("register"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step ++;
                    break;
                //Odebranie potwierdzenia i przydzielonego koloru
                case 2:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ActionCode.REGISTER_ACCEPT) {
                            try {
                                color = new Color(Integer.parseInt(reply.getContent()));
                            }catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        else if(reply.getPerformative() == ActionCode.REGISTER_DENY){
                            doDelete();
                        }

                        if (color!= null) {
                            existOnMap = true;
                            myStateGui.setColor(color);
                            myGui.hideGui();
                            myStateGui.showGui();
                            step ++;
                        }
                    }
                    else {
                        block();
                    }
                   // existOnMap = true;
                    break;
            }
        }

        @Override
        public boolean done() {

            if (step == 3) {
                return true;
            }

            else
                return false;
        }
    }



}
