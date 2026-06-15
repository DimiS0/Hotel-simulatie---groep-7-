package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;
import hotelsimulator.ruimtes.Restaurant;

public class NeedFood implements EventStrategie {
    private final Gast gast;
    public NeedFood(Gast gast){
        this.gast = gast;
    }
    @Override
    public void eventUitvoeren() {
        //in gast startGoToRestaurant starten
gast.startGoToRestaurant();
    }
}
