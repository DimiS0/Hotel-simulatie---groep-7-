package hotelsimulator.events.Ruimte;

import hotelsimulator.core.Hotel;
import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Persoon;
import hotelsimulator.ruimtes.Bioscoop;


public class StartCinema implements EventStrategie {
    private final Hotel hotel;
    private static final long FILM_DUUR_MS = 60000;
    private static long filmEinde = -1;

    public StartCinema(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public void eventUitvoeren() {
        // Start timer
        filmEinde = System.currentTimeMillis() + FILM_DUUR_MS;
        System.out.println("Film gestart! Duurt 60 seconden.");

        // Aparte thread zodat de timer niet de simulatie blokkeert
        new Thread(() -> {
            try {
                Thread.sleep(FILM_DUUR_MS);
            } catch (InterruptedException e) {
                return;
            }

            System.out.println("Film klaar! Gasten verlaten bioscoop.");
            filmEinde = -1;

            for (Persoon persoon : hotel.getPersonen()) {
                if (persoon instanceof Gast gast && gast.isGespawnd()) {
                    if (gast.getHuidigeRuimte() instanceof Bioscoop) {
                        gast.gaNaarRuimte(gast.getToegwezenKamer());
                    }
                }
            }
        }).start();
    }

    public static boolean isFilmBezig() {
        return filmEinde != -1 && System.currentTimeMillis() < filmEinde;
    }
}