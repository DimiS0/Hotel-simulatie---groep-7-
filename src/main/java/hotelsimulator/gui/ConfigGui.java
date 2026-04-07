package hotelsimulator.gui;

import javax.swing.*;
import hotelsimulator.config.HTE;
import hotelsimulator.config.ScenarioType;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.config.TimerSim;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ConfigGui {
    private HTE valueHTE;
    private final SimulatieConfig config;
    private final Consumer<Integer> onSpeedChange;

    //frame is een veld nu, zodat we het aan startergui kunnnen geven met getframe()
    private JFrame frame;

    public ConfigGui(SimulatieConfig config, Consumer<Integer> onSpeedChange, int timerSim) {
        this.config = config;
        this.onSpeedChange = onSpeedChange;

        createAndShowGUI();}
    public ConfigGui(SimulatieConfig config, Consumer<Integer> onSpeedChange){
        this.config = config;
        this.onSpeedChange = onSpeedChange;
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Instellingen");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel testLabel = new JLabel("Volume: " + config.getVolume(), SwingConstants.CENTER);
        testLabel.setOpaque(true);
        testLabel.setBackground(Color.GRAY);
        frame.add(testLabel, BorderLayout.SOUTH);

        panel.add(new JLabel("Snelheid"));
        JSlider snelheidSlider = new JSlider(1, 5, mapHTEToSlider(config.getSnelheid()));

        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(1, new JLabel("0.25x"));
        labels.put(2, new JLabel("0.5x"));
        labels.put(3, new JLabel("1x"));
        labels.put(4, new JLabel("2x"));
        labels.put(5, new JLabel("4x"));

        snelheidSlider.setLabelTable(labels);
        snelheidSlider.setPaintLabels(true);
        snelheidSlider.setPaintTicks(true);
        snelheidSlider.setMajorTickSpacing(1);

        snelheidSlider.addChangeListener(e -> {
            int value = snelheidSlider.getValue();
            HTE snelheid = mapSliderToHTE(value);
            config.setSnelheid(snelheid);
            valueHTE = snelheid;
            onSpeedChange.accept(value);
        });

        panel.add(snelheidSlider);

        panel.add(new JLabel("Aantal gasten:"));
        JTextField gastenField = new JTextField(String.valueOf(config.getAantalGasten()));
        gastenField.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                try {
                    int g = Integer.parseInt(gastenField.getText());
                    config.setAantalGasten(g);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Ongeldige waarde");
                }}});

        panel.add(gastenField);

        panel.add(new JLabel("Brightness:"));
        JSlider brightnessSlider = new JSlider(0, 100, config.getBrightness());
        brightnessSlider.addChangeListener(e -> {
            int value = brightnessSlider.getValue();
            config.setBrightness(value);
            int c = value * 255 / 100;
            testLabel.setBackground(new Color(c, c, c));
        });
        panel.add(brightnessSlider);

        panel.add(new JLabel("Volume:"));
        JSlider volumeSlider = new JSlider(0, 100, config.getVolume());
        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            config.setVolume(value);
            testLabel.setText("Volume: " + value);
        });
        panel.add(volumeSlider);

        panel.add(new JLabel("Scenario:"));
        JComboBox<ScenarioType> scenarioBox = new JComboBox<>(ScenarioType.values());
        scenarioBox.setSelectedItem(config.getScenario());
        scenarioBox.addActionListener(e ->
                config.setScenario((ScenarioType) scenarioBox.getSelectedItem()));
        panel.add(scenarioBox);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private HTE mapSliderToHTE(int value) {
        return switch (value) {
            case 1 -> HTE.LANGZAMER;
            case 2 -> HTE.LANGZAAM;
            case 3 -> HTE.NORMAAL;
            case 4 -> HTE.SNEL;
            case 5 -> HTE.VIER_X;
            default -> HTE.NORMAAL;
        };
    }

    private int mapHTEToSlider(HTE hte) {
        this.valueHTE = hte;
        return switch (hte) {
            case LANGZAMER -> 1;
            case LANGZAAM  -> 2;
            case NORMAAL   -> 3;
            case SNEL      -> 4;
            case VIER_X    -> 5;
            default        -> 3;
        };
    }

    //geeft instelling venster, zodat we die kunnen sluiten
    public JFrame getFrame() {
        return frame;
    }
}