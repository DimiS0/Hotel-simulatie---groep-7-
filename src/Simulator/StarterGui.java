package Simulator;

import javax.swing.*;
import java.awt.*;


public class StarterGui {
    private HoofdSimulator hoofdSimulator;
JFrame scherm1 = new JFrame();
JPanel bestandinvoegen = new JPanel();
JButton layout = new JButton("Kies .layout bestand");

    public StarterGui(HoofdSimulator hoofdSimulator) {
        this.hoofdSimulator = hoofdSimulator;

        //titel, niet grotermaken, sluiten, border toevoegn
        scherm1.setTitle("Hotel Simulator");
        scherm1.setResizable(false);
        scherm1.setSize(800, 1000);
        scherm1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        scherm1.setLayout(new BorderLayout());

        //gridbaglayout zodat het knopje in het midden komt
        bestandinvoegen.setLayout(new GridBagLayout());
        bestandinvoegen.add(layout);

        //zet de panel zelf in het midden
        scherm1.add(bestandinvoegen, BorderLayout.CENTER);

        //extra ruimte in de knop zelf dus hij wordt groter ,painted maakt het wat cleaner kan ook weg
        layout.setMargin(new Insets(10,20,10,20));
        layout.setFocusPainted(false);

        //actie als knop wordt geklikt
        layout.addActionListener(e -> {
        this.hoofdSimulator.LayoutKiezer();
        });



    }
    public void guiStart(){
        scherm1.setVisible(true);
    }
}