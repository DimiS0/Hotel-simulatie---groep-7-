package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelsimulator.core.Hotel;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.personen.Persoon;
import hotelsimulator.config.Snelheid;
import hotelsimulator.core.SimulatieLus;

import javax.swing.*;
import java.awt.*;


import static hotelsimulator.config.HTE.*;

public class HotelGui extends JPanel {

    private Hotel hotel;
    private HotelEventManager hotelEventManager;
    private SimulatieConfig config;
    private final int cellSize = 50;
    private ConfigGui configGui;
    private JFrame frame;
    private JLabel speed;
    boolean setDefaultSpeed;
    private HotelOverzicht overzicht;
    private SimulatieLus simulatieLus;

    public HotelGui(Hotel hotel, SimulatieConfig config, HotelEventManager eventManager) {
        this.frame = new JFrame("Hotel Layout");
        this.hotel = hotel;
        this.hotelEventManager = eventManager;
        this.config = config;
        this.setDefaultSpeed = false;
        this.speed = new JLabel("1x");
        setPreferredSize(new Dimension(10 * cellSize, 10 * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Maak het scherm eerst leeg voordat er opnieuw getekend wordt
        super.paintComponent(g);

        // Teken het rooster van horizontale en verticale lijnen
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 10; i++) {
            g.drawLine(0, i * cellSize, 10 * cellSize, i * cellSize);
            g.drawLine(i * cellSize, 0, i * cellSize, 10 * cellSize);
        }

        // Laat elke ruimte zichzelf tekenen (Java kiest automatisch de juiste print-methode)
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

        instellingenBtn.addActionListener(e -> {
            if (configGui == null || !configGui.getFrame().isDisplayable()) {
                configGui = new ConfigGui(config, value -> updateSpeedLabel(value));
            } else {
                configGui.getFrame().toFront();
            }
        });

        // Pas het venster aan zodat alle elementen precies passen
        frame.pack();

        // Zet het venster in het midden van het beeldscherm
        frame.setLocationRelativeTo(null);

        // Lees de snelheid uit config zodat de instelling van StarterGui meegenomen wordt
        updateSpeedLabel(mapHTEToSlider(config.getSnelheid()));

        // Maak alle personen aan (gasten + schoonmakers)
        hotel.maakPersonen(config.getAantalGasten());
        frame.setVisible(true);

        // Activeer alle schoonmakers direct bij de start — zij zijn al aanwezig in het hotel
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof Schoonmaker schoonmaker) {
                schoonmaker.activeer();
            }
        }

        // Spawn gasten één voor één via de timer (elke 2 seconden één gast)
        simulatieLus = new SimulatieLus(hotel, this);
        simulatieLus.start();

        // Klik op de lobby om het overzichtvenster te openen of te sluiten
        this.addMouseListener(new java.awt.event.MouseAdapter() {
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
                            if (overzicht == null || !overzicht.isVisible()){
                                hotelEventManager.pauze();
                                simulatieLus.stop();
                                overzicht = new HotelOverzicht(hotel,hotelEventManager);
                            }
                            else{
                                hotelEventManager.pauze();
                                simulatieLus.start();
                                overzicht.dispose();
                                overzicht = null;
                            }


                        }
                    }
                }
            }
        });


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

    // Vertaalt HTE-waarde naar een getal voor het snelheidslabel
    private int mapHTEToSlider(Snelheid hte) {
        double factor = hte.getFactor();
        if (factor <= 0.25) return 1;
        if (factor <= 0.5)  return 2;
        if (factor <= 1.0)  return 3;
        if (factor <= 2.0)  return 4;
        return 5;
    }
}