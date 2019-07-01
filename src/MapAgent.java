package src;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class MapAgent extends Agent {

    private static final String DRAW_TEXT = "No winner! All warriors are dead!";
    private static final String WINNER_TEXT = "The winer is ";

    private MapPrepGui prepGui;
    private MapGui mapGui;

    private static int MAX_WARRIORS = 8;
    //public static List<Color> warriorColors;

    private List<Color> avilibleColors;


    //  private List<AID> registeredWarriors;
    private List<WarriorsDetails> registeredWarriors;

    private boolean startFlag = false;
    private boolean waitForAllWarriors = false;

    private GameSteps gameStep = GameSteps.SEND_MOVES;
    private int warriorsMoved = 0;
    private MapField map;

    private ManageAction gameBehaviour;

    public List<WarriorsDetails> getRegisteredWarriors() {
        return registeredWarriors;
    }

    protected void setup() {
       // System.out.println("Map created");

        SetupColors();
        prepGui = new MapPrepGui(this);
        prepGui.showGui();

        registeredWarriors = new Vector<WarriorsDetails>();
        //   warriorColors = new Vector<Color>();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("map");
        sd.setName("JADE-map");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

      //  System.out.println("Map registred");
        addBehaviour(new GetRegistration());
        gameBehaviour = new ManageAction();
        addBehaviour(gameBehaviour);
        addBehaviour( new ReceiveDead() );
    }

    private void SetupColors() {
        avilibleColors = List.of(Color.BLACK, Color.BLUE, Color.ORANGE,
                Color.WHITE, Color.RED, Color.GREEN,
                Color.GRAY, Color.CYAN);
    }

    public void onStartClick(int treasuresToSpawn) {
        map = new MapField(registeredWarriors.size(), treasuresToSpawn);
        prepGui.hideGui();
        mapGui = new MapGui(this, map);
        mapGui.showGui();
        mapGui.updateMap();
        startFlag = true;
    }

    private class ManageAction extends CyclicBehaviour {

        public void action() {
            if (startFlag) {
                switch (gameStep) {

                    case SEND_MOVES:
                        handleSendMoves();
                    break;

                    case RECEIVE_MOVES:
                        handleReceiveMoves();
                        break;

                    case MAKE_MOVES:
                        handleMakeMoves();
                        break;

//                    case RECEIVE_DEAD:
//                    //    handleReceiveDead();
//                        break;

//                    case ONE_WARRIOR_LEFT:
//                       // handleWinner();
//                        break;

//                    case NO_WARRIOR_LEFT:
//                        handleDraw();
//                        break;

//                    case TREASURE_NO_REQUEST:
//                        handleTreasureNumerRequest();
//                        break;
//
//                    case RECEIVE_TREASURE_NO:
                     //   handleReceiveTreasure();
                }
            }
        }

        private void handleSendMoves()
        {
            for (WarriorsDetails warrior : registeredWarriors) {
                if (!warrior.isDeadFlag()){
                ACLMessage msg = new ACLMessage(ActionCode.POSITION);
                InformationPackage infPack = new InformationPackage();
                Vector<Character> visible = new Vector<Character>();
                int index = registeredWarriors.indexOf(warrior);
                infPack = map.getVisibleFields(index);
                msg.addReceiver(warrior.getAid());

                try {
                    msg.setContentObject(infPack);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                msg.setConversationId("move_send");
                msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                myAgent.send(msg);}
                //System.out.println("Wysłano mozliwe ruchy ");
            }
            gameStep = GameSteps.RECEIVE_MOVES;
        }

        private void handleReceiveMoves()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.DECISION);
            ACLMessage msg = myAgent.receive(mt);
            DecisionPackage decPack = new DecisionPackage();
            if (msg != null) {
                AID senderAID = msg.getSender();
                try {
                    decPack = (DecisionPackage) msg.getContentObject();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                int index = getListIndexByAID(senderAID);
                if (index != -1 && !registeredWarriors.get(index).isDecisionFlag()) {
                    registeredWarriors.get(index).setDecisionFlag(true);
                    registeredWarriors.get(index).setDecPack(decPack);
                    warriorsMoved++;
                }
            }
            int warriorsNr = 0;
            for (WarriorsDetails warrior : registeredWarriors) {
                if(!warrior.isDeadFlag())
                    warriorsNr++;
            }


            if (warriorsMoved >= warriorsNr) {
                warriorsMoved = 0;
                gameStep=GameSteps.MAKE_MOVES;
            }
        }

        private void handleMakeMoves()
        {

            Vector<WarriorsDetails> warriorsCollectedTreasure = new Vector<WarriorsDetails>();
            for (int i = 0; i < registeredWarriors.size(); i++) {
                DecisionPackage decPackage = registeredWarriors.get(i).getDecPack();
                if (decPackage.getType() == 'M') {
                    if(map.changeWariorLocation(i, decPackage.getDirection()))
                    warriorsCollectedTreasure.add(registeredWarriors.get(i));
                }
                if (decPackage.getType() == 'A') {
                ACLMessage msgAttack = new ACLMessage(ActionCode.ATTACK);
                msgAttack.addReceiver(registeredWarriors.get(i).getAid());
                DecisionPackage attackPack = new DecisionPackage('A', i, decPackage.getStrength());
                try {
                    msgAttack.setContentObject(attackPack);
                    myAgent.send(msgAttack);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                }
            }


            for(WarriorsDetails warrior : warriorsCollectedTreasure) {
                ACLMessage m = new ACLMessage(ActionCode.TREASURE_PICKED);
                Treasure treasure = new Treasure();
                Vector<Character> visible = new Vector<Character>();
                int index = registeredWarriors.indexOf(warrior);
                m.addReceiver(warrior.getAid());

                try {
                    m.setContentObject(treasure);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                m.setConversationId("treasure_picked");
                myAgent.send(m);
                System.out.println("Wyslano zebranie skarbu");
            }

            mapGui.updateMap();
            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            resetWarriorsFlag();
            gameStep = GameSteps.SEND_MOVES;

        }


        private void handleTreasureNumerRequest()
        {
            for (WarriorsDetails warrior : registeredWarriors) {
                ACLMessage msg = new ACLMessage(ActionCode.TREASURE_NO_REQUEST);
                msg.addReceiver(warrior.getAid());
                myAgent.send(msg);
            }
            gameStep = GameSteps.RECEIVE_TREASURE_NO;
        }



        private void resetWarriorsFlag() {
            for (WarriorsDetails warrior : registeredWarriors)
                warrior.setDecisionFlag(false);
        }

    }

    public int getListIndexByAID(AID aid) {
        for (int i = 0; i < getRegisteredWarriors().size(); i++) {
            if (getRegisteredWarriors().get(i).getAid().equals(aid)) {
                return i;
            }
        }
        return -1;
    }


    private class ReceiveDead extends CyclicBehaviour{
        public void action() {
            if (startFlag) {
                handleReceiveDead();
            }
        }

        private void handleReceiveTreasure() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.TREASURE_NO_INFO);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID senderAID = msg.getSender();
                int index = getListIndexByAID(senderAID);
                if (index != -1 && !registeredWarriors.get(index).isDecisionFlag()) {
                    try {
                        int treasures = Integer.valueOf(msg.getContent());
                        registeredWarriors.get(index).setTreasureCollected(treasures);
                        registeredWarriors.get(index).setTreasureNoSentFlag(true);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }

            if (registeredWarriors.size() == treasureInfoReceived()) {
                String bestWarrior = getBestWarrior().getAid().toString();
                showWinner(bestWarrior);
            }

        }
        private WarriorsDetails getBestWarrior()
        {
            int maxTreasures = -1;
            WarriorsDetails bestWarrior = registeredWarriors.get(0);
            for(WarriorsDetails warrior: registeredWarriors)
                if(warrior.getTreasureCollected() > maxTreasures) {
                    bestWarrior = warrior;
                    maxTreasures = warrior.getTreasureCollected();
                }
            return bestWarrior;
        }


        private int treasureInfoReceived()
        {
            int count = 0;
            for(WarriorsDetails warrior: registeredWarriors)
                if(warrior.getTreasureNoSentFlag())
                    count++;
            return count;
        }

        private void showWinner(String warriorName)
        {
            mapGui.drawText(WINNER_TEXT + warriorName + "!");
            removeBehaviour(gameBehaviour);
        }

        private void handleDraw()
        {
            mapGui.drawText(DRAW_TEXT);
            removeBehaviour(gameBehaviour);
        }

        private void sendInformationAboutEnd(WarriorsDetails warrior)
        {
            ACLMessage msg = new ACLMessage(ActionCode.END);
            msg.addReceiver(warrior.getAid());
            myAgent.send(msg);
        }


        private void handleWinner(int warriorsNumber)
        {
            if(warriorsNumber == 1) {
                WarriorsDetails warrior = new WarriorsDetails();
                for( WarriorsDetails tempwarrior: registeredWarriors) {
                    if(!tempwarrior.isDeadFlag()) {
                        warrior = tempwarrior;
                        break;
                    }
                }


                String warriorName = warrior.getAid().toString();
                sendInformationAboutEnd(warrior);
                showWinner(warriorName);
            }
            else if(warriorsNumber == 0)
                handleDraw();

        }



        private void handleReceiveDead()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.LIVESTATE);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID senderAID = msg.getSender();
                String result = msg.getContent();

                int index = getListIndexByAID(senderAID);

                if (result.equals("DEAD"))
                {
                     registeredWarriors.get(index).setDeadFlag(true);
                }
                if (index != -1 && !registeredWarriors.get(index).isDecisionFlag()) {
                    registeredWarriors.get(index).setDecisionFlag(true);
                    warriorsMoved++;
                }
            }

            int warriorsNumber = 0;
            for(WarriorsDetails warrior: registeredWarriors) {
                if(!warrior.isDeadFlag())
                    warriorsNumber ++;
            }







            // if (warriorsMoved == registeredWarriors.size()) {
            if(warriorsNumber == 1)
            {
                handleWinner(warriorsNumber);
                //gameStep = GameSteps.ONE_WARRIOR_LEFT;
            }
            else if(warriorsNumber <= 0)
            {
                handleDraw();
                //gameStep = GameSteps.NO_WARRIOR_LEFT;
            }
            else if(map.allTreasuresCollected())
            {
                handleReceiveTreasure();
                //gameStep = GameSteps.RECEIVE_TREASURE_NO;
            }
            else {
                //resetWarriorsFlag();
               // warriorsMoved = 0;
               // gameStep = GameSteps.SEND_MOVES;
            }
        }

    }








    private class GetRegistration extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.REGISTER);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();

                if (registeredWarriors.size() < MAX_WARRIORS) {

                    AID senderAID = msg.getSender();
                    Color setColor = avilibleColors.get(registeredWarriors.size());
                    //  warriorColors.add(setColor);
                    WarriorsDetails warrior = new WarriorsDetails(msg.getSender(), setColor);
                    reply.setContent(Integer.toString(setColor.getRGB()));
                    reply.setPerformative(ActionCode.REGISTER_ACCEPT);

                    //setting GUI
                    registeredWarriors.add(warrior);
                    prepGui.setWarriorsNumber(registeredWarriors.size());

                  //  System.out.println("Zarejestrowano wojownika " + senderAID);
                } else {
                    reply.setPerformative(ActionCode.REGISTER_DENY);
                  //  System.out.println("Odrzucono rejestracje " + msg.getSender());
                }

                myAgent.send(reply);
               // System.out.println(msg);
            } else {
                block();
            }
        }

    }

    private enum GameSteps {
        SEND_MOVES,
        RECEIVE_MOVES,
        MAKE_MOVES,
        RECEIVE_DEAD,
        TREASURE_NO_REQUEST,
        RECEIVE_TREASURE_NO,
        ONE_WARRIOR_LEFT,
        NO_WARRIOR_LEFT
    }
}
