package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LastMinuteKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    public LastMinuteKorting(ReceptieScherm receptieScherm){
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.85);
    }
}
