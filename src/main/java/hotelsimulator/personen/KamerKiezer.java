package hotelsimulator.personen;

import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.HotelRuimte;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class KamerKiezer {

    private static final Random random = new Random();
    private static final int MAX_POGINGEN = 15;


    public HotelRuimte kiesBesteKamer(List<HotelRuimte> kamers,
                                      int pixelX, int pixelY,
                                      List<HotelRuimte> alleRuimtes) {
        for (int poging = 0; poging < MAX_POGINGEN; poging++) {
            HotelRuimte kandidaat = kamers.get(random.nextInt(kamers.size()));

            if (kandidaat.isVol()) continue;

            Point ingang = Pathfinder.getKamerIngang(kandidaat);
            Point midden = getTrueMidden(kandidaat);

            // Sla ongeldige kamers over (midden moet boven de ingang liggen)
            if (midden.y >= ingang.y) continue;

            // Controleer of er een pad naartoe bestaat
            List<Point> pad = Pathfinder.vindPad(pixelX, pixelY,
                    ingang.x, ingang.y, alleRuimtes);
            if (!pad.isEmpty()) {
                return kandidaat;
            }
        }
        return null;
    }

    //Geeft de dichtstbijzijnde lifthalte terug voor een gegeven grid-rij.

    public int getNabijeStop(int gridY, int[] stops) {
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij)) {
                dichtstbij = stop;
            }
        }
        return dichtstbij;
    }

    // Berekent het pixel-midden van een kamer.
    private Point getTrueMidden(HotelRuimte kamer) {
        int middenX = (kamer.getX() + 1) * 50 + kamer.getBreedte() * 50 / 2;
        int middenY = (kamer.getY() - 1) * 50 + kamer.getHoogte()  * 50 / 2;
        return new Point(middenX, middenY);
    }
}
