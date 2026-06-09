package hotelsimulator.gui;

import hotelevents.HotelEventManager;
import hotelsimulator.core.*;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.personen.Persoon;
import hotelsimulator.config.Snelheid;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;


import java.util.List;

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
    private HoofdSimulator hoofdSimulator;


    public HotelGui(Hotel hotel, SimulatieConfig config, HotelEventManager eventManager, HoofdSimulator hoofdSimulator) {
        this.frame = new JFrame("Hotel Layout");
        this.hotel = hotel;
        this.hotelEventManager = eventManager;
        this.hoofdSimulator = hoofdSimulator;
        this.config = config;
        this.setDefaultSpeed = false;
        this.speed = new JLabel("1x");


// preferredSize in constructor:
        setPreferredSize(new Dimension((hotel.getMaxBreedte() + 4) * cellSize, (hotel.getMaxHoogte() + 2) * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int breedte = hotel.getMaxBreedte() + 4;
        int hoogte  = hotel.getMaxHoogte() + 2;

        // Horizontale lijnen
        for (int i = 0; i <= hoogte; i++) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0, i * cellSize, breedte * cellSize, i * cellSize);
        }

        // Verticale lijnen + ruimtes tekenen
        for (int i = 0; i <= breedte; i++) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(i * cellSize, 0, i * cellSize, hoogte * cellSize);

            for (HotelRuimte r : hotel.getRuimtes()) {
                r.print(g, cellSize);
            }
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

        // Wrap het hotel panel in een scrollpane
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setPreferredSize(new Dimension(10 * cellSize, 10 * cellSize));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(cellSize);
        scrollPane.getVerticalScrollBar().setUnitIncrement(cellSize);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton instellingenBtn = new JButton("Instellingen");
        JButton layoutPlusKnop = new JButton("+");
        JButton layoutInladenKnop = new JButton("Opgeslagen Layouts");

        topPanel.add(layoutPlusKnop);
        topPanel.add(layoutInladenKnop);
        topPanel.add(instellingenBtn);
        topPanel.add(speed);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(speed, BorderLayout.SOUTH);

        layoutPlusKnop.addActionListener(e -> {
            LayoutBeheer.layoutOpslaanInMap();
        });

        layoutInladenKnop.addActionListener(e -> {
            try {
                String tekst = "";
                List<OpgeslagenLayouts> layouts = OpgeslagenLayouts.laadLayoutsUitMap();

                if (layouts.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Er zijn geen layouts opgeslagen in de map!");
                    return;
                }
                //Jlist omdat  het een duidelijk selecteert, zodat de gebruiker ziet welke layout gekozen is
                JList  <OpgeslagenLayouts> lijst = new JList<>(layouts.toArray(new OpgeslagenLayouts[0]));
                int resultaat = JOptionPane.showConfirmDialog(null,new JScrollPane(lijst), "Kies layout", JOptionPane.OK_CANCEL_OPTION);

                if (resultaat == JOptionPane.OK_OPTION) {
                    OpgeslagenLayouts gekozenLayout = lijst.getSelectedValue();


                    if (gekozenLayout == null) {
                        JOptionPane.showMessageDialog(null, "Er is geeb geselecteerde layout");
                        return;
                    }
                    String layoutStr = gekozenLayout.getLayoutStr();

                    //ruimte vrij maken
                    if (overzicht != null) {
                        overzicht.dispose();
                        overzicht = null;
                    }

                    simulatieLus.stop();
                    hotel.reset();
                    hotel.maakHotelLayout(layoutStr);
                    hoofdSimulator.herstart(hoofdSimulator.getConfig().getScenario());

                    setPreferredSize(new Dimension((hotel.getMaxBreedte() + 4) * cellSize, (hotel.getMaxHoogte() + 2) * cellSize));
                    revalidate();
                    repaint();

                    hotelEventManager = hoofdSimulator.getEventManager();

                    for (Persoon p : hotel.getPersonen()) {
                        if (p instanceof Schoonmaker schoonmaker) {
                            schoonmaker.activeer();
                        }
                    }

                    simulatieLus = new SimulatieLus(hotel, this);
                    simulatieLus.start();
                    repaint();
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });

        instellingenBtn.addActionListener(e -> {
            if (configGui == null || !configGui.getFrame().isDisplayable()) {
                configGui = new ConfigGui(config, value -> updateSpeedLabel(value));
            } else {
                configGui.getFrame().toFront();
            }
        });

        // Pas het venster aan zodat alle elementen precies passen
        frame.pack();
        frame.setResizable(false);

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
    public void sluitVenster() {
        // Stop de bewegings- en spawntimer zodat gasten niet meer updaten
        // op een hotel dat al gereset is
        if (simulatieLus != null) {
            simulatieLus.stop();
        }
        // Sluit het venster en geeft geheugen vrij
        if (frame != null) {
            frame.dispose();
        }
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