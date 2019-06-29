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
import java.util.Random;
import java.util.Vector;
import java.util.List;

public class WarriorAgent extends Agent {


    private int live;
    private int strength;
   // private

    public int getTreasureAmount() {
        return treasureAmount;
    }

    private int coinAmount;
    private Color color = Color.RED;
    private boolean existOnMap = false;
    private boolean startFlag = false;
    private AID myMapAgent;
    private RegisterOnMap reg;
    private char lastDecision;
    private int treasureAmount;

    public Color getColor() {
        return color;
    }



    private WarriorAgentGui myGui;
    private WarriorAgentStateGui myStateGui;


    protected void setup() {
      
        System.out.println("Warrior created");
        treasureAmount = 0;
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
                    receivePossibleMoves();
                    handleReceivedTreasure();
                    handleReceiveAttack();
                    handleEnd();
                    handleTreasureNoRequest();

                    myStateGui.refreshGui();
                }
            }
        }

        private void receivePossibleMoves()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.POSITION);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    infoPack = (InformationPackage) msg.getContentObject();
                    addBehaviour(new MakeMoveDecision(infoPack));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            else
            {
                block();
            }
        }

        private void handleReceivedTreasure()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.TREASURE_PICKED);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null) {
                try {
                    Treasure treasure = (Treasure) msg.getContentObject();
                    live += treasure.getAddHp();
                    strength += treasure.getAddStrength();
                    treasureAmount++;
                    myStateGui.refreshGui();
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                block();
            }

        }

        private void handleReceiveAttack()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.ATTACK);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    DecisionPackage decisionPackage  = (DecisionPackage) msg.getContentObject();
                    addBehaviour(new SubstrLive(decisionPackage));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                block();
            }
        }

        private void handleEnd()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.END);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                doDelete();
            }
            else {
                block();
            }
        }

        private void handleTreasureNoRequest()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.TREASURE_NO_REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = new ACLMessage(ActionCode.TREASURE_NO_INFO);
                reply.addReceiver(myMapAgent);
                try{
                    reply.setContent(String.valueOf(treasureAmount));
                }
                catch(Exception ex) {ex.printStackTrace();}

                myAgent.send(reply);
            }
            else {
                block();
            }
        }

    }

    private class MakeMoveDecision extends OneShotBehaviour {
        InformationPackage infPack;
        float pointForLeft;
        float pointForRight;
        float pointForDown;
        float pointForTop;
        boolean leftBlocked = false;
        boolean rightBlocked = false;
        boolean downBlocked = false;
        boolean topBlocked = false;
        List<Character> decisionList;
        List<Character> warriorsList;



        MakeMoveDecision(InformationPackage nInfPack) {
            infPack = nInfPack;
            warriorsList = List.of('1','2','3','4','5','6','7','8','9');

        }

        public void action() {
            char decision = ' ';
            float maxPoint = -10000000 ;
            decisionList = new Vector<Character>();
           // temp = findPosition('s',infPack.getBottomVisible());
            if (infPack.getLeftVisible().size() == 1) leftBlocked = true;
            if (infPack.getRightVisible().size() == 1) rightBlocked = true;
            if (infPack.getDownVisible().size() == 1) downBlocked = true;
            if (infPack.getTopVisible().size() == 1) topBlocked = true;

            //Szukanie skarbu i przeciwnika
            if  (lastDecision == 'R')
                pointForLeft = - 10000;
            else
            pointForLeft += countProfit(infPack.getLeftVisible());

            if(lastDecision == 'L')
                pointForRight = - 10000;
                else
            pointForRight += countProfit(infPack.getRightVisible());

            if( lastDecision == 'T')
                pointForDown = - 10000;
            else
            pointForDown += countProfit(infPack.getDownVisible());

            if( lastDecision == 'D')
                pointForTop = - 10000;
            else
             pointForTop += countProfit(infPack.getTopVisible());

            if(!leftBlocked) {
            decision = 'L';
            decisionList.add(decision);
            maxPoint = pointForLeft;}

            if(!rightBlocked) {
            if (pointForRight >= maxPoint) {
                if (pointForRight > maxPoint)
                    decisionList.clear();
                maxPoint = pointForRight;
                decision = 'R';
                decisionList.add(decision);
            }
           }
            if(!downBlocked) {
            if (pointForDown >= maxPoint) {
                if (pointForDown > maxPoint)
                    decisionList.clear();
                maxPoint = pointForDown;
                decision = 'D';
                decisionList.add(decision);
            }}
            if(!topBlocked) {
            if (pointForTop > maxPoint) {
                if (pointForTop > maxPoint)
                    decisionList.clear();
                maxPoint = pointForTop;
                decision = 'T';}
                decisionList.add(decision);
            }
            Random generator = new Random();
            int index = generator.nextInt(decisionList.size());

            sendDecision(decisionList.get(index));

        }

        public void sendDecision(char decision) {
            Character position = ' ';
            switch(decision) {
                case 'L':
                    position = infPack.getLeftVisible().firstElement();
                    break;
                case 'R':
                    position = infPack.getRightVisible().firstElement();
                    break;
                case 'D':
                    position = infPack.getDownVisible().firstElement();
                    break;
                case 'T':
                    position = infPack.getTopVisible().firstElement();
                    break;
            }

            if(warriorsList.contains(position)){
                sendAttack(position);
            }
            else {
                lastDecision = decision;
//System.out.println(decision);
                sendMove(decision);
              //  sendMove('R');
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
