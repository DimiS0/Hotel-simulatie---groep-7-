package hotelsimulator.core;

import javax.swing.*;
import java.io.IOException;

public class LayoutBeheer {

    public static void layoutOpslaanInMap() {

        // filechooser object om file te kiezen
        JFileChooser chooser = new JFileChooser();

        //filteren op Json
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Layout bestanden in (*.json, *.layout)", "json", "layout"));

        //open dialoog om bestand te kiezen
        int resultaat = chooser.showOpenDialog(null);

        //als reslutaat leeg is dus dicht doen of cancel
        if (resultaat != JFileChooser.APPROVE_OPTION) {
            System.out.println("Geen bestand gekozen");
            return;
        }

        try {
            //Laad de JSON-layout van het gekozen bestand als String
            String layoutStr = new LayoutLader().laadVanBestand(chooser.getSelectedFile());

            //Vraag gebruiker om een naam voor deze layout
            String naam = JOptionPane.showInputDialog(null,
                    "Geef een naam voor deze layout:");

            //Als gebruiker geen naam invoert of een lege naam, geef fout en ga terug
            if (naam == null || naam.isBlank()) {
                JOptionPane.showMessageDialog(null, "Geen naam Ingevoerd/Ongeldig");
                return;
            }

            //Maak een object voor de opgeslagen layout met de naam en de layoutString
            OpgeslagenLayouts layout = new OpgeslagenLayouts(naam, layoutStr);

            // Sla dit layout object op in een bestand in de juiste map
            layout.layoutsInMapStoppen();

        } catch (IOException e) {
            //als opslaan mislukt, toon foutmelding met een dialoog bericht
            JOptionPane.showMessageDialog(null,
                    "Opslaan mislukt: " + e.getMessage(),
                    "Fout",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
