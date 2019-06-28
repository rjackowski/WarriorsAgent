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
import jade.lang.acl.UnreadableException;

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
    private char lastDecision;

    public Color getColor() {
        return color;
    }



    private WarriorAgentGui myGui;
    private WarriorAgentStateGui myStateGui;


    protected void setup() {
      
        System.out.println("Warrior created");
        coinAmount = 0;
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
      DecisionPackage decPack;
        ACLMessage msg;
      public SubstrLive(DecisionPackage decPack) {
          this.decPack = decPack;
      }


        public void action() {
            live = live - decPack.getStrength();
            msg = new ACLMessage(ActionCode.LIVESTATE);
            msg.addReceiver(myMapAgent);
            if(live > 0)
                msg.setContent("ALIVE - LIVE: " + live  );
            else
                msg.setContent("DEAD");
            System.out.println(msg.getContent());
            myAgent.send(msg);


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
                    //pobranie możliwych ruchów
                    boolean messageReceived = false;
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.POSITION);
                    ACLMessage msg = myAgent.receive(mt);
                    if (msg != null) {
                        messageReceived = true;
                        try {
                            infoPack = (InformationPackage) msg.getContentObject();
                            addBehaviour(new MakeMoveDecision(infoPack));
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    //receive info about picked treasure
                    mt = MessageTemplate.MatchPerformative(ActionCode.TREASURE_PICKED);
                    msg = myAgent.receive(mt);
                    if(msg != null) {
                        messageReceived = true;
                        try {
                            Treasure treasure = (Treasure) msg.getContentObject();
                            live += treasure.getAddHp();
                            strength += treasure.getAddStrength();
                            coinAmount++;
                            myStateGui.refreshGui();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }

                    //receive atack info
                     mt = MessageTemplate.MatchPerformative(ActionCode.ATTACK);
                     msg = myAgent.receive(mt);
                    if (msg != null) {
                        messageReceived = true;
                        try {

                            DecisionPackage decisionPackage  = (DecisionPackage) msg.getContentObject();
                            addBehaviour(new SubstrLive(decisionPackage));
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if(!messageReceived)
                        block();

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
            if(infPack.getLeftVisible().size() == 1 || lastDecision == 'R')
                pointForLeft = - 10000;
                else
            pointForLeft += countProfit(infPack.getLeftVisible());

            if(infPack.getRightVisible().size() == 1 || lastDecision == 'L')
                pointForRight = - 10000;
                else
            pointForRight += countProfit(infPack.getRightVisible());

            if(infPack.getDownVisible().size() == 1 || lastDecision == 'T')
                pointForDown = - 10000;
            else
            pointForDown += countProfit(infPack.getDownVisible());

            if(infPack.getTopVisible().size() == 1 || lastDecision == 'D')
                pointForTop = - 10000;
            else
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
            else {
                lastDecision = decision;

                sendMove(decision);
            }

        }

        public void sendAttack(char target){
            DecisionPackage decPack = new DecisionPackage('A',target,strength);
            ACLMessage msg = new ACLMessage(ActionCode.DECISION);
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
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        for (int i = 0; i < result.length; ++i) {
                            myMapAgent = result[i].getName();
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
