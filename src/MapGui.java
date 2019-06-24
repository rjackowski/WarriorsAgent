package src; /*****************************************************************
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


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 @author Giovanni Caire - TILAB
 */
class MapGui extends JFrame {
    private MapAgent myAgent;

    private JTextField titleField, priceField,thirdField, fourthField;
    private BufferedImage wallImage, floorImage;
    private MapField map;
    private JPanel panel;

    MapGui(MapAgent a, MapField map) {
        super(a.getLocalName());
        this.map = map;
        loadImages();

        myAgent = a;

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                for (int i = 0; i < map.getSizeX(); i++) {
                    int drawXPosition = wallImage.getHeight() * i;
                    for (int j = 0; j < map.getSizeY(); j++) {
                        int drawYPosition = wallImage.getWidth() * j;
                        char drawType = map.getFromPosition(j, i);
                        BufferedImage tempImage;
                        if (drawType == ' ')
                            tempImage = floorImage;
                        else
                            tempImage = wallImage;
                        g.drawImage(tempImage, drawXPosition, drawYPosition, null);
                    }
                }
            }
        };

        panel.setSize(map.getSizeX() * wallImage.getWidth(), map.getSizeY() * wallImage.getHeight());
        getContentPane().add(panel, BorderLayout.CENTER);

        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void updateMap()
    {
        panel.repaint();
    }

    public void showGui() {
        pack();
     //   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       // int centerX = (int)screenSize.getWidth() / 2;
       // int centerY = (int)screenSize.getHeight() / 2;
       // setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        setSize(map.getSizeX() * wallImage.getWidth(), map.getSizeY() * wallImage.getHeight());
        super.setVisible(true);
    }

    private void loadImages() {
        try {
            wallImage = ImageIO.read(new File("Images/wall.jpg"));
            floorImage = ImageIO.read(new File("Images/ground.jpg"));
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
