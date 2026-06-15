package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class StudentenKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    private int aantSterren;
    public StudentenKorting(ReceptieScherm receptieScherm, int aantSterren){
        this.aantSterren = aantSterren;
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.85, aantSterren);
    }
}
