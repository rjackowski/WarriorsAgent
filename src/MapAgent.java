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
import java.util.*;
import java.util.List;

public class MapAgent extends Agent {

    private MapPrepGui prepGui;
    private MapGui mapGui;

    private static int MAX_WARRIORS = 8;
    private static Map<String, Color> warriorColors;

    private List<Color> avilibleColors;

    protected void setup() {
        System.out.println("Map created");

        SetupColors();

        prepGui = new MapPrepGui(this);
        prepGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("map");
        sd.setName("JADE-map");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Map registred");
        addBehaviour(new GetRandomLocation());

    }

    private void SetupColors()
    {
        avilibleColors = List.of(Color.BLACK, Color.BLUE, Color.ORANGE,
                Color.WHITE, Color.RED, Color.GREEN,
                Color.GRAY, Color.CYAN);
    }

    public void onStartClick()
    {
        System.out.println("Starting map generation");
    }


    private class GetRandomLocation extends CyclicBehaviour {
        private AID bestSeller; // The agent who provides the best offer
        private int bestPrice;  // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if(msg !=  null) {
                System.out.println(msg);
            }
//            myAgent.send(msg);
        }

    }  // End of inner class RequestPerformer
}
