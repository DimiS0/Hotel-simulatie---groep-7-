package hotelsimulator.core;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import hotelsimulator.config.HTE;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.events.Evenement;
import hotelsimulator.gui.ConfigGui;
import hotelsimulator.gui.HotelGui;
import hotelsimulator.gui.StarterGui;
import hotelsimulator.ruimtes.HotelRuimte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class HoofdSimulator {
	private Hotel hotel;
	private StarterGui swingGui;
	private Evenement event;
	private HTE hte;
	private SimulatieConfig config;

    public HoofdSimulator() {
        this.hotel = new Hotel();
        this.config = new SimulatieConfig();
        this.swingGui = new StarterGui(this);
	}

	public void start() {
		swingGui.guiStart();

	}

	public void laadStandaardLayout() {
        StringBuilder sb = null;
        try {
            // Leest het bestand direct uit de resources map
            InputStream is = getClass().getResourceAsStream("/hotel_layout.json");
            if (is == null) {
                throw new FileNotFoundException("Standaard layout niet gevonden in resources.");
            }

            Scanner scanner = new Scanner(is);
            sb = new StringBuilder();

            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();

			//stringbuilder naar string omzetten
            String layoutStr = sb.toString();
			//hotel layout maken met string
            hotel.maakHotelLayout(layoutStr);

            System.out.println("Standaard layout succesvol ingeladen.");

        } catch (Exception e) {
            System.out.println("Fout bij laden standaard layout: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Het standaard layoutbestand kan niet worden geladen.", "Fout",
                    JOptionPane.ERROR_MESSAGE);
        }
        HotelGui gui = new HotelGui(hotel, config);
        gui.showGui();
    }

	public void LayoutKiezer() {

		// maakt het bestand venster aan
		JFileChooser fileChooser = new JFileChooser();

		// Voegt een filter toe zodat alleen json of layout bestanden gekozen kunnen
		// worden
		fileChooser.setFileFilter(new FileNameExtensionFilter("Layout Bestanden (*.json, *.layout)", "json", "layout"));

		StringBuilder sb = new StringBuilder();

		System.out.println("Voer een naam van een .layout of .json bestand in");

		// opent het venster, geeft terug wat de gebruiker heeft geclicked
		int result = fileChooser.showOpenDialog(null);

		// als gebruiker op x klikt is result niet goedgekeurd
		if (result != JFileChooser.APPROVE_OPTION) {
			System.out.println("geen bestand gekozen");
			return;
		}

		// dit is het bestand dat de gebruiker gekozen heeft
		File file = fileChooser.getSelectedFile();

		try {
			// opent het bestand met scanner om het te lezen
			Scanner fileScanner = new Scanner(file);

			// elke regel lezen + print de regel
			while (fileScanner.hasNextLine()) {
				String regel = fileScanner.nextLine();

				// optioneel: debug print
				System.out.println(regel);
				sb.append(regel);
			}

			fileScanner.close();
			String layoutStr = sb.toString();
			hotel.maakHotelLayout(layoutStr);

			System.out.println("Custom layout succesvol ingeladen.");

			// als het bestand ongeldig is / niet kan geopend worden
		} catch (FileNotFoundException e) {

			// Logt in de console waarom het mis ging
			System.out.println("Bestand niet gevonden: " + e.getMessage());

			// messagebox: text is "het..geladen", de titel is: Fout, Error_Message zorgt
			// voor rood icoontje
			JOptionPane.showMessageDialog(
					null,
					"Het gekozen layoutbestand kan niet worden geladen.",
					"Fout",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
        HotelGui gui = new HotelGui(hotel, config);
        gui.showGui();
	}

	public SimulatieConfig getConfig() {
		return config;
	}
}