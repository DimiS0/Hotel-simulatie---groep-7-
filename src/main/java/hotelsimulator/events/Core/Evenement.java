package hotelsimulator.events.Core;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;

public class Evenement {

    // hoteleventklasse meegeven aan evenement van hotel event hier wordt dan een strategie gekozen
private EventStrategie eventStrategie;

	public Evenement(HotelEventManager eventManager) {
        eventManager.register(evt -> {
            HotelEventType type = evt.getEventType();

            if (type != hotelevents.HotelEventType.NONE){
                System.out.println(type + " "+ evt.getGuestId());
                setEventStrategie(evenementFactory.verwerken(type));
                eventStrategie.eventUitvoeren();

            }
        });
    }
    public void setEventStrategie(EventStrategie type){
        this.eventStrategie = type;
    }
}

