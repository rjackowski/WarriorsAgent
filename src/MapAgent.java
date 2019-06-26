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

    private int gameStep = 0;

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
        MapField m = new MapField(registeredWarriors.size(), treasuresToSpawn);
        prepGui.hideGui();
        mapGui = new MapGui(this, m);
        mapGui.showGui();
        mapGui.updateMap();
        startFlag = true;
    }

    private class ManageAction extends CyclicBehaviour {
        public void action() {
            if (startFlag) {
                switch (gameStep) {

                    //Wysłanie informacji o możliwych ruchach
                    case 0: {
                        for (WarriorsDetails warrior : registeredWarriors) {
                            ACLMessage msg = new ACLMessage(ActionCode.POSITION);
                            InformationPackage infPack = new InformationPackage();
                            Vector<Character> visible = new Vector<Character>();
                            visible.add(' ');
                            visible.add(' ');
                            visible.add(' ');
                            infPack.setDownVisible(visible);
                            infPack.setRightVisible(visible);
                            visible.set(2, '1');
                            infPack.setTopVisible(visible);
                            visible.set(2, ' ');
                            visible.set(1, 's');
                            infPack.setLeftVisible(visible);
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
                        gameStep++;
                    }
                    break;
                    //Odbieranie informacji o wykonanych ruchach
                    case 1:
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
                            if(index != -1) {
                                registeredWarriors.get(index).setDecisionFlag(true);
                                registeredWarriors.get(index).setDecPack(decPack);
                            }
                        }

                        int count = 0;
                        for (WarriorsDetails warrior : registeredWarriors) {
                            if (!warrior.isDecisionFlag())
                                count ++;
                        }

                        if (count == 0) {
                            System.out.println("Odebrano wszystkie decyzje");
                            gameStep++;
                        }
                        break;
                        //Wykonanie ruchów
                    case 2:

                        for(int i = 0 ; i < registeredWarriors.size(); i++) {
                            DecisionPackage decPackage = registeredWarriors.get(i).getDecPack();
                            if (decPackage.getType() == 'M') {
                                System.out.println("Wykonać ruch dla: " + i );
                                mapGui.changeWariorLocation(i,'T');
                                System.out.println("Wykonać ruch");
                                try{
                                Thread.sleep(2000);}
                                catch(Exception ex){ex.printStackTrace();}
                            }
                        }
                        break;


                }


            }

        }
    }
    public int getListIndexByAID(AID aid) {
        for(int i = 0; i< getRegisteredWarriors().size(); i++) {
            System.out.println("1:"+ aid);
            System.out.println("2:"+ getRegisteredWarriors().get(i).getAid());
            if(getRegisteredWarriors().get(i).getAid().equals(aid)) {
                return i;
            }
        }
        return -1;
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
}
