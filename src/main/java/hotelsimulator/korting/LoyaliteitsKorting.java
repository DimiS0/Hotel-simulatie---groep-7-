package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LoyaliteitsKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    public LoyaliteitsKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.80, aantSterren);
    }
}
