package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;

public class GoToFitness implements EventStrategie {
    private final Gast gast;
    public GoToFitness(Gast gast){
        this.gast = gast;
    }
    @Override
    public void eventUitvoeren() {

        //In gast methode startGoToFitness starten
gast.startGoToFitness();
    }
}
