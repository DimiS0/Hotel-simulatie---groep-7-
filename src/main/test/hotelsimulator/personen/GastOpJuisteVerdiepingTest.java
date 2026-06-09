package hotelsimulator.personen;

import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelevents.HotelEventManager;
import hotelsimulator.personen.Gast;
import org.junit.Test;
import static org.junit.Assert.*;

public class GastOpJuisteVerdiepingTest {

    @Test
    public void gastSpawntOnderaan() {
        String minimalLayout = "[{\"AreaType\":\"Room\",\"Position\":\"1,1\",\"Dimension\":\"2,3\",\"Capacity\":\"1\",\"Classification\":\"3\"}]";

        HotelEventManager eventManager = new HotelEventManager();
        SimulatieConfig config = new SimulatieConfig();
        Hotel hotel = new Hotel(eventManager, config);
        hotel.maakHotelLayout(minimalLayout);

        Gast gast = new Gast(hotel.getLift(), hotel.getSchacht(), hotel,
                eventManager, config, 1, hotel.getMaxBreedte(), hotel.getMaxHoogte());

        assertTrue(gast.getPixelY() > hotel.getMaxHoogte() * 50);
    }

    @Test
    public void gastIsNaActiveerInLobby() {
        String minimalLayout = "[{\"AreaType\":\"Room\",\"Position\":\"1,1\",\"Dimension\":\"2,3\",\"Capacity\":\"1\",\"Classification\":\"3\"}]";

        HotelEventManager eventManager = new HotelEventManager();
        SimulatieConfig config = new SimulatieConfig();
        Hotel hotel = new Hotel(eventManager, config);
        hotel.maakHotelLayout(minimalLayout);

        Gast gast = new Gast(hotel.getLift(), hotel.getSchacht(), hotel,
                eventManager, config, 1, hotel.getMaxBreedte(), hotel.getMaxHoogte());

        int spawnY = gast.getPixelY();
        gast.activeer();

        assertEquals(Gast.Status.WACHT_IN_LOBBY, gast.getStatus());
        assertEquals(spawnY, gast.getPixelY());
    }
}