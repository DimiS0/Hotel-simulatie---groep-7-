package hotelsimulator.config;

public interface Snelheid {

    // Vermenigvuldigingsfactor voor bewegingssnelheid (bijv. 0.25, 1.0, 4.0).
    double getFactor();

    // Vertraging in milliseconden voor de event-loop timer.
    int getDelay();

}
