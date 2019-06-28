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

    public List<WarriorsDetails> getRegisteredWarriors() {
        return registeredWarriors;
    }

    protected void setup() {
        System.out.println("Map created");

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

        System.out.println("Map registred");
        addBehaviour(new GetRegistration());
        addBehaviour(new ManageAction());
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

                    //Wysłanie informacji o możliwych ruchach
                    case SEND_MOVES: {
                        for (WarriorsDetails warrior : registeredWarriors) {
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
                            myAgent.send(msg);
                            System.out.println("Wysłano mozliwe ruchy ");
                        }
                        gameStep = GameSteps.RECEIVE_MOVES;
                    }
                    break;
                    //Odbieranie informacji o wykonanych ruchach
                    case RECEIVE_MOVES:
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

                        if (warriorsMoved == registeredWarriors.size()) {

                            warriorsMoved = 0;
                            gameStep=GameSteps.MAKE_MOVES;

                        }
                        break;
                    //Wykonanie ruchów
                    case MAKE_MOVES:

                        for (int i = 0; i < registeredWarriors.size(); i++) {
                            DecisionPackage decPackage = registeredWarriors.get(i).getDecPack();
                            if (decPackage.getType() == 'M') {
                                mapGui.changeWariorLocation(i, decPackage.getDirection());
                            }
                            ACLMessage msgAttack = new ACLMessage(ActionCode.ATTACK);
                            msgAttack.addReceiver(registeredWarriors.get(i).getAid());
                            DecisionPackage attackPack = new DecisionPackage('A', i, 15);
                            try {
                                msgAttack.setContentObject(attackPack);
                                myAgent.send(msgAttack);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        Vector<WarriorsDetails> warriorsCollectedTreasure = new Vector<WarriorsDetails>();
                        for(int i = 0 ; i < registeredWarriors.size(); i++) {
                            DecisionPackage decPackage = registeredWarriors.get(i).getDecPack();
                            if (decPackage.getType() == 'M') {
                                System.out.println("Wykonać ruch dla: " + i );
                                if(map.changeWariorLocation(i,'T'))
                                    warriorsCollectedTreasure.add(registeredWarriors.get(i));

                                System.out.println("Wykonać ruch");

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
                        try{
                            Thread.sleep(2000);}
                        catch(Exception ex){ex.printStackTrace();}
                        resetWarriorsFlag();
                    
                        mapGui.updateMap();
                        try {
                            Thread.sleep(2000);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                        resetWarriorsFlag();
                        gameStep = GameSteps.RECEIVE_DEAD;

                        break;
                    //Odebranie info o umierającyc
                    case RECEIVE_DEAD:
                         mt = MessageTemplate.MatchPerformative(ActionCode.LIVESTATE);
                         msg = myAgent.receive(mt);
                        if (msg != null) {
                            AID senderAID = msg.getSender();
                            String result = msg.getContent();

                            int index = getListIndexByAID(senderAID);

                            if (result == "DEAD")
                                registeredWarriors.get(index).setDeadFlag(true);
                            if (index != -1 && !registeredWarriors.get(index).isDecisionFlag()) {
                                registeredWarriors.get(index).setDecisionFlag(true);
                                warriorsMoved++;
                            }
                        }
                        if (warriorsMoved == registeredWarriors.size()) {
                            resetWarriorsFlag();
                            warriorsMoved = 0;
                            gameStep=GameSteps.SEND_MOVES;

                        }

                        break;


                }
            }
        }

        private void resetWarriorsFlag() {
            for (WarriorsDetails warrior : registeredWarriors)
                warrior.setDecisionFlag(false);
        }

        public int getListIndexByAID(AID aid) {
            for (int i = 0; i < getRegisteredWarriors().size(); i++) {
                System.out.println("1:" + aid);
                System.out.println("2:" + getRegisteredWarriors().get(i).getAid());
                if (getRegisteredWarriors().get(i).getAid().equals(aid)) {
                    return i;
                }
            }
            return -1;
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

                    System.out.println("Zarejestrowano wojownika " + senderAID);
                } else {
                    reply.setPerformative(ActionCode.REGISTER_DENY);
                    System.out.println("Odrzucono rejestracje " + msg.getSender());
                }

                myAgent.send(reply);
                System.out.println(msg);
            } else {
                block();
            }
        }

    }

    private enum GameSteps {
        SEND_MOVES,
        RECEIVE_MOVES,
        MAKE_MOVES,
        RECEIVE_DEAD
    }
}
