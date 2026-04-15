package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;

public class HotelOverzicht extends JFrame {

    private final Hotel hotel;
    private final HotelEventManager eventManager;
    private final JPanel mainPanel;
    private final JLabel statusLabel;

    public HotelOverzicht(Hotel hotel, HotelEventManager eventManager) {
        this.hotel = hotel;
        this.eventManager = eventManager;

        setTitle("Hotel Overzicht");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(
                mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        verversen();
        setVisible(true);

        //timer zodat de layout wordt ververst als de layout misschien veranderdt
        eventManager.register(evt -> {
            if (evt.getEventType() == HotelEventType.NONE) {
                SwingUtilities.invokeLater(this::verversen);
            }
        });
    }

    public void verversen() {
        mainPanel.removeAll();

        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = ruimte.getX();
            gbc.gridy = ruimte.getY();
            gbc.gridwidth = ruimte.getBreedte();
            gbc.gridheight = ruimte.getHoogte();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            JButton btn = new JButton(ruimte.getAreaType());

            btn.addActionListener(e -> openRuimteDialog(ruimte));

            mainPanel.add(btn, gbc);
        }

        statusLabel.setText("Totaal aantal personen in simulatie: " + hotel.getPersonen().size());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void openRuimteDialog(HotelRuimte ruimte) {
        JDialog dialog = new JDialog(this, ruimte.getAreaType(), false);
        dialog.setSize(320, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));

        //interface zegt dat updateinfo iets kan uitvoeren, je kan het oproepen met .run . Wordt op de achtergrond gedraait. () -> is de verkorte versie.
        Runnable updateInfo = () -> infoArea.setText(
                "Sterren: " + ruimte.getSterrenAantal() + "\n" +
                        "Aantal gasten: " + ruimte.getAantalGasten() + "\n" +
                        "Aantal Schoonmakers " + ruimte.getAantalSchoonmakers+ "\n" +
                        "Max personen: " + ruimte.getMaxPersonen() + "\n"
        );

        updateInfo.run();

        JButton sluitBtn = new JButton("Sluiten");
        sluitBtn.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        dialog.add(sluitBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);

        //timer van school, elke keer updateInfo
        eventManager.register(evt -> {
            if (evt.getEventType() == HotelEventType.NONE && dialog.isShowing()) {
                SwingUtilities.invokeLater(updateInfo);
            }
        });
    }
}