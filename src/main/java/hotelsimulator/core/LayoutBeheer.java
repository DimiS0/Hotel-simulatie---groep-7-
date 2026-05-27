package hotelsimulator.core;

import javax.swing.*;
import java.io.IOException;

public class LayoutBeheer {

    public static void layoutOpslaanInMap() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Layout bestanden in (*.json, *.layout)", "json", "layout"));

        int resultaat = chooser.showOpenDialog(null);

        if (resultaat != JFileChooser.APPROVE_OPTION) {
            System.out.println("Geen bestand gekozen");
            return;
        }
        try {
            String layoutStr = new LayoutLader().laadVanBestand(chooser.getSelectedFile());

            String naam = JOptionPane.showInputDialog(null,
                    "Geef een naam voor deze layout:");

            if (naam == null || naam.isBlank()) {
                JOptionPane.showMessageDialog(null, "Geen naam Ingevoerd/Ongeldig");
                return;
            }
            //van invoer een object maken
            OpgeslagenLayouts layout = new OpgeslagenLayouts(naam, layoutStr);
            layout.layoutsInMapStoppen();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Opslaan mislukt: " + e.getMessage(),
                    "Fout",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
