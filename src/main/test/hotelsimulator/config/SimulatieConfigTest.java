package hotelsimulator.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulatieConfigTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void setSnelheid() {
    SimulatieConfig config = new SimulatieConfig();
    config.setSnelheid(HTE.NORMAAL);
    assertEquals(HTE.NORMAAL, config.getSnelheid());
    }

    @Test
    void setAantalGasten() {
    SimulatieConfig config = new SimulatieConfig();
    config.setAantalGasten(10);
    assertEquals(10, config.getAantalGasten());
    }

    @Test
    void setBrightness() {
        SimulatieConfig config = new SimulatieConfig();
        config.setBrightness(10);
        assertEquals(10, config.getBrightness());
    }

    @Test
    void setVolume() {
    SimulatieConfig config = new SimulatieConfig();
    config.setVolume(80);
    assertEquals(80, config.getVolume());
    }}