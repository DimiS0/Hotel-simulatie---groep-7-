package hotelsimulator.events.Guest;

import hotelsimulator.events.Core.EventStrategie;
import hotelsimulator.personen.Gast;

public class CheckIn implements EventStrategie {

    private Gast gast;
    private int gewildeSterren;

    public CheckIn(Gast gast, int data) {
        this.gast = gast;
        this.gewildeSterren = data;
    }

    @Override
    public void eventUitvoeren() {
        //kijken welke gast inchekt + handlecheckin methode afspelen + eventmanager data meegeven
        System.out.println("CheckIn voor gast " + gast.getGuestID());
        gast.handleCheckIn(gewildeSterren);
    }
}