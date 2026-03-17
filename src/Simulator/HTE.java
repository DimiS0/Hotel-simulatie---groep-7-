package Simulator;
public enum HTE {
    LANGZAAM("0.5x", 0.5),
    NORMAAL("1.0", 1.0),
    SNEL("2.0", 2.0),
    VIER_X("4.0", 4.0);

    private final String label;
    private final double factor;

    HTE(String label, double factor) {
        this.label = label;
        this.factor = factor;
    }

    //
    public String getLabel() {
        return label;
    }

    public double getFactor() {
        return factor;
    }
}
