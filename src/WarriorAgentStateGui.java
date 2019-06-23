/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A.

 GNU Lesser General Public License

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation,
 version 2.1 of the License.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/
package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 @author Giovanni Caire - TILAB
 */
class WarriorAgentStateGui extends JFrame {
    private WarriorAgent myAgent;

    private JTextField liveField, coinField;
    private JButton agentButton;

    WarriorAgentStateGui(WarriorAgent a) {
        super(a.getLocalName());

        myAgent = a;
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1,1));
          p.add(new JLabel("Agent"));
          p.add(new JLabel("Nazwas"));
        getContentPane().add(p, BorderLayout.NORTH);



         p = new JPanel();
        p.setLayout(new GridLayout(1,1));




     //   p.add(new JLabel("Agent"));
      //  p.add(new JLabel("Kolor"));
        agentButton = new JButton("");
        agentButton.setBackground(a.getColor());
        p.add(agentButton);
        getContentPane().add(p, BorderLayout.AFTER_LAST_LINE);



        p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Życie:"));
        liveField = new JTextField(15);
        p.add(liveField);
        p.add(new JLabel("Ilość skarbów:"));
        coinField = new JTextField(15);
        p.add(coinField);
        getContentPane().add(p, BorderLayout.CENTER);


        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }


    public void showParamaters() {
        liveField.setText( Integer.toString(myAgent.getLive()));
     //   System.out.println("I am here: ");
        coinField.setText(Integer.toString(myAgent.getCoinAmount()));
    }

    public void showGui() {
        pack();
        showParamaters();
       // Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //int centerX = (int)screenSize.getWidth() / 2;
       // int centerY = (int)screenSize.getHeight() / 2;
       // setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public void refreshGui() {
        pack();
        showParamaters();
        super.setVisible(true);
    }

    public void setColor(Color color)
    {
        agentButton.setBackground(color);
    }


}
