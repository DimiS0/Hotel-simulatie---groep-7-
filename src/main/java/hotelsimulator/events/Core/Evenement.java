package hotelsimulator.events.Core;

import hotelevents.HotelEventManager;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;

public class Evenement {
    private Hotel hotel;

    // hoteleventklasse meegeven aan evenement van hotel event hier wordt dan een strategie gekozen
private EventStrategie eventStrategie;

	public Evenement(HotelEventManager eventManager, Hotel hotel) {
        this.hotel = hotel;

        eventManager.register(evt -> {
            HotelEventType type = evt.getEventType();

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

