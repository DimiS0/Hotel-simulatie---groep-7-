package hotelsimulator.core;

import javax.swing.*;

import hotelevents.HotelEventManager;
import hotelsimulator.config.HTE;
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
	private Evenement event;
	private HTE hte;
	private SimulatieConfig config;

    //voorkomt dat de eventmanager stopt als hij nog niet gestart is
    private boolean eventManagerGestart = false;

    public HoofdSimulator() {
		this.config = new SimulatieConfig();
        this.hotel = new Hotel(config,eventManager,config);
        this.swingGui = new StarterGui(this);
		this.eventManager = new hotelevents.HotelEventManager();
        this.event = new Evenement(eventManager,hotel);

        CleaningEmergency.setHotel(hotel);

		config.addListener(() -> {
			eventManager.setHte(config.getSnelheid().getDelay());
		});
	}

	public void start() {
		swingGui.guiStart();

	}

    public HotelEventManager getEventManager() {
        return eventManager;
    }

    public void laadStandaardLayout() {
        try {
            String layoutStr = new LayoutLader().laadVanResources("/hotel_layout.json");
            hotel.maakHotelLayout(layoutStr);
            System.out.println("Standaard layout succesvol ingeladen.");
        } catch (IOException e) {
            System.out.println("Fout bij laden standaard layout: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Het standaard layoutbestand kan niet worden geladen.", "Fout",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        HotelGui gui = new HotelGui(hotel, config, eventManager,this);
        gui.showGui();

        eventManager.register(evt -> {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    hotel.getLift().liftBwegen();
                    gui.repaint();
                });
        });
        eventManager.start(config.getScenario());
        System.out.println(config.getScenario());
    }
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


        eventManager.register(evt -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                hotel.getLift().liftBwegen();
            });
        });

        eventManager.start(scenarioNummer);
        eventManagerGestart = true;
    }

    public void LayoutKiezer() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Layout Bestanden (*.json, *.layout)", "json", "layout"));

        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("Geen bestand gekozen");
            return;
        }

        try {
            String layoutStr = new LayoutLader().laadVanBestand(fileChooser.getSelectedFile());
            hotel.maakHotelLayout(layoutStr);
            System.out.println("Custom layout succesvol ingeladen.");
        } catch (IOException e) {
            System.out.println("Bestand niet gevonden: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Het gekozen layoutbestand kan niet worden geladen.", "Fout",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        HotelGui gui = new HotelGui(hotel, config, eventManager, this);
        gui.showGui();

        eventManager.register(evt -> {
            if (evt.getEventType() == hotelevents.HotelEventType.NONE) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    hotel.getLift().liftBwegen();
                    gui.repaint();
                });
            }
        });
        eventManager.start(config.getScenario());
    }

	public SimulatieConfig getConfig() {
		return config;
	}
}