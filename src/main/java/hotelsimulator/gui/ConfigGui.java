package hotelsimulator.gui;

import javax.swing.*;
import hotelsimulator.config.HTE;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.config.Snelheid;


import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ConfigGui {
    private final SimulatieConfig config;
    private final Consumer<Integer> onSpeedChange;

    //frame is een veld nu, zodat we het aan startergui kunnnen geven met getframe()
    private JFrame frame;


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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        //scenario label en slider maken
        panel.add(new JLabel("Scenario"));
        JSlider scenarioSlider = new JSlider(1, 4, config.getScenario());

        //voor onder de slider om visueel te laten ien waar je bent
        Hashtable<Integer, JLabel> scenarios = new Hashtable<>();
        scenarios.put(1, new JLabel("1"));
        scenarios.put(2, new JLabel("2"));
        scenarios.put(3, new JLabel("3"));
        scenarios.put(4, new JLabel("4"));

        // voegt | streepjes toe en de labels
        scenarioSlider.setLabelTable(scenarios);
        scenarioSlider.setPaintLabels(true);
        scenarioSlider.setPaintTicks(true);
        scenarioSlider.setMajorTickSpacing(1);

        //listener om de waarde op te halen van de slider en opslaan
        scenarioSlider.addChangeListener(e ->{
            int scenarioValue = scenarioSlider.getValue();
            config.setScenario(scenarioValue);
        });
        panel.add(scenarioSlider);

        //label voor gasten en een tetfeld aanmaken
        panel.add(new JLabel("Aantal gasten:"));
        JTextField gastenField = new JTextField(String.valueOf(config.getAantalGasten()));

        //wanneer de gastenveld geen focus heeft
        gastenField.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                try {
                    //de tekst ophalen en in een int zetten
                    int g = Integer.parseInt(gastenField.getText());

                    //opslaan in config
                    config.setAantalGasten(g);
                } catch (NumberFormatException ex) {
                    //crashes opvangen
                    JOptionPane.showMessageDialog(frame, "Ongeldige waarde");
                }}});

        //aan paneel toevoegen
        panel.add(gastenField);

        //nu voor snelheid label en slider
        panel.add(new JLabel("Snelheid"));
        JSlider snelheidSlider = new JSlider(1, 5, mapHTEToSlider(config.getSnelheid()));

        //weer labels maken voor onder de slider
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(1, new JLabel("0.25x"));
        labels.put(2, new JLabel("0.5x"));
        labels.put(3, new JLabel("1x"));
        labels.put(4, new JLabel("2x"));
        labels.put(5, new JLabel("4x"));

        //labels toevoegen en tekenen, met de |
        snelheidSlider.setLabelTable(labels);
        snelheidSlider.setPaintLabels(true);
        snelheidSlider.setPaintTicks(true);
        snelheidSlider.setMajorTickSpacing(1);

        //changelistener om een waarde optehalen als het veranderd
        snelheidSlider.addChangeListener(e -> {
            //opslaan in een int
            int value = snelheidSlider.getValue();
            //ometten in een case
            Snelheid snelheid = mapSliderToHTE(value);
            //opslaan
            config.setSnelheid(snelheid);
            //voor gui schermen zodat ze kunnen updaten
            onSpeedChange.accept(value);
        });

        panel.add(snelheidSlider);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private Snelheid mapSliderToHTE(int value) {
        return switch (value) {
            case 1 -> HTE.LANGZAMER;
            case 2 -> HTE.LANGZAAM;
            case 3 -> HTE.NORMAAL;
            case 4 -> HTE.SNEL;
            case 5 -> HTE.VIER_X;
            default -> HTE.NORMAAL;
        };
    }

    private int mapHTEToSlider(Snelheid hte) {
        double factor = hte.getFactor();
        if (factor <= 0.25) return 1;
        if (factor <= 0.5)  return 2;
        if (factor <= 1.0)  return 3;
        if (factor <= 2.0)  return 4;
        return 5;
    }

    //geeft instelling venster, zodat we die kunnen sluiten
    public JFrame getFrame() {
        return frame;
    }
}