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
    private final Hotel hotel;

    private static final int[] LIFT_STOPS = {8, 5, 2};
    private int liftTeller    = 0;
    private int liftStopIndex = 0;

    public SimulatieLus(Hotel hotel, HotelGui gui) {
        this.hotel = hotel;

        // Bewegingstimer: ~60 FPS
        bewegingsTimer = new Timer(16, e -> {
            for (Persoon p : hotel.getPersonen()) {
                if (p instanceof Gast gast) {
                    gast.update();
                } else if (p instanceof Schoonmaker schoonmaker) {
                    schoonmaker.update();
                } else {
                    p.beweeg();
                }
            }
            gui.repaint();
        });

        // Spawntimer: één gast per seconde
        List<Persoon> personen = hotel.getPersonen();
        final int[] spawnIndex = {0};

        spawnTimer = new Timer(1000, e -> {
            while (spawnIndex[0] < personen.size()) {
                Persoon p = personen.get(spawnIndex[0]);
                if (p instanceof Gast gast && !gast.isGespawnd()) {
                    gast.activeer();
                    spawnIndex[0]++;
                    break;
                }
                spawnIndex[0]++;
            }
        });
    }

    // Start beide timers.
    public void start() {
        bewegingsTimer.start();
        spawnTimer.start();
    }

    // Stopt beide timers (bijv. bij pauzeren).
    public void stop() {
        bewegingsTimer.stop();
        spawnTimer.stop();
    }

    // Geeft terug of de simulatie momenteel loopt.
    public boolean isActief() {
        return bewegingsTimer.isRunning();
    }
}

