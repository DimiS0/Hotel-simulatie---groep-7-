package hotelsimulator.config;
import hotelsimulator.config.HTE;
import java.util.ArrayList;
import java.util.List;

public class SimulatieConfig  {
    private Snelheid snelheid;
	private int aantalGasten;
	private int brightness;
	private int volume;
    private int scenario = 1;
	// Constructor
	public SimulatieConfig() {
		this.snelheid = HTE.NORMAAL;
		this.aantalGasten = 10;
		this.brightness = 50;
		this.volume = 50;
	}
    //  listeners
    private final List<ConfigListener> listeners = new ArrayList<>();

    //toevoegen aan observer Lijst
    public void addListener(ConfigListener listener) {
        listeners.add(listener);
    }

    //loopen door list en updaten
    private void notifyListeners() {
        for (ConfigListener listener : listeners) {
            listener.onConfigChanged();
        }
    }
	// GETTERS
	public Snelheid getSnelheid() {
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

    public int getScenario(){
        return this.scenario;
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

    public void setSnelheid(Snelheid snelheid) {
        this.snelheid = snelheid;
        notifyListeners();
    }

    public void setAantalGasten(int aantalGasten) {
        this.aantalGasten = aantalGasten;
        notifyListeners();
    }
    public void setScenario(int value){
        this.scenario = value;
    }

}
