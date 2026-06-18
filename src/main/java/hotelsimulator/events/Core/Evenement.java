package hotelsimulator.events.Core;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;

public class Evenement {

    // hoteleventklasse meegeven aan evenement van hotel event hier wordt dan een strategie gekozen
private EventStrategie eventStrategie;

	public Evenement(HotelEventManager eventManager, Hotel hotel) {

        eventManager.register(evt -> {

            //event opslaan voor de check
            HotelEventType type = evt.getEventType();

            //Als event geen NONE is, printen + naar factory sturen en als strategie zetten. als laatste de strategie uitvoeren
            if (type != hotelevents.HotelEventType.NONE){
                System.out.println(type + " "+ evt.getGuestId());
                setEventStrategie(evenementFactory.verwerken(evt, hotel));
                eventStrategie.eventUitvoeren();

            }
        });
    }
    public void setEventStrategie(EventStrategie type){
        this.eventStrategie = type;
    }
}

