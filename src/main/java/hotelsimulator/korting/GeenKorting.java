package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class GeenKorting implements SoortKortingen{
    private ReceptieScherm receptieScherm;
    public GeenKorting(ReceptieScherm receptieScherm){
        this.receptieScherm = receptieScherm;
        kortingToepassen();
    }
    @Override
    public void kortingToepassen(){
        receptieScherm.receptie(0.85);
    }
}
