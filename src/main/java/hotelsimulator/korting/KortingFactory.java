package hotelsimulator.korting;

import hotelsimulator.gui.ReceptieScherm;

public class KortingFactory {
    public KortingFactory (String soortKorting, ReceptieScherm receptieScherm){
        switch(soortKorting){
            case "StudentenKorting" -> new StudentenKorting(receptieScherm);
            case "LoyaliteitsKorting" -> new LoyaliteitsKorting(receptieScherm);
            case "LastMinuteKorting" -> new LastMinuteKorting(receptieScherm);
            case "GEENKORTING" -> new GeenKorting(receptieScherm);
        }
    }
}
