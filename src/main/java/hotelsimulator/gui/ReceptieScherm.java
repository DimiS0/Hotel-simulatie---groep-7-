package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelsimulator.core.Hotel;
import hotelsimulator.core.SimulatieLus;
import hotelsimulator.korting.KortingFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class ReceptieScherm {
    private JFrame kortingFrame = new JFrame();
    private JPanel kortingScherm = new JPanel(new GridLayout(2,2));
    private JLabel saldo = new JLabel("Totale hotel Saldo € "+"0");
    private String saldoString = "";
    private Timer timer;

    private JButton studentenKorting = new JButton("StudentenKorting");
    private JButton loyaliteitskorting = new JButton("LoyaliteitsKorting");
    private JButton lastMinuteKorting = new JButton("LastMinuteKorting");
    private JButton geenKorting = new JButton("GEEN KORTING");
    private int randomSterren = new Random().nextInt(0,5);
    private int randomDialoog = new Random().nextInt(0,4);
    private JLabel foutLabel = new JLabel();
    private JLabel dialoog;
    private double [] prijsKamers = {100.0,150.0,200.0,250.0,300.0};
    private double saldoBerekenen = 0.0;
    private String[][] klantenDialoog = {
            // 1 ster
            {
                    "Ik wil graag een 1 sterren suite boeken. Ik ben student, mag ik studentenkorting?",
                    "Ik wil graag een 1 sterren suite boeken. Ik kom hier al jaren, krijg ik loyaliteitskorting?",
                    "Ik wil graag last minute een 1 sterren suite boeken. Is er nog korting beschikbaar?",
                    "Ik wil graag een 1 sterren suite boeken. Ik val niet onder kortingen, de standaard prijs is prima."
            },
            // 2 sterren
            {
                    "Ik wil graag een 2 sterren suite boeken. Ik heb mijn studentenkaart bij me, kan ik korting krijgen?",
                    "Ik wil graag een 2 sterren suite boeken. Ik ben een vaste klant, is er een loyaliteitskorting voor mij?",
                    "Ik wil graag last minute een 2 sterren suite boeken. Ik boek op het laatste moment, is er een last-minutekorting?",
                    "Ik wil graag een 2 sterren suite boeken. Ik heb geen kortingspas, gewoon de normale prijs graag."
            },
            // 3 sterren
            {
                    "Ik wil graag een 3 sterren suite boeken. Ik ben voltijds student, geldt hier ook studentenkorting?",
                    "Ik wil graag een 3 sterren suite boeken. Ik heb al meer dan 10 keer bij jullie geboekt, geldt er een korting?",
                    "Ik wil graag last minute een 3 sterren suite boeken. Ik heb een kamer nodig voor vanavond, is er nog korting?",
                    "Ik wil graag een 3 sterren suite boeken. Ik val niet onder de beschikbare kortingen, ik betaal de volle prijs."
            },
            // 4 sterren
            {
                    "Ik wil graag een 4 sterren suite boeken. Ik studeer nog, is er studentenkorting beschikbaar?",
                    "Ik wil graag een 4 sterren suite boeken. Ik kom hier al jaren, is er loyaliteitskorting mogelijk?",
                    "Ik wil graag last minute een 4 sterren suite boeken. Ik boek spontaan, is er een last-minute aanbieding?",
                    "Ik wil graag een 4 sterren suite boeken. Geen korting nodig, gewoon de standaard prijs is goed."
            },
            // 5 sterren
            {
                    "Ik wil graag een 5 sterren suite boeken. Ik ben student, mag ik ook studentenkorting op deze luxe kamer?",
                    "Ik wil graag een 5 sterren suite boeken. Ik ben een trouwe klant, is er loyaliteitskorting mogelijk?",
                    "Ik wil graag last minute een 5 sterren suite boeken. Is er nog een last-minute aanbieding beschikbaar?",
                    "Ik wil graag een 5 sterren suite boeken. Ik val niet onder kortingen, ik betaal gewoon de volle prijs."
            }
    };

    public ReceptieScherm(){
        //dialoog opbouwen met random gekoen sterren en dialoog
        dialoog = new JLabel("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");

        kortingFrame.setTitle("ReceptieScherm");
        kortingFrame.setMinimumSize(new Dimension(800, 650));
        kortingFrame.pack();
        kortingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        kortingFrame.setResizable(false);
        kortingFrame.setLocationRelativeTo(null);

        //fout label kleur
        foutLabel.setForeground(Color.RED);

        //center
        foutLabel.setHorizontalAlignment(SwingConstants.CENTER);

        //kortingen buttons toevoegen
        kortingScherm.add(studentenKorting);
        kortingScherm.add(loyaliteitskorting);
        kortingScherm.add(lastMinuteKorting);
        kortingScherm.add(geenKorting);

        //korting panel grootte
        kortingScherm.setPreferredSize(new Dimension(600, 300));

        //paneel om de knoppen heen
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(kortingScherm, BorderLayout.CENTER);

        //ruimte rondom de salo label
        saldo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        //de euro linksboven hebben met een nieuwe panel north, appart paneel
        JPanel saldoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saldoPanel.add(saldo);

        //dialoog in midden
        dialoog.setHorizontalAlignment(SwingConstants.CENTER);
        dialoog.setVerticalAlignment(SwingConstants.CENTER);

       // paneel voor de dialoog met foutlabel eronder
        JPanel noordPanel = new JPanel(new BorderLayout());
        noordPanel.add(dialoog, BorderLayout.CENTER);
        noordPanel.add(foutLabel, BorderLayout.SOUTH);


        kortingFrame.setLayout(new BorderLayout());
        kortingFrame.add(saldoPanel, BorderLayout.NORTH);
        kortingFrame.add(noordPanel, BorderLayout.CENTER);
        kortingFrame.add(wrapper, BorderLayout.SOUTH);

        //venster ichtbaar
        kortingFrame.setVisible(true);

        //knopen direct vullen bij openen
        verversen();

        //timer die elke 500ms ververst
        timer = new Timer(500, e -> SwingUtilities.invokeLater(this::verversen));
        timer.start();

        // luisteren wanneer het venster gesloten wordt
        kortingFrame.addWindowListener( new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                //timer opnieuw starten wanneer dat gebeurt
                timer.start();
            }
        });

        studentenKorting.addActionListener(e -> {
            // check of de klant daadwerkelijk recht heeft op studentenkorting
            if(randomDialoog == 0){
                // foutmelding leegmaken
                foutLabel.setText("");

                // korting toepassen via de factory
                new KortingFactory("StudentenKorting",this, randomSterren);

                // nieuwe random klant kiezen voor de volgende ronde
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);

                //dialoog tekst bijwerken met de nieuwe klant
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                // foutmelding tonen als de klant geen recht heeft op deze korting
                foutLabel.setText("Deze klant is GEEN student en heeft geen recht op studentenkorting!");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        loyaliteitskorting.addActionListener(e -> {
            // check of de klant daadwerkelijk recht heeft op loyaliteitskorting
            if(randomDialoog == 1){
                // foutmelding leegmaken
                foutLabel.setText("");

                //korting toepassem
                new KortingFactory("LoyaliteitsKorting",this, randomSterren);

                //nieuwe random klant kiezen voor de volgende ronde
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);

                //dialoog tekst bijwerken met de nieuwe klant
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                // foutmelding tonen als de klant geen recht heeft op deze korting
                foutLabel.setText("Deze klant heeft GEEN loyaliteitskorting!");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        lastMinuteKorting.addActionListener(e -> {
            // check of de klant daadwerkelijk recht heeft op lastminutekorting
            if(randomDialoog == 2){
                //fout label leeg maken
                foutLabel.setText("");

                //korting toepassen
                new KortingFactory("LastMinuteKorting",this, randomSterren);

                //nieuwe random klant kiezen voor de volgende ronde
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);

                //dialoog tekst bijwerken met de nieuwe klant
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");

            } else {
                // foutmelding tonen als de klant geen recht heeft op deze korting
                foutLabel.setText("Deze klant heeft GEEN lastminutekorting");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        geenKorting.addActionListener(e -> {
            // check of de klant daadwerkelijk geen recht heeft op korting
            if(randomDialoog == 3){

                //fout label legen
                foutLabel.setText("");

                //korting toepassen
                new KortingFactory("GEENKORTING",this, randomSterren);

                //nieuwe random klant kiezen voor de volgende ronde
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);

                //dialoog tekst bijwerken met de nieuwe klant
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                // foutmelding tonen als de klant geen recht heeft op deze korting
                foutLabel.setText("Deze klant heeft WEL recht op korting");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });
    }

    public void receptie(double kortingFactor, int sterrenAantal){
        //saldo zetten tijdelijk
        saldo.setText("Totale hotel Saldo €");
        this.saldoBerekenen = saldoBerekenen + prijsKamers[sterrenAantal] * kortingFactor;
        saldoString = String.valueOf(saldoBerekenen);
        saldo.setText("Totale hotel Saldo € "+saldoString);
    }

    public void verversen(){
        //oude knoppen verwijderen
        kortingScherm.removeAll();

        //knoppen toevoeen
        kortingScherm.add(studentenKorting);
        kortingScherm.add(loyaliteitskorting);
        kortingScherm.add(lastMinuteKorting);
        kortingScherm.add(geenKorting);

        //paneel opnieuw laten teken
        kortingScherm.revalidate();
        kortingScherm.repaint();
    }

    public void sluit() {
        //timer stoppen
        timer.stop();

        //venster vrijlatem voor geheugen
        kortingFrame.dispose();
    }

    //kortingFrame ophalen
    public JFrame getKortingFrame() {
        return kortingFrame;
    }
}
