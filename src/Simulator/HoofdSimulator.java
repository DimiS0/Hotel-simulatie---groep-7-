package Simulator;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HoofdSimulator {
	private Hotel hotel;
	private StarterGui swingGui;
	private Evenement event;
	private HTE hte;
	private SimulatieConfig config;

	public HoofdSimulator() {
		swingGui = new StarterGui(this);
		config = new SimulatieConfig();
	}

	public void start() {
		swingGui.guiStart();
	}

	public void LayoutKiezer() {

		// maakt het bestand venster aan
		JFileChooser fileChooser = new JFileChooser();
		StringBuilder sb = new StringBuilder();
		Hotel hotel = new Hotel();

		System.out.println("Voer een naam van een .layout bestand in");

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
			String layout = sb.toString();
			hotel.maakHotelLayout(layout);

			for (HotelRuimte r : hotel.getRuimtes()) {
				System.out.println(
						r.getAreaType() + " " + r.getX() + " " + r.getY() + " breedte=" + r.getBreedte() + " sterren="
								+ r.getSterrenAantal() + " max=" + r.getMaxPersonen());
			}

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
	}

	public SimulatieConfig getConfig() {
		return config;
	}
}
