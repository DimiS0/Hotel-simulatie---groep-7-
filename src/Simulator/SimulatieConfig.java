package Simulator;

public class SimulatieConfig {
	private int snelheid;
	private int aantalGasten;
	private int brightness;
	private int volume;
	private ScenarioType scenario;

	// Constructor
	public SimulatieConfig() {
		this.snelheid = 1;
		this.aantalGasten = 10;
		this.brightness = 50;
		this.volume = 50;
		this.scenario = ScenarioType.STANDAARD;
	}

	// GETTERS
	public int getSnelheid() {
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
	public void setSnelheid(int snelheid) {
		if (snelheid > 0) {
			this.snelheid = snelheid;
		} else {
			System.out.println("Ongeldige snelheid");
		}
	}

	public void setAantalGasten(int aantalGasten) {
		if (aantalGasten >= 0) {
			this.aantalGasten = aantalGasten;
		} else {
			System.out.println("Ongeldig aantal gasten");
		}
	}

	public void setBrightness(int brightness) {
		if (brightness >= 0 && brightness <= 100) {
			this.brightness = brightness;
		} else {
			System.out.println("Brightness moet tussen 0 en 100 zijn");
		}
	}

	public void setVolume(int volume) {
		if (volume >= 0 && volume <= 100) {
			this.volume = volume;
		} else {
			System.out.println("Volume moet tussen 0 en 100 zijn");
		}
	}

	public void setScenario(ScenarioType scenario) {
		if (scenario != null) {
			this.scenario = scenario;
		} else {
			System.out.println("Ongeldig scenario");
		}
	}
}