package hotelsimulator.events.Emergency;

import hotelsimulator.config.HTE;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelKamer;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.IRuimteFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CleaningEmergencyTest {

    private Hotel hotel;

    @BeforeEach
    void setup() {
        SimulatieConfig config = new SimulatieConfig();
        hotel = new Hotel(config, null, config);
        CleaningEmergency.setHotel(hotel);
    }

    @Test
    void kamerMetEmergencyWordtNietGeboekt() {
        // Maak een kamer aan met cleaning emergency
        HotelKamer kamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        kamer.setCleaningEmergency(true);
        hotel.getRuimtes().add(kamer);

        // Gast probeert kamer te boeken
        HotelKamer gevonden = hotel.zoekVrijeHotelKamer(1);

        // Kamer mag niet gevonden worden
        assertNull(gevonden);
    }

    @Test
    void kamerZonderEmergencyWordtWelGeboekt() {
        HotelKamer kamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        kamer.setCleaningEmergency(false);
        hotel.getRuimtes().add(kamer);

        HotelKamer gevonden = hotel.zoekVrijeHotelKamer(1);

        assertNotNull(gevonden);
    }

    @Test
    void emergencyKamerKrijgtPriorityInWachtrij() {
        HotelKamer normaleKamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        HotelKamer emergencyKamer = new HotelKamer("Room", 1, 2, 1, 1, 1, 2);

        hotel.voegToeAanSchoonmaakWachtrij(normaleKamer);
        hotel.voegToeAanSchoonmaakWachtrijVooraan(emergencyKamer);

        // Emergency kamer moet als eerste gepakt worden
        HotelKamer eerste = hotel.pakVolgendeSchoonmaakKamer();
        assertEquals(emergencyKamer, eerste);
    }
}