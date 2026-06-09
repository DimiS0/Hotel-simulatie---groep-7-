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
        //voor dat je tekent moet je eerst scherm verschonen
        super.paintComponent(g);

        //hoogte en breedte ophalen +4 voor de 2 legeruimtes + trap en schacht
        int breedte = hotel.getMaxBreedte() + 4;

        // +2 voor de 2 lege ruimtes
        int hoogte  = hotel.getMaxHoogte() + 2;

        // Horizontale lijnen tekenen
        for (int i = 0; i <= hoogte; i++) {

            //kleur instellen van de lijnen
            g.setColor(Color.LIGHT_GRAY);

            //lijnen tekenen y veranderdt, x blijft op dezelfde plek van 0 tot breedte
            g.drawLine(0, i * cellSize, breedte * cellSize, i * cellSize);
        }

        // Verticale lijnen tekenen
        for (int i = 0; i <= breedte; i++) {

            //kleur instellen
            g.setColor(Color.LIGHT_GRAY);

            //lijnen tekenen x veranderdt, y blijft op dezelfde plek van 0 tot hoogte
            g.drawLine(i * cellSize, 0, i * cellSize, hoogte * cellSize);

            //elke kamer heen loopen en laten printen (polymorfisme)
            for (HotelRuimte r : hotel.getRuimtes()) {
                r.print(g, cellSize);
            }
        }
        //personen ophalen als het niet null is dan voor elke persoon printen
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
            //layout beheer methode om layout opteslaan met naam
            LayoutBeheer.layoutOpslaanInMap();
        });

        layoutInladenKnop.addActionListener(e -> {
            try {
                //gebruiker vragen om layout te kiezen
                String tekst = "";
                List<OpgeslagenLayouts> layouts = OpgeslagenLayouts.laadLayoutsUitMap();

                //geen layouts, foutmelding geven en niks doen
                if (layouts.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Er zijn geen layouts opgeslagen in de map!");
                    return;
                }

                //Toon een lijst met alle layouts (JList maakt een duidelijke selectie voor gebruiker)
                JList  <OpgeslagenLayouts> lijst = new JList<>(layouts.toArray(new OpgeslagenLayouts[0]));

                int resultaat = JOptionPane.showConfirmDialog(null,new JScrollPane(lijst), "Kies layout", JOptionPane.OK_CANCEL_OPTION);

                //op oke clicken gaan we door
                if (resultaat == JOptionPane.OK_OPTION) {
                    OpgeslagenLayouts gekozenLayout = lijst.getSelectedValue();

                    //niks gekozen? dan foutmelding
                    if (gekozenLayout == null) {
                        JOptionPane.showMessageDialog(null, "Er is geeb geselecteerde layout");
                        return;
                    }
                    //layout string ophalen van de gekozen layout
                    String layoutStr = gekozenLayout.getLayoutStr();

                    //ruimte vrij maken door de frame weg te doen
                    if (overzicht != null) {
                        overzicht.dispose();
                        overzicht = null;
                    }

                    //stoppen van de simulatie en het hotel reseten
                    simulatieLus.stop();
                    hotel.reset();

                    //nieuwe hotel maken van de selecteerde layout string
                    hotel.maakHotelLayout(layoutStr);

                    //herstart de hoofdsimulator met de bijgehoude scenario nummer
                    hoofdSimulator.herstart(hoofdSimulator.getConfig().getScenario());

                    //paneel op groote maken voor de nieuwe hotel layout altijd + 4 en + 2 zie uitleg aan het begin van hotel gui bij het tekenen
                    setPreferredSize(new Dimension((hotel.getMaxBreedte() + 4) * cellSize, (hotel.getMaxHoogte() + 2) * cellSize));
                    revalidate();
                    repaint();

                    //hotelevent manager ophalen omdat we dezelfde manager willen waar hoofdsimualtor op werkt
                    hotelEventManager = hoofdSimulator.getEventManager();

                    //schoonmakers activeren met for loopje
                    for (Persoon p : hotel.getPersonen()) {
                        if (p instanceof Schoonmaker schoonmaker) {
                            schoonmaker.activeer();
                        }
                    }

                    //nieuwe simulatie lus maken en starten
                    simulatieLus = new SimulatieLus(hotel, this);
                    simulatieLus.start();
                    repaint();
                }

            } catch (IOException ex) {
                // als er een crash is opvangen en niks doen
                throw new RuntimeException(ex);
            }

        });

        instellingenBtn.addActionListener(e -> {
            //Als configGui niet bestaat of niet zichtbaar is, maak een nieuw ConfigGui
            if (configGui == null || !configGui.getFrame().isDisplayable()) {
                configGui = new ConfigGui(config, value -> updateSpeedLabel(value));
            } else {
                //als config gui bestaat, haal venster naar voren, dus boven hotelGUI
                configGui.getFrame().toFront();
            }
        });

        // Pas het venster aan zodat alle elementen precies passen + niet vergrootbaar
        frame.pack();
        frame.setResizable(false);

        // Zet het venster in het midden van het beeldscherm
        frame.setLocationRelativeTo(null);

        // Lees de snelheid uit config zodat de instelling van StarterGui meegenomen wordt
        updateSpeedLabel(mapHTEToSlider(config.getSnelheid()));

        // Maak alle personen aan (gasten + schoonmakers)
        hotel.maakPersonen(config.getAantalGasten());
        frame.setVisible(true);

        // Activeer alle schoonmakers direct bij de start, zij zijn al aanwezig in het hotel
        for (Persoon p : hotel.getPersonen()) {
            if (p instanceof Schoonmaker schoonmaker) {
                schoonmaker.activeer();
            }
        }

        // Spawn gasten één voor één via de timer elke seconde of zelf aanstelbaar in simulatie lus
        simulatieLus = new SimulatieLus(hotel, this);
        simulatieLus.start();

        // Klik op de lobby om het overzichtvenster te openen of te sluiten
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {

                //pixels omzetten naar grid coordinatem
                int gridX = e.getX() / cellSize;
                int gridY = e.getY() / cellSize;

                //loop voor lobby
                for (HotelRuimte r : hotel.getRuimtes()) {
                    if (r instanceof hotelsimulator.ruimtes.Lobby) {

                        // zelfde offset als in Lobby.print()
                        int lobbyX = r.getX() + 1;
                        int lobbyY = r.getY() - 1;

                        //kijken of de klik in de lobby valt
                        if (gridX >= lobbyX && gridX < lobbyX + r.getBreedte() && gridY >= lobbyY && gridY < lobbyY + r.getHoogte()) {

                            //Als overzicht niet bestaat of niet zichtbaar is, maak het
                            if (overzicht == null || !overzicht.isVisible()){
                                hotelEventManager.pauze();
                                simulatieLus.stop();

                                //het overzichtvenster maken
                                overzicht = new HotelOverzicht(hotel,hotelEventManager);

                                //Als gebruiker het venster sluit, herstart de simulatie
                                overzicht.addWindowListener(new java.awt.event.WindowAdapter() {
                                    public void windowClosing(java.awt.event.WindowEvent e) {
                                        //unpauze, simulatie start
                                        hotelEventManager.pauze();
                                        simulatieLus.start();
                                        overzicht = null;
                                    }
                                });
                            }
                            else{
                                // Als overzicht al zichtbaar is, sluit het en herstart
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