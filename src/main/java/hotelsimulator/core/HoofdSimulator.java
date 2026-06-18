package hotelsimulator.core;

import javax.swing.*;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.events.Core.Evenement;
import hotelsimulator.gui.HotelGui;
import hotelsimulator.gui.StarterGui;
import hotelsimulator.events.Emergency.CleaningEmergency;


import java.io.IOException;



public class HoofdSimulator {
	private hotelevents.HotelEventManager eventManager;
	private Hotel hotel;
	private StarterGui swingGui;
	@SuppressWarnings("unused")
    private Evenement event;
	private SimulatieConfig config;

    //voorkomt dat de eventmanager stopt als hij nog niet gestart is
    private boolean eventManagerGestart = false;

    public HoofdSimulator() {
		this.config = new SimulatieConfig();
        this.hotel = new Hotel(eventManager,config);
        this.swingGui = new StarterGui(this);
		this.eventManager = new hotelevents.HotelEventManager();
        this.event = new Evenement(eventManager,hotel);

        CleaningEmergency.setHotel(hotel);

        // registreren bij observer om de snelheid te veranderen
		config.addListener(() -> {
			eventManager.setHte(config.getSnelheid().getDelay());
		});
	}


    // start de applicatie scherm
	public void start() {
		swingGui.guiStart();

	}

    //getter om de event manager te halen
    public HotelEventManager getEventManager() {
        return eventManager;
    }


    public void laadStandaardLayout() {
        try {
            //lees het bestand in de resources folder
            String layoutStr = new LayoutLader().laadVanResources("/hotel_layout.json");

            //maak objecten met hulp van het bestand
            hotel.maakHotelLayout(layoutStr);

            //de string opslaan voor hergebruik van de layout in Layouts map
            String naam = "Standaard Layout 1";
            OpgeslagenLayouts layout = new OpgeslagenLayouts(naam, layoutStr);
            layout.layoutsInMapStoppen();

            //debug om te kijken of dit goed gaat
            System.out.println("Standaard layout succesvol ingeladen.");

            //crashes opvangen
        } catch (IOException e) {

            //debug voor ons
            System.out.println("Fout bij laden standaard layout: " + e.getMessage());

            //dialoog laten zien
            JOptionPane.showMessageDialog(null,
                    "Het standaard layoutbestand kan niet worden geladen.", "Fout",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        //als alles goed is gegaan maken we een nieuwe scherm waar de hotel op komt
        HotelGui gui = new HotelGui(hotel, config, eventManager,this);
        gui.showGui();

        //timer voor onze simulatie, wat we willen dat er elke tick gebeurt
        eventManager.register(evt -> {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    hotel.getLift().liftBwegen();
                    gui.repaint();
                });
        });

        //de juiste scenario inladen
        eventManager.start(config.getScenario());
        eventManagerGestart = true;
        //debug om te kijken welke scenario we hebben
        System.out.println(config.getScenario());
    }

    // herstart methode om te zorgen dat we layouts weer kunnen inladen ipv opniewu opstarten van aplicatie
    public void herstart(int scenarioNummer) {

        // Stop de huidige simulatie
        if (eventManagerGestart) {
            eventManager.stop();
            eventManagerGestart = false;
        }

        // Maak nieuwe personen aan
        hotel.maakPersonen(config.getAantalGasten());


        // Start nieuwe simulatie met gekozen scenario
        eventManager = new hotelevents.HotelEventManager();
        event = new Evenement(eventManager, hotel);
        CleaningEmergency.setHotel(hotel);

        // Timer weer aanmaken
        eventManager.register(evt -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                hotel.getLift().liftBwegen();
            });
        });

        //scenario nummer meegeven
        eventManager.start(scenarioNummer);
        eventManagerGestart = true;
    }


    public void LayoutKiezer() {
        //objecten aanmaken om je File te kiezen
        JFileChooser fileChooser = new JFileChooser();

        //filteren op json, layout
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Layout Bestanden (*.json, *.layout)", "json", "layout"));

        int result = fileChooser.showOpenDialog(null);

        //als er geen bestand wordt gekozen
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("Geen bestand gekozen");
            return;
        }

        try {

            //file proberen te lezen
            String layoutStr = new LayoutLader().laadVanBestand(fileChooser.getSelectedFile());

            //kamers maken en opslaan met hulp van de file
            hotel.maakHotelLayout(layoutStr);

            //debug
            System.out.println("Custom layout succesvol ingeladen.");

        } catch (IOException e) {
            //crash catchen als hij het bestand niet kan vinden/laden

            //debug
            System.out.println("Bestand niet gevonden: " + e.getMessage());

            //dialoog zodat gebruiker weet dat de bestand verkeerd/ corrupt is
            JOptionPane.showMessageDialog(null,
                    "Het gekozen layoutbestand kan niet worden geladen.", "Fout",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //als alles goed gaat maken we een hscherm om de hotel te laten zien
        HotelGui gui = new HotelGui(hotel, config, eventManager, this);
        gui.showGui();

        //en een timer kopelen
        eventManager.register(evt -> {
            if (evt.getEventType() == hotelevents.HotelEventType.NONE) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    hotel.getLift().liftBwegen();
                    gui.repaint();
                });
            }
        });
        //met de scenario meegegeven die de gebruiker heeft gekozen
        eventManager.start(config.getScenario());
    }

    //getter om config meetegeven aan andere klasses
	public SimulatieConfig getConfig() {
		return config;
	}
}