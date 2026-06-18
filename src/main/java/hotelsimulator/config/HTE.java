package hotelsimulator.config;

public enum HTE implements Snelheid {
    LANGZAMER( 0.25),
    LANGZAAM( 0.5),
    NORMAAL( 1.0),
    SNEL( 2.0),
    VIER_X(4.0);

    private final double factor;

    HTE(double factor) {
        this.factor = factor;
    }

    //factor ophalen
    @Override
    public double getFactor() {
        return factor;
    }

    // berekenen van hte in ms
    @Override
    public int getDelay() {
        return (int) (1000 / getFactor());
    }
}