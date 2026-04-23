package hotelsimulator.core;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;

public class LayoutLader {

    public String laadVanResources(String pad) throws IOException {
        InputStream is = getClass().getResourceAsStream(pad);
        if (is == null) {
            throw new FileNotFoundException("Bestand niet gevonden in resources: " + pad);
        }
        return new String(is.readAllBytes());
    }

    public String laadVanBestand(File bestand) throws IOException {
        return Files.readString(bestand.toPath());
    }
}

