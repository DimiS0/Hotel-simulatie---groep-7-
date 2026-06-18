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

    private Hotel hotel; // Hotel dat in alle tests gebruikt wordt

    @BeforeEach
    void setup() {
        SimulatieConfig config = new SimulatieConfig(); // Maak een simulatieconfiguratie aan
        hotel = new Hotel(null, config); // Maak een nieuw hotel voor de test
        CleaningEmergency.setHotel(hotel); // Geef het hotel door aan CleaningEmergency
    }

    @Test
    void kamerMetEmergencyWordtNietGeboekt() {

        // Maak een hotelkamer aan
        HotelKamer kamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        // Zet cleaning emergency aan
        kamer.setCleaningEmergency(true);
        // Voeg de kamer toe aan het hotel
        hotel.getRuimtes().add(kamer);
        // Zoek een vrije kamer voor een gast
        HotelKamer gevonden = hotel.zoekVrijeHotelKamer(1);
        // Controleer dat de kamer niet gevonden wordt
        assertNull(gevonden);
    }

    @Test
    void kamerZonderEmergencyWordtWelGeboekt() {

        // Maak een hotelkamer aan
        HotelKamer kamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        // Zet cleaning emergency uit
        kamer.setCleaningEmergency(false);
        // Voeg de kamer toe aan het hotel
        hotel.getRuimtes().add(kamer);
        // Zoek een vrije kamer
        HotelKamer gevonden = hotel.zoekVrijeHotelKamer(1);
        // Controleer dat de kamer gevonden wordt
        assertNotNull(gevonden);
    }

    @Test
    void emergencyKamerKrijgtPriorityInWachtrij() {
        // Maak een normale kamer aan
        HotelKamer normaleKamer = new HotelKamer("Room", 1, 1, 1, 1, 1, 2);
        // Maak een emergency-kamer aan
        HotelKamer emergencyKamer = new HotelKamer("Room", 1, 2, 1, 1, 1, 2);
        // Voeg de normale kamer achteraan de wachtrij toe
        hotel.voegToeAanSchoonmaakWachtrij(normaleKamer);
        // Voeg de emergency-kamer vooraan de wachtrij toe
        hotel.voegToeAanSchoonmaakWachtrijVooraan(emergencyKamer);
        // Pak de volgende kamer uit de wachtrij
        HotelKamer eerste = hotel.pakVolgendeSchoonmaakKamer();
        // Controleer dat de emergency-kamer als eerste wordt gepakt
        assertEquals(emergencyKamer, eerste);
    }
}