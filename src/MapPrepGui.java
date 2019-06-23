package src;
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


import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

class MapPrepGui extends JFrame {
    private MapAgent myAgent;

    private JLabel warriorsNumberLabel;
    private JLabel treasuresLabel;
    private JButton startButton;
    private JFormattedTextField treasureText;

    MapPrepGui(MapAgent a) {
        super(a.getLocalName());



        myAgent = a;

        //add panel
        JPanel p = new JPanel();

        getContentPane().add(p, BorderLayout.CENTER);
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //add warriors label
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(3,3,3,3);
        warriorsNumberLabel = new JLabel();
        setWarriorsNumber(0);
        p.add(warriorsNumberLabel, c);

        //add treasure label
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = new Insets(3,3,3,3);
        treasuresLabel = new JLabel();
        treasuresLabel.setText("Treasures to spawn:");
        p.add(treasuresLabel, c);

        //add treasure text
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.insets = new Insets(3,3,3,3);
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(100);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        treasureText = new JFormattedTextField(formatter);
        treasureText.setValue(5);
        treasureText.setColumns(4);
        p.add(treasureText, c);

        //add start button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = new Insets(3,3,3,3);
        startButton = new JButton();
        startButton.setText("START");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myAgent.onStartClick();
            }
        });
        p.add(startButton, c);

        //on close window
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(true);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public void hideGui()
    {
        super.setVisible(false);
    }

    public void setWarriorsNumber(int number)
    {
        warriorsNumberLabel.setText("Warriors registered: " + number);
    }
}
