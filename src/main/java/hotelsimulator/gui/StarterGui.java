package hotelsimulator.gui;

import javax.swing.*;

import hotelsimulator.core.HoofdSimulator;

import java.awt.*;

public class StarterGui {
	private HoofdSimulator hoofdSimulator;
	JFrame scherm1 = new JFrame();
	JPanel bestandinvoegen = new JPanel();
	JButton defaultLayout = new JButton("Laad standaard layout");
	JButton customLayout = new JButton("Kies custom layout");

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

		// zet de panel zelf in het midden
		scherm1.add(bestandinvoegen, BorderLayout.CENTER);

		// extra ruimte in de knop zelf dus hij wordt groter, painted maakt het wat
		// cleaner
		defaultLayout.setMargin(new Insets(10, 20, 10, 20));
		defaultLayout.setFocusPainted(false);

		customLayout.setMargin(new Insets(10, 20, 10, 20));
		customLayout.setFocusPainted(false);

		// actie als knop wordt geklikt
        defaultLayout.addActionListener(e -> {
            scherm1.dispose();
            this.hoofdSimulator.laadStandaardLayout();
        });

        customLayout.addActionListener(e -> {
            scherm1.dispose();
            this.hoofdSimulator.LayoutKiezer();
        });

	}

	public void guiStart() {
		scherm1.setVisible(true);
	}
}
