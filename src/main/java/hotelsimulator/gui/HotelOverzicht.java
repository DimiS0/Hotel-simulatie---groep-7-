package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Schoonmaker;
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
        setSize(600, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Legenda bovenaan ---
        // Legt uit welk symbool bij welke rol hoort
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legendaPanel.setBorder(BorderFactory.createTitledBorder("Legenda"));

        // Gast-symbool: blauwe gevulde cirkel met "G"
        legendaPanel.add(maakSymboolLabel("G", new Color(50, 255, 50), "Gast"));

        // Scheidingsteken
        legendaPanel.add(new JLabel("|"));

        // Schoonmaker-symbool: paarse gevulde cirkel met "S"
        legendaPanel.add(maakSymboolLabel("S", new Color(160, 32, 240), "Schoonmaker"));

        add(legendaPanel, BorderLayout.NORTH);

        // --- Ruimtenoverzicht in het midden ---
        mainPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(
                mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        //  Statusbalk onderaan
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        verversen();
        setVisible(true);

        // Ververst het overzicht elke keer dat de eventManager een NONE-event stuurt
        eventManager.register(evt -> {
            if (evt.getEventType() == HotelEventType.NONE) {
                SwingUtilities.invokeLater(this::verversen);
            }
        });
    }

    // Maakt een klein label met een gekleurd cirkel-icoon en een tekst ernaast
    // Wordt gebruikt in de legenda om het symbool uit te leggen
    private JPanel maakSymboolLabel(String letter, Color kleur, String beschrijving) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        wrapper.setOpaque(false);

        // Cirkel met letter erin als custom component
        JPanel cirkel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(kleur);
                g.fillOval(0, 0, 20, 20);
                g.setColor(Color.BLACK);
                g.drawOval(0, 0, 20, 20);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 11));
                g.drawString(letter, 6, 14);
            }
        };
        cirkel.setPreferredSize(new Dimension(22, 22));
        cirkel.setOpaque(false);

        wrapper.add(cirkel);
        wrapper.add(new JLabel("= " + beschrijving));
        return wrapper;
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

            // Elke ruimte krijgt een knop met ruimtenaam + symbolen voor aanwezige personen
            JButton btn = maakRuimteKnop(ruimte);
            btn.addActionListener(e -> openRuimteDialog(ruimte));
            mainPanel.add(btn, gbc);
        }

        statusLabel.setText("Totaal aantal personen in simulatie: " + hotel.getPersonen().size());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JButton maakRuimteKnop(HotelRuimte ruimte) {
        long aantalG = hotel.getPersonen().stream()
                .filter(p -> p instanceof Gast && p.getHuidigeRuimte() == ruimte)
                .count();
        long aantalS = hotel.getPersonen().stream()
                .filter(p -> p instanceof Schoonmaker && p.getHuidigeRuimte() == ruimte)
                .count();

        boolean inGebruik = (aantalG + aantalS) > 0;

        String emoji = switch (ruimte.getAreaType()) {
            case "Cinema"     -> "🎬";
            case "Restaurant" -> "🍽";
            case "Fitness"    -> "💪";
            case "Room"       -> "🛏";
            default           -> "";
        };

        StringBuilder html = new StringBuilder("<html><center>");
        if (!emoji.isEmpty()) html.append(emoji).append(" ");
        html.append("<b>").append(ruimte.getAreaType()).append("</b><br>");

        for (int i = 0; i < aantalG; i++) {
            html.append("<font color='#0064FF'><b>G</b></font> ");
        }
        for (int i = 0; i < aantalS; i++) {
            html.append("<font color='#9400D3'><b>S</b></font> ");
        }
        html.append("</center></html>");

        JButton btn = new JButton(html.toString());
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        if (inGebruik) {
            btn.setBackground(new Color(144, 238, 144));
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 3));
        } else {
            btn.setBackground(null);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        return btn;
    }

    // Opent een detaildialoog voor een specifieke ruimte met live-bijwerkende info
    private void openRuimteDialog(HotelRuimte ruimte) {
        JDialog dialog = new JDialog(this, ruimte.getAreaType(), false);
        dialog.setSize(340, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Runnable die de tekst in het dialoogvenster bijwerkt.
        Runnable updateInfo = () -> {
            long gastenAanwezig = hotel.getPersonen().stream()
                    .filter(p -> p instanceof Gast && p.getHuidigeRuimte() == ruimte)
                    .count();
            long schoonmakersAanwezig = hotel.getPersonen().stream()
                    .filter(p -> p instanceof Schoonmaker && p.getHuidigeRuimte() == ruimte)
                    .count();
            infoArea.setText(
                    "Sterren: " + ruimte.getSterrenAantal() + "\n" +
                            "Aantal gasten: " + gastenAanwezig + "\n" +
                            "Aantal Schoonmakers " + schoonmakersAanwezig + "\n" +
                            "Max personen: " + ruimte.getMaxPersonen() + "\n"
            );
        };


        updateInfo.run();

        JButton sluitBtn = new JButton("Sluiten");
        sluitBtn.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        dialog.add(sluitBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        // Ververst de info in het dialoogvenster elke keer via de eventManager
        eventManager.register(evt -> {
            if (evt.getEventType() == HotelEventType.NONE && dialog.isShowing()) {
                SwingUtilities.invokeLater(updateInfo);
            }
        });
    }
}
