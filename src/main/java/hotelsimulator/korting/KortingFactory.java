package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class KortingFactory {
    public KortingFactory (String soortKorting, ReceptieScherm receptieScherm, int aantSterren){
        //simpele factory om objecten aan te maken
        switch(soortKorting){
            case "StudentenKorting" -> new StudentenKorting(receptieScherm, aantSterren);
            case "LoyaliteitsKorting" -> new LoyaliteitsKorting(receptieScherm, aantSterren);
            case "LastMinuteKorting" -> new LastMinuteKorting(receptieScherm, aantSterren);
            case "GEENKORTING" -> new GeenKorting(receptieScherm, aantSterren);
        }
    }
}
