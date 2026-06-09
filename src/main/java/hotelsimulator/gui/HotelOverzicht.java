package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        //legenda bovenaan, Legt uit welk symbool bij welke rol hoort
        JPanel legendaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legendaPanel.setBorder(BorderFactory.createTitledBorder("Legenda"));

        //Gast symbool, blauwe gevulde cirkel met "G"
        legendaPanel.add(maakSymboolLabel("G", new Color(50, 255, 50), "Gast"));

        // Scheidingsteken
        legendaPanel.add(new JLabel("|"));

        //Schoonmakersymbool paarse gevulde cirkel met "S"
        legendaPanel.add(maakSymboolLabel("S", new Color(160, 32, 240), "Schoonmaker"));

        add(legendaPanel, BorderLayout.NORTH);

        //Ruimtenoverzicht in het midden
        mainPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(
                mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
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

        //Stopt de timer wanneer het overzichtvenster wordt gesloten
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                timer.stop();
            }
        });
    }

    // Maakt een label voor de legenda met een gekleurde cirkel en een beschrijving.
    private JPanel maakSymboolLabel(String letter, Color kleur, String beschrijving) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        wrapper.setOpaque(false);

        //ekent een gekleurde cirkel met daarin een letter
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

        // Zet het symbool en de beschrijving naast elkaar
        wrapper.add(cirkel);
        wrapper.add(new JLabel("= " + beschrijving));
        return wrapper;
    }

    //overzicht verversen voor real time info
    public void verversen() {
        mainPanel.removeAll();

        //Plaatst voor elke ruimte een knop op de juiste positie
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

            // Elke knop toont de naam van de ruimte en aanwezige personen
            JButton btn = maakRuimteKnop(ruimte);
            btn.addActionListener(e -> openRuimteDialog(ruimte));
            mainPanel.add(btn, gbc);
        }

        //Laat onderaan zien hoeveel personen er in totaal in de simulatie zitten
        statusLabel.setText("Totaal aantal personen in simulatie: " + hotel.getPersonen().size());

        //Tekent het paneel opnieuw nadat alle knoppen zijn toegevoegd
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    //maakt elke knop
    private JButton maakRuimteKnop(HotelRuimte ruimte) {
        // Telt hoeveel gasten zich in deze ruimte bevinden, long omdat count met long werkt
        long aantalG = hotel.getPersonen().stream()
                .filter(p -> p instanceof Gast && p.getHuidigeRuimte() == ruimte)
                .count();

        // Telt hoeveel schoonmakers zich in deze ruimte bevinden
        long aantalS = hotel.getPersonen().stream()
                .filter(p -> p instanceof Schoonmaker && p.getHuidigeRuimte() == ruimte)
                .count();

        //controlle voor de zekerheid
        boolean inGebruik = (aantalG + aantalS) > 0;

        //kiest een emoji bij de juiste ruimte
        String emoji = switch (ruimte.getAreaType()) {
            case "Cinema"     -> "🎬";
            case "Restaurant" -> "🍽";
            case "Fitness"    -> "💪";
            case "Room"       -> "🛏";
            default           -> "";
        };

        // Gebruikt HTML om tekst en symbolen eenvoudig in de knop te tonen
        StringBuilder html = new StringBuilder("<html><center>");

        //Toont eerst de emoji en daarna de naam van de ruimte
        if (!emoji.isEmpty()) html.append(emoji).append(" ");
        html.append("<b>").append(ruimte.getAreaType()).append("</b><br>");

        //blauwe G  voor elke aanwezige gast
        for (int i = 0; i < aantalG; i++) {
            html.append("<font color='#0064FF'><b>G</b></font> ");
        }
        //Voegt voor elke aanwezige schoonmaker een paarse S
        for (int i = 0; i < aantalS; i++) {
            html.append("<font color='#9400D3'><b>S</b></font> ");
        }
        //html string afmaken
        html.append("</center></html>");

        //knop maken emt de html tekst
        JButton btn = new JButton(html.toString());

        //zet de tekst in het midden
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        //in bezette ruimtes een kleur geven die opvallend is
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

        // Tekstvak waarin de details van de ruimte worden getoond
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Runnable die de tekst in het dialoogvenster bijwerkt. geen nieuwe thread
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

        // Toont direct de eerste informatie
        updateInfo.run();

        //Knop om het dialoogvenster te sluiten
        JButton sluitBtn = new JButton("Sluiten");
        sluitBtn.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        dialog.add(sluitBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        // Ververst de info in het dialoogvenster elke keer via de eventManager
        eventManager.register(evt -> {
            if (dialog.isShowing()) {
                //opnieuw uitvoeren van de runnable bij elke event
                SwingUtilities.invokeLater(updateInfo);
            }
        });
    }
}
