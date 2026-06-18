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

        //legenda
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legendaPanel.setBorder(BorderFactory.createTitledBorder("Legenda"));

        //gast symbool maken met een G
        legendaPanel.add(maakSymboolLabel("G", new Color(50, 255, 50), "Gast"));

        // Scheidingsteken
        legendaPanel.add(new JLabel("|"));

        // Schoonmaker symbool met een paarse S
        legendaPanel.add(maakSymboolLabel("S", new Color(160, 32, 240), "Schoonmaker"));

        //boven toevoegen
        add(legendaPanel, BorderLayout.NORTH);

        //ruimte overicht
        mainPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(
                mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        //scroll snelhaied
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        //  Statusbalk onderaan
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        verversen();
        setVisible(true);

        // Ververst het overzicht elke 500ms
        Timer timer = new Timer(500, e -> SwingUtilities.invokeLater(this::verversen));
        timer.start();

        //luistert naar of hoteloverzicht gesloten is
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {

                //timer stoppen zodat we niet 100den timers hebben
                timer.stop();
            }
        });

    }

    // Maakt een klein label met een gekleurd cirkel-icoon en een tekst ernaast
    // Wordt gebruikt in de legenda om het symbool uit te leggen
    private JPanel maakSymboolLabel(String letter, Color kleur, String beschrijving) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        //niet tarnsperant maken
        wrapper.setOpaque(false);

        // Cirkel met letter erin als custom component
        JPanel cirkel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                //klaar maken om te tekenen
                super.paintComponent(g);

                //vaste kleur toevoegen
                g.setColor(kleur);

                //vullen
                g.fillOval(0, 0, 20, 20);

                //outline
                g.setColor(Color.BLACK);

                //outline tekenen
                g.drawOval(0, 0, 20, 20);

                //tekst color
                g.setColor(Color.WHITE);

                //font kiezen en grootte
                g.setFont(new Font("Arial", Font.BOLD, 11));

                //symbool tekenen
                g.drawString(letter, 6, 14);
            }
        };

        //grootte van de cirkel vastzetten

        cirkel.setPreferredSize(new Dimension(22, 22));

        //geen doorzichtbaarheid
        cirkel.setOpaque(false);

        //toevoegen aan de wrapper
        wrapper.add(cirkel);
        wrapper.add(new JLabel("= " + beschrijving));
        return wrapper;
    }

    public void verversen() {
        //Alle knopen verwijderen
        mainPanel.removeAll();

        //loopen door alle ruimtes en x en y en hoogte en breedte doorgeven
        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = ruimte.getX();
            gbc.gridy = ruimte.getY();
            gbc.gridwidth = ruimte.getBreedte();
            gbc.gridheight = ruimte.getHoogte();
            gbc.fill = GridBagConstraints.BOTH;

            //marge tussen knoopen
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            // Elke ruimte krijgt een knop met ruimtenaam + symbolen voor aanwezige personen
            JButton btn = maakRuimteKnop(ruimte);

            //elke button heeft een action listener wat een dialoog opent
            btn.addActionListener(e -> openRuimteDialog(ruimte));
            mainPanel.add(btn, gbc);
        }

        //bijwerken van status balk
        statusLabel.setText("Totaal aantal personen in simulatie: " + hotel.getPersonen().size());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JButton maakRuimteKnop(HotelRuimte ruimte) {
        //Gasten en Schoonmakers Long omdat .count een long terug geeft
        long aantalG = hotel.getPersonen().stream().filter(p -> p instanceof Gast && p.getHuidigeRuimte() == ruimte).count();
        long aantalS = hotel.getPersonen().stream().filter(p -> p instanceof Schoonmaker && p.getHuidigeRuimte() == ruimte).count();

        //ruimte in gebruik als er minstens een persoon is
        boolean inGebruik = (aantalG + aantalS) > 0;

        //elke kamer krijgt zijn eigen emoji
        String emoji = switch (ruimte.getAreaType()) {
            case "Cinema"     -> "🎬";
            case "Restaurant" -> "🍽";
            case "Fitness"    -> "💪";
            case "Room"       -> "🛏";
            default           -> "";
        };

        //html mode, omdat dit het misnte werk is dan component g of meerderelabels in een panel stoppen
        //html tekst opbouwen voor knop
        StringBuilder html = new StringBuilder("<html><center>");

        //als er een emoji is die toevoegen, daarna naam van ruimte
        if (!emoji.isEmpty()) html.append(emoji).append(" ");
        html.append("<b>").append(ruimte.getAreaType()).append("</b><br>");

        //blauwe G, voor elke gast
        for (int i = 0; i < aantalG; i++) {
            html.append("<font color='#0064FF'><b>G</b></font> ");
        }
        //paarse S, voor elke schoonmaker
        for (int i = 0; i < aantalS; i++) {
            html.append("<font color='#9400D3'><b>S</b></font> ");
        }
        //html tekst afsluiten
        html.append("</center></html>");

        //knop krijgt het als tekst
        //dus knop aanmaken met de opgebouwde html tekst
        JButton btn = new JButton(html.toString());

        //zet de tekst in het midden
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        //iemeand is in de ruimte
        if (inGebruik) {

            //groen gekluered
            btn.setBackground(new Color(144, 238, 144));

            //doorzzichtig
            btn.setOpaque(true);

            //border maken met donkergroen
            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 3));
        } else {
            //geen backround
            btn.setBackground(null);
            //en normale grijze outline
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }

        //button terug geven
        return btn;
    }

    // Opent een detaildialoog voor een specifieke ruimte met live bijwerkende info
    private void openRuimteDialog(HotelRuimte ruimte) {
        // nieuw dialoogvenster aanmaken met de naam van de ruimte als titel
        JDialog dialog = new JDialog(this, ruimte.getAreaType(), false);

        //grootte  van dialoog zzetten
        dialog.setSize(340, 260);

        // dialoog midden op het overzichtvenster plaatsen
        dialog.setLocationRelativeTo(this);

        //layout instellen
        dialog.setLayout(new BorderLayout());

        //tekstvak voor informatie in de ruimte
        JTextArea infoArea = new JTextArea();

        //tekst niet kunnen veranderen
        infoArea.setEditable(false);

        //font zetten
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));

        //Runnable die de tekst in het dialoogvenster bijwerkt.
        Runnable updateInfo = () -> {
            //gasten en schoonmakers opnieuw optellen
            long gastenAanwezig = hotel.getPersonen().stream().filter(p -> p instanceof Gast && p.getHuidigeRuimte() == ruimte).count();
            long schoonmakersAanwezig = hotel.getPersonen().stream().filter(p -> p instanceof Schoonmaker && p.getHuidigeRuimte() == ruimte).count();

            //tekst van het inflo vlak bijwerken met nieuwe gegevens
            infoArea.setText("Sterren: " + ruimte.getSterrenAantal() + "\n" + "Aantal gasten: " + gastenAanwezig + "\n" + "Aantal Schoonmakers " + schoonmakersAanwezig + "\n" + "Max personen: " + ruimte.getMaxPersonen() + "\n"
            );
        };

        //direct runnen bij het openen
        updateInfo.run();

        //sluiten button
        JButton sluitBtn = new JButton("Sluiten");

        //dialoog sluiten bij click
        sluitBtn.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        dialog.add(sluitBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        // Ververst de info in het dialoogvenster elke keer via de eventManager
        eventManager.register(evt -> {
            // alleen bijwerken als het dialoogvenster nog zichtbaar is
            if (dialog.isShowing()) {
                SwingUtilities.invokeLater(updateInfo);
            }
        });
    }
}
