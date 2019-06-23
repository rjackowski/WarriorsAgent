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


public class WarriorAgent extends Agent {


    private int live;
    private int strength;
    private InformationPackage infoPack;

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

        public void action() {
            if(startFlag) {
                if (existOnMap == false)
                    myAgent.addBehaviour(reg);

                // Wojownik znajduje się na mapie
                else {
                    myAgent.addBehaviour(new SubstrLive(1));
                    myStateGui.refreshGui();
                }
            }
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
                                System.out.println("Jestem tu");
                                color = new Color(Integer.parseInt(reply.getContent()));
                            }catch(Exception ex) {
                                ex.printStackTrace();
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
                   // existOnMap = true;
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



}
