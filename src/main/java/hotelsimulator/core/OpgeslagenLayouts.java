package hotelsimulator.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class OpgeslagenLayouts {
    private String naam;
    private String layoutStr;

    public OpgeslagenLayouts(String naam, String layoutStr) {
        this.naam = naam;
        this.layoutStr = layoutStr;
    }

    //layout opslaan als bestand in een map
    public void layoutsInMapStoppen() throws IOException {
        //Maken van map Layouts als die niet gemaakt is
       Path mapPad = Path.of("layouts");
        Files.createDirectories(mapPad);

        //bestaandsnaam dus met + .json aan het einde
        Path bestandMap = mapPad.resolve(naam + ".json");

        //als er al een bestaat met deelfde naam printen we nee, en niks doen
        if (Files.exists(bestandMap)) {
            System.out.println("Zelfde naam niet toegestaan");
            return;
        }

        //Schrijven van de json string naar het bestand
        Files.writeString(bestandMap, layoutStr);

    }
    //geeft de layout String terug
    public String getLayoutStr() {
        return layoutStr;
    }

    @Override
    //In een lijst wordt dit getoond als de naam, niet als hele object-string
    public String toString() {
        return naam;
    }

    //Laad alle opgeslagen layouts uit layout map en geef een lijst terug
    public static List<OpgeslagenLayouts> laadLayoutsUitMap() throws IOException {
        List<OpgeslagenLayouts> layouts = new ArrayList<>();
        Path mapPad = Path.of("layouts");

        //als map niet bestaat lege lijst teruggeven
        if (!Files.exists(mapPad)) {
            return layouts;
        }

        //Zoek alle .json-bestanden in de map
        try (Stream<Path> bestanden = Files.list(mapPad)) {
            for (Path bestand : bestanden.toList()) {

                //lees de inhoud van bestand
                String inhoud = Files.readString(bestand);

                //naam ophalen zonder json
                String bestandsNaam = bestand.getFileName().toString();
                String naam = bestandsNaam.replace(".json", "");

                //Maak een layout object van de naam en inhoud
                OpgeslagenLayouts layout = new OpgeslagenLayouts(naam, inhoud);
                layouts.add(layout);
            }
            return layouts;
        }
    }
}
