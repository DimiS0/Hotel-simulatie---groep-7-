package hotelsimulator.gui;

import hotelsimulator.config.HTE;
import hotelsimulator.core.Hotel;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.personen.Gast;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.personen.Persoon;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static hotelsimulator.config.HTE.*;

public class HotelGui extends JPanel {

    private Hotel hotel;
    private SimulatieConfig config;
    private final int cellSize = 50;
    private ConfigGui configGui;
    private JFrame frame;
    private JLabel speed;
    boolean setDefaultSpeed;
    private int liftTeller = 0;
    private final int[] LIFT_STOPS = {8, 5, 2}; // Grid-Y posities van de lifthaltes
    private int liftStopIndex = 0;
    public HotelGui(Hotel hotel, SimulatieConfig config) {
        this.frame = new JFrame("Hotel Layout");
        this.hotel = hotel;
        this.config = config;
        this.setDefaultSpeed = false;
        this.speed = new JLabel("1x");
        setPreferredSize(new Dimension(10 * cellSize, 10 * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        //maak scherm eerst leeg voordat je tekent
        super.paintComponent(g);

        //tekent horizontaale en verticale lijnen wat ons rooster is
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 10; i++) {
            g.drawLine(0, i * cellSize, 10 * cellSize, i * cellSize);
            g.drawLine(i * cellSize, 0, i * cellSize, 10 * cellSize);
        }

        //loopt door elke ruimte heen en zegt dat hij zichzelf moet printen
        //java kiest automatisch de juiste print op basis van het kamertype
        for (HotelRuimte r : hotel.getRuimtes()) {
            r.print(g, cellSize);
        }
        if (hotel.getPersonen() != null) {
            for (Persoon p : hotel.getPersonen()) {
                p.print(g);
            }
        }
    }

    public void showGui() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        frame.add(this, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton instellingenBtn = new JButton("Instellingen");

        topPanel.add(instellingenBtn);
        topPanel.add(speed);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(speed, BorderLayout.SOUTH);

        instellingenBtn.addActionListener(e -> new ConfigGui(config, value -> updateSpeedLabel(value)));

        HotelOverzicht overzicht = new HotelOverzicht(hotel);
        frame.add(overzicht, BorderLayout.SOUTH);
        //frame pack past grootte aan van venster zodat alle knoppen precies passen
        frame.pack();

        //zet het venster in het midden van de scherm, null -> in het midden van je hele beeldscherm
        frame.setLocationRelativeTo(null);

        //bij het openen van hotelGui lezen we snelheid van config, want zodat snelheid in starterGui meegnomen wordt.
        updateSpeedLabel(mapHTEToSlider(config.getSnelheid()));

        frame.setVisible(true);

        hotel.maakPersonen(config.getAantalGasten());

        // Spawn één gast per 2 seconden vanuit de lobby
        List<Persoon> personen = hotel.getPersonen();
        final int[] spawnIndex = {0};

        Timer spawnTimer = new Timer(2000, ev -> {
            while (spawnIndex[0] < personen.size()) {
                Persoon p = personen.get(spawnIndex[0]);
                if (p instanceof Gast gast && !gast.isGespawnd()) {
                    gast.activeer();
                    spawnIndex[0]++;
                    break;
                }
                spawnIndex[0]++;
            }
        });
        spawnTimer.start();

        // Bewegingstimer: ~60 FPS
        Timer bewegingsTimer = new Timer(16, ev -> {
            // Lift laten rijden (partner's code)
            // Elke ~5 seconden de lift naar volgende verdieping sturen
            liftTeller++;
            if (liftTeller >= 300) {
                liftTeller = 0;
                int volgendeStop = LIFT_STOPS[liftStopIndex % LIFT_STOPS.length];

                // Eerst aan wachtrij toevoegen, dan lift starten
                hotel.getLiftOproepen().add(volgendeStop);
                if (hotel.getLift().getBeschikbaar()) {
                    hotel.getLift().roepLiftNaar(volgendeStop);
                }
                liftStopIndex++;
            }
            for (Persoon p : hotel.getPersonen()) {
                if (p instanceof Gast gast) {
                    gast.update();
                } else {
                    p.beweeg();
                }
            }
            repaint();
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int gridX = e.getX() / cellSize;
                int gridY = e.getY() / cellSize;

                for (HotelRuimte r : hotel.getRuimtes()) {
                    if (r instanceof hotelsimulator.ruimtes.Lobby) {
                        // zelfde offset als in Lobby.print()
                        int lobbyX = r.getX() + 1;
                        int lobbyY = r.getY() - 1;
                        if (gridX >= lobbyX && gridX < lobbyX + r.getBreedte() &&
                                gridY >= lobbyY && gridY < lobbyY + r.getHoogte()) {
                            overzicht.setVisible(!overzicht.isVisible());
                        }
                    }
                }
            }
        });
        bewegingsTimer.start();
    }

    public void updateSpeedLabel(int value) {
        switch (value) {
            case 1 -> speed.setText("0.25x");
            case 2 -> speed.setText("0.50x");
            case 3 -> speed.setText("1.0x");
            case 4 -> speed.setText("2.0x");
            case 5 -> speed.setText("4.0x");
        }
        frame.remove(speed);
        frame.add(speed, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
    }

    // vertaalt HTE waarde naar getal, dus snel -> 4 en die wordt 2.0x
    private int mapHTEToSlider(HTE hte) {
        return switch (hte) {
            case LANGZAMER -> 1;
            case LANGZAAM  -> 2;
            case NORMAAL   -> 3;
            case SNEL      -> 4;
            case VIER_X    -> 5;
            default        -> 3;
        };
    }
}