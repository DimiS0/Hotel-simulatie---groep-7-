package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class GeenKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    private double [] prijsKamers;
    public GeenKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        this.prijsKamers = receptieScherm.getPrijsKamers();
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        //berekening maken
        receptieScherm.setSaldoDouble(receptieScherm.getSaldoDouble() + prijsKamers[aantSterren]);
    }
}
