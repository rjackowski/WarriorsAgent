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
    public static List<Color> warriorColors;

    private List<Color> avilibleColors;
    private List<AID> registeredWarriors;

    private boolean startFlag = false;
    private boolean waitForAllWarriors = false;

    protected void setup() {
        System.out.println("Map created");

        SetupColors();
        prepGui = new MapPrepGui(this);
        prepGui.showGui();

        registeredWarriors = new Vector<AID>();
        warriorColors = new Vector<Color>();

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

    public void onStartClick(int treasuresToSpawn)
    {
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
                if (!waitForAllWarriors) {
                    for (AID warrior : registeredWarriors) {

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
                        msg.addReceiver(warrior);
                        try {
                            msg.setContentObject(infPack);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        msg.setConversationId("move_send");
                        msg.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                        myAgent.send(msg);
                        System.out.println("Wysłano mozliwe ruchy ");
                        waitForAllWarriors = true;
                    }
                }
            }

        }
    }


    private class GetRegistration extends CyclicBehaviour {

        public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ActionCode.REGISTER);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    ACLMessage reply = msg.createReply();

                    if(registeredWarriors.size() < MAX_WARRIORS) {
                        AID senderAID = msg.getSender();
                        Color setColor = avilibleColors.get(registeredWarriors.size());
                        warriorColors.add(setColor);
                        reply.setContent(Integer.toString(setColor.getRGB()));
                        reply.setPerformative(ActionCode.REGISTER_ACCEPT);

                        //setting GUI
                        registeredWarriors.add(senderAID);
                        prepGui.setWarriorsNumber(registeredWarriors.size());

                        System.out.println("Zarejestrowano wojownika " + senderAID);
                    }
                    else {
                        reply.setPerformative(ActionCode.REGISTER_DENY);
                        System.out.println("Odrzucono rejestracje " + msg.getSender());
                    }

                    myAgent.send(reply);
                    System.out.println(msg);
                }
                else {
                    block();
                }

                myAgent.send(reply);
                System.out.println(msg);
            } else {
                block();
            }
        }

    }
}
