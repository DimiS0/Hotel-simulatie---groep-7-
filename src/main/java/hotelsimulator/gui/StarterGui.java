package hotelsimulator.gui;

import javax.swing.*;

import hotelsimulator.core.HoofdSimulator;
import hotelsimulator.core.Hotel;

import java.awt.*;

public class StarterGui {
	private HoofdSimulator hoofdSimulator;
    private Hotel hotel;
	JFrame scherm1 = new JFrame();
	JPanel bestandinvoegen = new JPanel();
	JButton defaultLayout = new JButton("Laad standaard layout");
	JButton customLayout = new JButton("Kies custom layout");
	JButton instellingenBtn = new JButton("Instellingen");

	//onthoudt het configvenster, zodat we die kunnen sluiten
	private JFrame configFrame = null;

	public StarterGui(HoofdSimulator hoofdSimulator) {
		this.hoofdSimulator = hoofdSimulator;

		// titel, niet grotermaken, sluiten, border toevoegen
		scherm1.setTitle("Hotel Simulator");
		scherm1.setResizable(false);
		scherm1.setSize(800, 1000);
		scherm1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scherm1.setLayout(new BorderLayout());

		// gridbaglayout zodat de knoppen in het midden komen
		bestandinvoegen.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);

		bestandinvoegen.add(defaultLayout, gbc);
		bestandinvoegen.add(customLayout, gbc);
		bestandinvoegen.add(instellingenBtn, gbc);
		// zet de panel zelf in het midden
		scherm1.add(bestandinvoegen, BorderLayout.CENTER);

		// extra ruimte in de 3 knoppen zelf dus hij wordt groter, painted maakt het wat mooier
		defaultLayout.setMargin(new Insets(10, 20, 10, 20));
		defaultLayout.setFocusPainted(false);

		customLayout.setMargin(new Insets(10, 20, 10, 20));
		customLayout.setFocusPainted(false);

		instellingenBtn.setMargin(new Insets(10, 20, 10, 20));
		//zorgt ervoor dat het even groot is als de andere knoppen
		instellingenBtn.setPreferredSize(new Dimension(defaultLayout.getPreferredSize().width, defaultLayout.getPreferredSize().height));

		//standaard layout knop
		defaultLayout.addActionListener(e -> {

			//Sluit de instellingen venster in standaardlayout button, config sluiten en scherm
			if (configFrame != null && configFrame.isVisible()) {
				configFrame.dispose();
			}
			scherm1.dispose();
			this.hoofdSimulator.laadStandaardLayout();
		});

		//eigen layout kiezen button
		customLayout.addActionListener(e -> {

			//Sluit de instellingenvenster in eigen layout modus, config sluiten en scherm
			if (configFrame != null && configFrame.isVisible()) {
				configFrame.dispose();
			}
			scherm1.dispose();
			this.hoofdSimulator.LayoutKiezer();
		});

		//instelling button
		instellingenBtn.addActionListener(e -> {

			// sla het instellingen venster op in config, later als gebruiker layout kiest sluiten
            configFrame = new ConfigGui(hoofdSimulator.getConfig(), value -> {}).getFrame();

        });
	}

	public void guiStart() {
		scherm1.setVisible(true);
	}
}
