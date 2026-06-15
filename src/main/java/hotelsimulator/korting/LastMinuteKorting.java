package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class LastMinuteKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    private double [] prijsKamers;
    public LastMinuteKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        this.prijsKamers = receptieScherm.getPrijsKamers();
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.setSaldoDouble(receptieScherm.getSaldoDouble() + prijsKamers[aantSterren] * 0.90);
    }
}
