package hotelsimulator.config;

public enum HTE {
	LANGZAMER("0.25x", 0.25),
	LANGZAAM("0.5x", 0.5),
	NORMAAL("1.0", 1.0),
	SNEL("2x", 2.0),
	VIER_X("4x", 4.0);

	private final String label;
	private final double factor;

	HTE(String label, double factor) {
		this.label = label;
		this.factor = factor;
	}

	// String label
	public String getLabel() {
		return label;
	}

	public double getFactor() {
		return factor;
	}
}
