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

    public void layoutsInMapStoppen() throws IOException {
       Path mapPad = Path.of("layouts");
        Files.createDirectories(mapPad);
        Path bestandMap = mapPad.resolve(naam + ".json");

        if (Files.exists(bestandMap)) {
            System.out.println("Zelfde naam niet toegestaan");
            return;
        }

        Files.writeString(bestandMap, layoutStr);

    }

    public String getNaam() {
        return naam;
    }

    public String getLayoutStr() {
        return layoutStr;
    }

    public static List<OpgeslagenLayouts> laadLayoutsUitMap() throws IOException {
        List<OpgeslagenLayouts> layouts = new ArrayList<>();
        Path mapPad = Path.of("layouts");

        // als map niet bestaat lege lijst teruggeven
        if (!Files.exists(mapPad)) {
            return layouts;
        }

        try (Stream<Path> bestanden = Files.list(mapPad)) {
            for (Path bestand : bestanden.toList()) {
                String inhoud = Files.readString(bestand);

                String bestandsNaam = bestand.getFileName().toString();
                String naam = bestandsNaam.replace(".json", "");

                //van opslag terug naar object
                OpgeslagenLayouts layout = new OpgeslagenLayouts(naam, inhoud);
                layouts.add(layout);
            }
            return layouts;
        }
    }
}
