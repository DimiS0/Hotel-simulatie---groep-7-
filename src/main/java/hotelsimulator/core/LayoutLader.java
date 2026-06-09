package hotelsimulator.core;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;

public class LayoutLader {

    //Laad tekst uit resources
    public String laadVanResources(String pad) throws IOException {

        //Bestand zoeken in de resources via het gekozen pad
        InputStream is = getClass().getResourceAsStream(pad);

        // Als het bestand niet gevonden wordt, stuur een foutbericht
        if (is == null) {
            throw new FileNotFoundException("Bestand niet gevonden in resources: " + pad);
        }
        //Lees alle bytes van het InputStream en maak er een String
        return new String(is.readAllBytes());
    }

    //Laad tekst uit een  bestand op je pc
    public String laadVanBestand(File bestand) throws IOException {

        //Converteren van het File object naar een pad en lees de volledige inhoud als String en die geven we terug
        return Files.readString(bestand.toPath());
    }
}

