package hotelsimulator.core;

import hotelsimulator.gui.HotelGui;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Persoon;
import hotelsimulator.personen.Schoonmaker;

import javax.swing.Timer;
import java.util.List;

public class SimulatieLus {

    private final Timer bewegingsTimer;
    private final Timer spawnTimer;

    public SimulatieLus(Hotel hotel, HotelGui gui) {

        // Bewegingstimer: ~60 FPS
        bewegingsTimer = new Timer(16, e -> {
            //alle personen updaten
            for (Persoon p : hotel.getPersonen()) {
                //gasten hebben hun eigen update
                if (p instanceof Gast gast) {
                    gast.update();
                } else if (p instanceof Schoonmaker schoonmaker) {
                    //schoonmakers hebben ook hun speciale update
                    schoonmaker.update();
                }
            }
            // Verwijder personen die het hotel hebben verlaten / hebben geen kamer
            hotel.getPersonen().removeIf(Persoon::moetVerwijderdWorden);

            //regenereer de GUI
            gui.repaint();
        });

        // Spawntimer: één gast per seconde
        List<Persoon> personen = hotel.getPersonen();

        //index om bij te houden welke gast is ge spawned
        final int[] spawnIndex = {0};

        //spawnt een gast elke 1s
        spawnTimer = new Timer(1000, e -> {
            while (spawnIndex[0] < personen.size()) {
                Persoon p = personen.get(spawnIndex[0]);

                // Als het een nog niet gespawnde gast is, activeer hem
                if (p instanceof Gast gast && !gast.isGespawnd()) {
                    gast.activeer();
                    spawnIndex[0]++;
                    break;
                }
                //naar volgende gast gaan
                spawnIndex[0]++;
            }
        });
    }

    // Start beide timers.
    public void start() {
        bewegingsTimer.start();
        spawnTimer.start();
    }

    // Stopt beide timers bij pauzeren
    public void stop() {
        bewegingsTimer.stop();
        spawnTimer.stop();
    }
}

