package hotelsimulator.gui;

import javax.swing.*;

import hotelsimulator.config.ScenarioType;
import hotelsimulator.config.SimulatieConfig;

import java.awt.*;

public class ConfigGui {

	private SimulatieConfig config;

	public ConfigGui(SimulatieConfig config) {
		this.config = config;
		creatAndShowGUI();

	}

	private void creatAndShowGUI() {
		JFrame frame = new JFrame("instellingen");
		frame.setSize(400, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
		// snelheid
		panel.add(new JLabel("Snelheid"));
		JSlider snelheidSlider = new JSlider(1, 3, config.getSnelheid());
		snelheidSlider.addChangeListener(e -> config.setSnelheid(snelheidSlider.getValue()));
		panel.add(snelheidSlider);

		// Aantal gasten
		panel.add(new JLabel("Aantal gasten:"));
		JTextField gastenField = new JTextField(String.valueOf(config.getAantalGasten()));
		gastenField.addActionListener(e -> {
			try {
				int g = Integer.parseInt(gastenField.getText());
				config.setAantalGasten(g);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "Ongeldige waarde");
			}
		});
		panel.add(gastenField);

		// Brightness
		panel.add(new JLabel("Brightness:"));
		JSlider brightnessSlider = new JSlider(0, 100, config.getBrightness());
		brightnessSlider.addChangeListener(e -> config.setBrightness(brightnessSlider.getValue()));
		panel.add(brightnessSlider);

		// Volume
		panel.add(new JLabel("Volume:"));
		JSlider volumeSlider = new JSlider(0, 100, config.getVolume());
		volumeSlider.addChangeListener(e -> config.setVolume(volumeSlider.getValue()));
		panel.add(volumeSlider);

		// Scenario
		panel.add(new JLabel("Scenario:"));
		JComboBox<ScenarioType> scenarioBox = new JComboBox<>(ScenarioType.values());
		scenarioBox.setSelectedItem(config.getScenario());
		scenarioBox.addActionListener(e -> config.setScenario((ScenarioType) scenarioBox.getSelectedItem()));
		panel.add(scenarioBox);

		frame.add(panel);
		frame.setVisible(true);
	}

}
