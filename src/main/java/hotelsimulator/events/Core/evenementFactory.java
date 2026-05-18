package hotelsimulator.events.Core;

import hotelevents.HotelEvent;
import hotelevents.HotelEventType;
import hotelsimulator.core.Hotel;
import hotelsimulator.events.Emergency.CleaningEmergency;
import hotelsimulator.events.Emergency.Evacuate;
import hotelsimulator.events.Emergency.Godzilla;
import hotelsimulator.events.Guest.*;
import hotelsimulator.events.Ruimte.StartCinema;
import hotelsimulator.personen.Gast;

public class evenementFactory {
    public static EventStrategie verwerken(HotelEvent evt, Hotel hotel){

        //de type en guestID opslaan voor als hij nodig is voor events
        HotelEventType type = evt.getEventType();
        int guestId = evt.getGuestId();

        //juiste gedrag willen voor type event
        switch(type){
            case CHECK_IN -> {Gast gast = hotel.zoekGastOpId(guestId); if (gast == null) {System.out.println("Gast niet gevonden met id: " + guestId); return () -> {};} return new CheckIn(gast, evt.getData() );}
            case CHECK_OUT -> {Gast gast = hotel.zoekGastOpId(guestId); if (gast == null) {System.out.println("Gast niet gevonden met id: " + guestId); return () -> {};} return new CheckOut(gast);}
            case CLEANING_EMERGENCY -> {return new CleaningEmergency();}
            case NEED_FOOD -> {Gast gast = hotel.zoekGastOpId(guestId); if (gast == null) {; return () -> {};}return new NeedFood(gast);}
            case GOTO_CINEMA -> { Gast gast = hotel.zoekGastOpId(guestId); if (gast == null) {; return () -> {};}return new GoToCinema(gast);}
            case GOTO_FITNESS -> {Gast gast = hotel.zoekGastOpId(guestId); if (gast == null) {; return () -> {};}return new GoToFitness(gast);}
            case START_CINEMA -> {return new StartCinema(hotel);}
            default -> {throw new IllegalArgumentException(type+" is Geen event type");
        }


        }
    }
}

