package hotelsimulator.events.Emergency;

import hotelsimulator.core.Hotel;
import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.ruimtes.HotelKamer;
import hotelsimulator.ruimtes.HotelRuimte;

import java.util.Random;

public class CleaningEmergency implements EventStrategie {

    private static Hotel hotel;
    private static final Random random = new Random();

    public CleaningEmergency() {}

    public static void setHotel(Hotel hotelInstantie) {
        hotel = hotelInstantie;
    }

    @Override
    public void eventUitvoeren() {
        if (hotel == null) return;

        // Kies een willekeurige HotelKamer (alleen echte kamers, geen restaurant etc.)
        var kamers = hotel.getRuimtes().stream()
                .filter(r -> r instanceof HotelKamer)
                .map(r -> (HotelKamer) r)
                .toList();

        if (kamers.isEmpty()) return;

        HotelKamer noodkamer = kamers.get(random.nextInt(kamers.size()));
        noodkamer.setCleaningEmergency(true);

        // Gewoon toevoegen aan de schoonmaakwachtrij — schoonmakers pakken het op
        hotel.voegToeAanSchoonmaakWachtrij(noodkamer);
        System.out.println("Cleaning emergency toegevoegd aan wachtrij: " + noodkamer.getAreaType());
    }
}