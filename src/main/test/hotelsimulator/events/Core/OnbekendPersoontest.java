package hotelsimulator.events.Core;

import hotelevents.HotelEvent;
import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class OnbekendePersoonTest {

    @Test
    void eventVoorOnbekendePersoonWordtOvergeslagen() {

        // Arrange
        // Maak een simulatieconfiguratie aan
        SimulatieConfig config = new SimulatieConfig();
        // Maak een hotel aan met een EventManager
        Hotel hotel = new Hotel(new HotelEventManager(), config);
        // Maak een eenvoudige hotellayout
        hotel.maakHotelLayout("[]");
        // Maak 3 gasten aan (ID 1, 2 en 3)
        hotel.maakPersonen(3); // gasten 1, 2, 3 bestaan
        // Maak een event voor gast 4 die niet bestaat
        HotelEvent event = new HotelEvent(1, HotelEventType.GOTO_FITNESS, 4, -1);

        // Act
        // Laat de EventFactory een strategie voor het event maken
        EventStrategie strategie = evenementFactory.verwerken(event, hotel);

        // Assert
        // Controleer dat er een strategie is teruggegeven
        assertNotNull(strategie);
        // Controleer dat het uitvoeren van het event geen fout geeft
        assertDoesNotThrow(() -> strategie.eventUitvoeren());
        // Controleer dat gast 4 nog steeds niet bestaat
        assertNull(hotel.zoekGastOpId(4));
    }}