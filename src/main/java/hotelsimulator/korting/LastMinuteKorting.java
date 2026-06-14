package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LastMinuteKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    public LastMinuteKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.90, aantSterren);
    }
}
