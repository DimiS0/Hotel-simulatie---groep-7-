package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class StudentenKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    public StudentenKorting(ReceptieScherm receptieScherm){
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.85);
    }
}
