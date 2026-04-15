package hotelsimulator.core;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.ruimtes.HotelRuimte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HotelTest {
    private Hotel hotel;
    private SimulatieConfig config;
    private HotelEventManager hotelEventManager;

    @BeforeEach
    void setUp() {
        hotelEventManager = new HotelEventManager();
        config = new SimulatieConfig();
        hotel = new Hotel(config, hotelEventManager);


    }

    @Test
    void maakHotelLayout() {
        String jsontest = "[{\"AreaType\": \"Cinema\", \"Position\": \"1,8\", \"Dimension\": \"2,2\", \"Capacity\": \"50\", \"Classification\": \"5\"}]";
        hotel.maakHotelLayout(jsontest);

        HotelRuimte hotelRuimte = hotel.getRuimtes().get(0);

        assertEquals(5,hotel.getRuimtes().size());
        assertEquals("Cinema",hotel.getRuimtes().get(0).getAreaType());
        assertEquals(1, hotelRuimte.getX());
        assertEquals(1, hotelRuimte.getY());
        assertEquals(2,hotelRuimte.getBreedte());
        assertEquals(2,hotelRuimte.getHoogte());
        assertEquals(50, hotelRuimte.getMaxPersonen());
        assertEquals("5",hotelRuimte.getSterrenAantal());
    }
}