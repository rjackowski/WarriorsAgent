package src;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WarriorAgent extends Agent {
    private int live;
    private int strength;
    private int location;



    protected void setup() {
        System.out.println("Warrior created");
        addBehaviour(new AskForLocation());


    }

    private class AskForLocation extends Behaviour {
        private AID bestSeller; // The agent who provides the best offer
        private int bestPrice;  // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents
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
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }



//


        }

        public boolean done() {
            return true;
        }
    }  // End of inner class RequestPerformer




}
