package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;
import hotelsimulator.ruimtes.Bioscoop;


public class GoToCinema implements EventStrategie {
    private final Gast gast;

    public GoToCinema(Gast gast) {
        this.gast = gast;
    }

    @Override
    public void eventUitvoeren() {
        gast.startGoToBioscoop();
    }
}