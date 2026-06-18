package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LoyaliteitsKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    private double [] prijsKamers;
    public LoyaliteitsKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        this.prijsKamers = receptieScherm.getPrijsKamers();
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.setSaldoDouble(receptieScherm.getSaldoDouble() + prijsKamers[aantSterren] * 0.80);
    }
}
