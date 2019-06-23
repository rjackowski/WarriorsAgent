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


public class WarriorAgent extends Agent {


    private int live;
    private int strength;

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

        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                if(startFlag) {
                    if (existOnMap == false)
                        myAgent.addBehaviour(reg);
                    else {
                        myAgent.addBehaviour(new SubstrLive());
                        myStateGui.refreshGui();
                    }
                }
            }
        });


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


    private class SubstrLive extends Behaviour {
        public void action() {
            live = live - 1;
        }

        public boolean done() {
            return true;
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
                        System.out.println("Found the following map:");

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
                    System.out.println("Send register request");
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
                    System.out.println("Receive");
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ActionCode.REGISTER_ACCEPT) {
                            try {
                                color = new Color(Integer.parseInt(reply.getContent()));
                            }catch(Exception ex) {

                            }
                        }
                        else if(reply.getPerformative() == ActionCode.REGISTER_DENY){
                            doDelete();
                        }

                        if (color!= null) {
                            System.out.println("color Done");
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
                    existOnMap = true;
                    break;
            }
        }

        @Override
        public boolean done() {

            if (step == 3) {
                System.out.println("Done");
                return true;
            }

            else
                return false;
        }
    }


    private class AskForLocation extends Behaviour {
        //private AID map;

        //private AID bestSeller; // The agent who provides the best offer
        //private int bestPrice;  // The best offered price
        //private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        private AID[] map;

        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("map");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println("Found the following maps:");
                map = new AID[result.length];
                for (int i = 0; i < result.length; ++i) {
                    map[i] = result[i].getName();
                    ACLMessage question = new ACLMessage(ACLMessage.INFORM);
                    question.addReceiver(map[i]);
                    question.setContent("Location");
                    question.setConversationId("location");
                    myAgent.send(question);
                    System.out.println(map[i].getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }


//


        }

        public boolean done() {
            return true;
        }
    }  // End of inner class RequestPerformer

}
