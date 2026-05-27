package hotelsimulator.events.Core;

import hotelevents.HotelEvent;
import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelevents.HotelEventListener;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.events.Core.evenementFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EvenementTest {

        @Test
        void godzillaAlsOnbekendEventGeeftException() {

            // Arrange
            SimulatieConfig config = new SimulatieConfig();
            Hotel hotel = new Hotel(config, new HotelEventManager(), config);
            HotelEvent event = new HotelEvent(1, HotelEventType.GODZILLA, -1, -1);

            // Act
            Executable act = () -> evenementFactory.verwerken(event, hotel);

            // Assert
            assertThrows(IllegalArgumentException.class, act);

            //test faalt
            // assertThrows(NullPointerException.class, act);
        }
}