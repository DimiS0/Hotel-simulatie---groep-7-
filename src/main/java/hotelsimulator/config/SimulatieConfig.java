package hotelsimulator.config;
import hotelsimulator.config.HTE;
import java.util.ArrayList;
import java.util.List;

public class SimulatieConfig {
	private HTE snelheid;
	private int aantalGasten;
	private int brightness;
	private int volume;
	private ScenarioType scenario;
	// Constructor
	public SimulatieConfig() {
		this.snelheid = HTE.NORMAAL;
		this.aantalGasten = 10;
		this.brightness = 50;
		this.volume = 50;
		this.scenario = ScenarioType.STANDAARD;
	}
    //  listeners
    private final List<Runnable> listeners = new ArrayList<>();

    public void addListener(Runnable r) {
        listeners.add(r);
    }

    private void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
	// GETTERS
	public HTE getSnelheid() {
		return snelheid;
	}

	public int getAantalGasten() {
		return aantalGasten;
	}

	public int getBrightness() {
		return brightness;
	}

	public int getVolume() {
		return volume;
	}

	public ScenarioType getScenario() {
		return scenario;
	}

	// SETTERS
    public void setBrightness(int brightness) {
        this.brightness = brightness;
        notifyListeners();
    }

    public void setVolume(int volume) {
        this.volume = volume;
        notifyListeners();
    }

    public void setSnelheid(HTE snelheid) {
        this.snelheid = snelheid;
        notifyListeners();
    }

    public void setAantalGasten(int aantalGasten) {
        this.aantalGasten = aantalGasten;
        notifyListeners();
    }

    public void setScenario(ScenarioType scenario) {
        this.scenario = scenario;
        notifyListeners();
    }
}
