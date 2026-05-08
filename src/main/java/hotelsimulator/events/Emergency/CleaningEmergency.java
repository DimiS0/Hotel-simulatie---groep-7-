package hotelsimulator.events.Emergency;


import hotelsimulator.core.Hotel;
import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Persoon;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.HotelRuimte;

import java.util.List;
import java.util.Random;

public class CleaningEmergency implements EventStrategie {

    // Hotel wordt eenmalig ingesteld via setHotel()
    private static Hotel hotel;

    private static final int MAX_SCHOONMAKERS = 2;
    private static final Random random = new Random();

    // Lege constructor zodat de factory hem zonder parameters kan aanmaken
    public CleaningEmergency() {}

    // Wordt eenmalig aangeroepen vanuit HoofdSimulator na het aanmaken van hotel
    public static void setHotel(Hotel hotelInstantie) {
        hotel = hotelInstantie;
    }

    @Override
    public void eventUitvoeren() {
        if (hotel == null) {
            System.out.println("CleaningEmergency: hotel is nog niet ingesteld!");
            return;
        }

        List<HotelRuimte> kamers = hotel.getKamers();
        if (kamers.isEmpty()) return;

        // Kies een willekeurige kamer als noodgeval
        int willekeurigeIndex = random.nextInt(kamers.size());
        HotelRuimte noodkamer = kamers.get(willekeurigeIndex);
        noodkamer.setCleaningEmergency(true);

        System.out.println("Cleaning emergency in: " + noodkamer.getAreaType());

        // Stuur maximaal 2 schoonmakers die vrij zijn naar de noodkamer
        int aantalGestuurd = 0;

        for (Persoon persoon : hotel.getPersonen()) {
            boolean maxBereikt = aantalGestuurd >= MAX_SCHOONMAKERS;
            if (maxBereikt) break;

            boolean isSchoonmaker = persoon instanceof Schoonmaker;
            if (!isSchoonmaker) continue;

            Schoonmaker schoonmaker = (Schoonmaker) persoon;
            boolean isVrij = schoonmaker.isInGang();

            if (isVrij) {
                schoonmaker.reageerOpEmergency(noodkamer);
                aantalGestuurd++;
            }
        }

        System.out.println(aantalGestuurd + " schoonmaker(s) onderweg naar emergency.");
    }
}
