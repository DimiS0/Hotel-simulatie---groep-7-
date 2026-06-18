package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;

public class CheckOut implements EventStrategie {
    private Gast gast;
    public CheckOut(Gast gast) {
        this.gast = gast;
    }

    @Override
    public void eventUitvoeren() {
        //checkout handle in gast uitvoeren
        gast.checkOutHandleIn();

    }
}
