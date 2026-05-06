package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;

public class CheckIn implements EventStrategie {

    private Gast gast;

    public CheckIn(Gast gast) {
        this.gast = gast;
    }

    @Override
    public void eventUitvoeren() {
        System.out.println("CheckIn voor gast " + gast.getGuestID());
        gast.handleCheckIn();
    }
}