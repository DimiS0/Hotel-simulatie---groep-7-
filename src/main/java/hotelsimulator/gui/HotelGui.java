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
    private ReceptieScherm receptieScherm;


    public HotelGui(Hotel hotel, SimulatieConfig config, HotelEventManager eventManager, HoofdSimulator hoofdSimulator) {
        this.frame = new JFrame("Hotel Layout");
        this.hotel = hotel;
        this.hotelEventManager = eventManager;
        this.hoofdSimulator = hoofdSimulator;
        this.config = config;
        this.setDefaultSpeed = false;
        this.speed = new JLabel("1x");


// preferredSize in constructor:
        setPreferredSize(new Dimension((hotel.getMaxBreedte() + 3) * cellSize, (hotel.getMaxHoogte() + 2) * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {

        //scherm leeg maken om te tekenen
        super.paintComponent(g);

        // breeedte en hoofte opslaan,
        int breedte = hotel.getMaxBreedte() + 3;
        int hoogte  = hotel.getMaxHoogte() + 2;

        // Horizontale lijnen tekenen, x blijft het zelfde, y veranderdt
        for (int i = 0; i <= hoogte; i++) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0, i * cellSize, breedte * cellSize, i * cellSize);
        }

        // Verticale lijnen tekenen, Y blijft het zelfde, x veranderd 1 opzij
        for (int i = 0; i <= breedte; i++) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(i * cellSize, 0, i * cellSize, hoogte * cellSize);

            //elke kamer zichzelf laten printen
            for (HotelRuimte r : hotel.getRuimtes()) {
                r.print(g, cellSize);
            }
        }
        //daarna persoonen printen
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
        JButton receptieKnop = new JButton("receptie");

        topPanel.add(layoutPlusKnop);
        topPanel.add(layoutInladenKnop);
        topPanel.add(instellingenBtn);
        topPanel.add(speed);
        topPanel.add(receptieKnop);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(speed, BorderLayout.SOUTH);

        //layout + knop action listener
        layoutPlusKnop.addActionListener(e -> {
            //layout beheer methode runnen
            LayoutBeheer.layoutOpslaanInMap();
        });

        //receptie minigame knop
        receptieKnop.addActionListener(e -> {

            //geen receptie scherm dan
            if (receptieScherm == null) {
                //pauze van beide timers
                hotelEventManager.pauze();
                simulatieLus.stop();

                //scherm aanmaken
                receptieScherm = new ReceptieScherm();

                //luisteren of het scherm gesloten is met het kruisje
                receptieScherm.getKortingFrame().addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {

                        //unpauzeren timers
                        hotelEventManager.pauze();
                        simulatieLus.start();

                        //receptie op null zetten
                        receptieScherm = null;
                    }
                });
                // Scherm is al open en de knop wordt opnieuw geklikt
            } else {

                //scherm sluiten
                receptieScherm.sluit();

                //timers starten
                hotelEventManager.pauze();
                simulatieLus.start();

                //receptie scherm op null ette
                receptieScherm = null;
            }
        });

        //layout inladen knop
        layoutInladenKnop.addActionListener(e -> {
            try {

                //lijst met de layouts ophalen uit de map
                List<OpgeslagenLayouts> layouts = OpgeslagenLayouts.laadLayoutsUitMap();

                //als het leeg is
                if (layouts.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Er zijn geen layouts opgeslagen in de map!");
                    return;
                }
                //JList omdat het een duidelijke selectie geeft, zodat de gebruiker ziet welke layout gekozen is
                JList  <OpgeslagenLayouts> lijst = new JList<>(layouts.toArray(new OpgeslagenLayouts[0]));

                // cancel en oke knoppen toevoegen
                int resultaat = JOptionPane.showConfirmDialog(null,new JScrollPane(lijst), "Kies layout", JOptionPane.OK_CANCEL_OPTION);

                //als er op oke is gedrukt
                if (resultaat == JOptionPane.OK_OPTION) {

                    //geselecteerde layout ophalen
                    OpgeslagenLayouts gekozenLayout = lijst.getSelectedValue();

                    //als de layout null is dan niks doen
                    if (gekozenLayout == null) {
                        JOptionPane.showMessageDialog(null, "Er is geeb geselecteerde layout");
                        return;
                    }

                    //de string ophalen om hotel
                    String layoutStr = gekozenLayout.getLayoutStr();

                    //ruimte vrij maken
                    if (overzicht != null) {
                        overzicht.dispose();
                        overzicht = null;
                    }

                    //simulatielus stoppen
                    simulatieLus.stop();

                    //hotel reste emthode aanropeen
                    hotel.reset();

                    //en een nieuwe hotel maken met de gekozen layout string
                    hotel.maakHotelLayout(layoutStr);

                    //de eventmanager herstarten met een scenario nummer
                    hoofdSimulator.herstart(hoofdSimulator.getConfig().getScenario());

                    //frame aanmaken met de juiste hoogte en breedte
                    setPreferredSize(new Dimension((hotel.getMaxBreedte() + 3) * cellSize, (hotel.getMaxHoogte() + 2) * cellSize));

                    //hertekenen
                    revalidate();
                    repaint();

                    //eventmanager opvragen om gekopeld te zijn
                    hotelEventManager = hoofdSimulator.getEventManager();

                    //schoonmakers activeren
                    for (Persoon p : hotel.getPersonen()) {
                        if (p instanceof Schoonmaker schoonmaker) {
                            schoonmaker.activeer();
                        }
                    }

                    //simulatie lus maken
                    simulatieLus = new SimulatieLus(hotel, this);

                    //simulatie starten
                    simulatieLus.start();
                    repaint();
                }

            } catch (IOException ex) {
                //crash opvangen en dialoog tonen implaats van een crash geven
                JOptionPane.showMessageDialog(null, "Fout bij het laden van layouts: " + ex.getMessage());
            }

        });

        //insteling button action lsistener
        instellingenBtn.addActionListener(e -> {

            //als het schemr null is  en frame is niet op display
            if (configGui == null || !configGui.getFrame().isDisplayable()) {

                //nieuwe gui aanmaken
                configGui = new ConfigGui(config, value -> updateSpeedLabel(value));

            } else {
                //als het al open is / displayable, scherm op de voorkant laten zien
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

        // Activeer alle schoonmakers direct bij de start, zij zijn al aanwezig in het hotel
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

                //lobby ophalen
                for (HotelRuimte r : hotel.getRuimtes()) {
                    if (r instanceof hotelsimulator.ruimtes.Lobby) {

                        // zelfde offset als in Lobby.print() methode
                        int lobbyX = r.getX() + 1;
                        int lobbyY = r.getY() - 1;

                        //kijken of er in de lobby is geklikt
                        if (gridX >= lobbyX && gridX < lobbyX + r.getBreedte() &&
                                gridY >= lobbyY && gridY < lobbyY + r.getHoogte()) {

                            //nog een check om te kijken of de overszicht zelf null is of niet visable
                            if (overzicht == null || !overzicht.isVisible()){
                                //timers op pause
                                hotelEventManager.pauze();
                                simulatieLus.stop();

                                //overicht maken
                                overzicht = new HotelOverzicht(hotel,hotelEventManager);

                                //overzicht kijken of die is gesloten met het kruisje
                                overzicht.addWindowListener(new java.awt.event.WindowAdapter() {
                                    public void windowClosing(java.awt.event.WindowEvent e) {
                                        //timers starten
                                        hotelEventManager.pauze();
                                        simulatieLus.start();

                                        //overicht op null zetten voor veiligheid
                                        overzicht = null;
                                    }
                                });
                            }
                            //als je weer op lobby klikt
                            else{

                                //timers starten
                                hotelEventManager.pauze();
                                simulatieLus.start();

                                //overicht verwijderen
                                overzicht.dispose();

                                //op null zetten om veilig te zijn
                                overzicht = null;
                            }


                        }
                    }
                }
            }
        });


    }

    //voor de hotelgui label
    public void updateSpeedLabel(int value) {
        switch (value) {
            case 1 -> speed.setText("Simulatie snelheid op 0.25x");
            case 2 -> speed.setText("Simulatie snelheid op 0.50x");
            case 3 -> speed.setText("Simulatie snelheid op 1.0x");
            case 4 -> speed.setText("Simulatie snelheid op 2.0x");
            case 5 -> speed.setText("Simulatie snelheid op 4.0x");
        }
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