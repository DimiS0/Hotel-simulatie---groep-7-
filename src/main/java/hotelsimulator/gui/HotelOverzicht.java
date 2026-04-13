package hotelsimulator.gui;

import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;
import hotelsimulator.ruimtes.HotelRuimte;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Comparator;


public class HotelOverzicht extends JFrame {

    public HotelOverzicht(Hotel hotel) {
        setTitle("Hotel Overzicht");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx      = ruimte.getX();
            gbc.gridy      = ruimte.getY();
            gbc.gridwidth  = ruimte.getBreedte();
            gbc.gridheight = ruimte.getHoogte();
            gbc.fill       = GridBagConstraints.BOTH;
            gbc.insets     = new Insets(2, 2, 2, 2);
            gbc.weightx    = 1.0;
            gbc.weighty    = 1.0;

            JButton btn = new JButton(ruimte.getAreaType());
            btn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "Sterren: " + ruimte.getSterrenAantal() + "\n" +
                            "Max: " + ruimte.getMaxPersonen() + " pers.",
                    ruimte.getAreaType(),
                    JOptionPane.INFORMATION_MESSAGE));

            mainPanel.add(btn, gbc);
        }

        setVisible(true);
    }
}