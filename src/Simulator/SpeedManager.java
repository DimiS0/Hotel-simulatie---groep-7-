package Simulator;

public class SpeedManager {
	private NaarHTEOmzetten n;
	private HTE currentSpeed;

	public SpeedManager() {
		this.n = new NaarHTEOmzetten();
		this.currentSpeed = HTE.NORMAAL;
	}

	public void kiesSnelheid(String Label) {
		currentSpeed = n.fromLabel(Label);
	}

	public void toonSnelheid() {
		System.out.println(currentSpeed.getLabel());
	}
}
