package hotelsimulator.events.Core;

import hotelevents.HotelEventType;
import hotelsimulator.events.Emergency.CleaningEmergency;
import hotelsimulator.events.Emergency.Evacuate;
import hotelsimulator.events.Emergency.Godzilla;
import hotelsimulator.events.Guest.*;
import hotelsimulator.events.Ruimte.StartCinema;

public class evenementFactory {
    public static EventStrategie verwerken(HotelEventType type){
        switch(type){
            case CHECK_IN -> {return new CheckIn();}
            case CHECK_OUT -> {return new CheckOut();}
            case CLEANING_EMERGENCY -> {return new CleaningEmergency();}
            case EVACUATE -> {return new Evacuate();}
            case GODZILLA -> {return new Godzilla();}
            case NEED_FOOD -> {return new NeedFood();}
            case GOTO_CINEMA -> {return new GoToCinema();}
            case GOTO_FITNESS -> {return new GoToFitness();}
            case START_CINEMA -> {return new StartCinema();}
            default -> {throw new IllegalArgumentException(type+" is Geen event type");
        }


        }
    }
}
