package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LoyaliteitsKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    public LoyaliteitsKorting(ReceptieScherm receptieScherm){
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.80);
    }
}
