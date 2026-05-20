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
        SimulatieConfig config = new SimulatieConfig();
        Hotel hotel = new Hotel(config, new HotelEventManager(), config);
        hotel.maakHotelLayout("[]");
        hotel.maakPersonen(3); // gasten 1, 2, 3 bestaan
        HotelEvent event = new HotelEvent(1, HotelEventType.GOTO_FITNESS, 4, -1);

        // Act
        EventStrategie strategie = evenementFactory.verwerken(event, hotel);

        // Assert
        assertNotNull(strategie);
        assertDoesNotThrow(() -> strategie.eventUitvoeren());
        assertNull(hotel.zoekGastOpId(4));
    }}