package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class GeenKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    public GeenKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(1.0, aantSterren);
    }
}
