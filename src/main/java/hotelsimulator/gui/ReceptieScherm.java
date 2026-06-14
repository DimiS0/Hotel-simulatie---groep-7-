package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelsimulator.core.Hotel;
import hotelsimulator.core.SimulatieLus;
import hotelsimulator.korting.KortingFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class ReceptieScherm extends JFrame {
    private JFrame kortingFrame = new JFrame();
    private JPanel kortingScherm = new JPanel(new GridLayout(2,2));
    private JLabel saldo = new JLabel("€ "+"0");
    private String saldoString = "";
    private Timer timer;

    private JButton studentenKorting = new JButton("StudentenKorting");
    private JLabel infoKaart;
    private JButton loyaliteitskorting = new JButton("LoyaliteitsKorting");
    private JButton lastMinuteKorting = new JButton("LastMinuteKorting");
    private JButton geenKorting = new JButton("GEEN KORTING");
    private Dimension buttonSize = new Dimension(400,150);
    private int randomSterren = new Random().nextInt(0,5);
    private int randomDialoog = new Random().nextInt(0,4);
    private JLabel foutLabel = new JLabel();
    private JLabel dialoog;
    private SimulatieLus simulatieLus;
    private double [] prijsKamers = {100.0,150.0,200.0,250.0,300.0};
    private String [] prijsKamersString;
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
        prijsKamersString =  new String[prijsKamers.length];
        for(int i = 0; i < prijsKamers.length; i++){
            prijsKamersString[i] = String.valueOf(prijsKamers[i]);
        }
        dialoog = new JLabel("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
        infoKaart = new JLabel(
                "<html>" +
                        "kamerprijs<br>" +
                        "1 ster € " + prijsKamersString[0] + "<br>" +
                        "2 ster € " + prijsKamersString[1] + "<br>" +
                        "3 ster € " + prijsKamersString[2] + "<br>" +
                        "4 ster € " + prijsKamersString[3] + "<br>" +
                        "5 ster € " + prijsKamersString[4] +
                        "</html>"
        );

        kortingFrame.setTitle("ReceptieScherm");
        kortingFrame.setMinimumSize(new Dimension(800, 650));
        kortingFrame.pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        foutLabel.setForeground(Color.RED);
        foutLabel.setHorizontalAlignment(SwingConstants.CENTER);

        kortingScherm.add(studentenKorting);
        kortingScherm.add(loyaliteitskorting);
        kortingScherm.add(lastMinuteKorting);
        kortingScherm.add(geenKorting);
        kortingScherm.setPreferredSize(new Dimension(600, 300));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(kortingScherm, BorderLayout.CENTER);

        JPanel EastPanel = new JPanel(new BorderLayout());
        EastPanel.add(infoKaart, BorderLayout.EAST);
        EastPanel.setBorder(BorderFactory.createEmptyBorder(10,20,70,10));
        saldo.setBorder(BorderFactory.createEmptyBorder(10,20,0,0));
        dialoog.setBorder(BorderFactory.createEmptyBorder(0,200,0,0));

        JPanel noordPanel = new JPanel(new GridLayout(4, 1));
        noordPanel.setPreferredSize(new Dimension(400, 100));
        noordPanel.add(saldo);
        noordPanel.add(dialoog);
        noordPanel.add(foutLabel);

        kortingFrame.setLayout(new BorderLayout());
        kortingFrame.add(noordPanel, BorderLayout.CENTER);
        kortingFrame.add(wrapper, BorderLayout.SOUTH);
        kortingFrame.add(EastPanel,BorderLayout.EAST);

        kortingFrame.setVisible(true);

        verversen();

        timer = new Timer(500, e -> SwingUtilities.invokeLater(this::verversen));
        timer.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                timer.start();
            }
        });

        studentenKorting.addActionListener(e -> {
            if(randomDialoog == 0){
                foutLabel.setText("");
                new KortingFactory("StudentenKorting",this, randomSterren);
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                foutLabel.setText("Deze klant is GEEN student en heeft geen recht op studentenkorting!");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        loyaliteitskorting.addActionListener(e -> {
            if(randomDialoog == 1){
                foutLabel.setText("");
                new KortingFactory("LoyaliteitsKorting",this, randomSterren);
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                foutLabel.setText("Deze klant heeft GEEN loyaliteitskorting!");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        lastMinuteKorting.addActionListener(e -> {
            if(randomDialoog == 2){
                foutLabel.setText("");
                new KortingFactory("LastMinuteKorting",this, randomSterren);
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                foutLabel.setText("Deze klant heeft GEEN lastminutekorting");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });

        geenKorting.addActionListener(e -> {
            if(randomDialoog == 3){
                foutLabel.setText("");
                new KortingFactory("GEENKORTING",this, randomSterren);
                randomDialoog = new Random().nextInt(0,4);
                randomSterren = new Random().nextInt(0,5);
                dialoog.setText("<html><div style='width: 300px'>" + klantenDialoog[randomSterren][randomDialoog] + "</div></html>");
            } else {
                foutLabel.setText("Deze klant heeft WEL recht op korting");
                kortingFrame.revalidate();
                kortingFrame.repaint();
            }
        });
    }

    public void receptie(double kortingFactor, int sterrenAantal){
        saldo.setText("€");
        this.saldoBerekenen = saldoBerekenen + prijsKamers[sterrenAantal] * kortingFactor;
        saldoString = String.valueOf(saldoBerekenen);
        saldo.setText("€ "+saldoString);
    }

    public void verversen(){
        kortingScherm.removeAll();
        kortingScherm.add(studentenKorting);
        kortingScherm.add(loyaliteitskorting);
        kortingScherm.add(lastMinuteKorting);
        kortingScherm.add(geenKorting);

        kortingScherm.revalidate();
        kortingScherm.repaint();
    }

    public void sluit() {
        timer.stop();
        kortingFrame.dispose();
    }

    public JFrame getKortingFrame() {
        return kortingFrame;
    }
}
