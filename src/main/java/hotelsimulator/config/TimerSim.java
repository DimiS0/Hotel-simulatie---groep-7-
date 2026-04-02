package hotelsimulator.config;

import java.util.*;

public class TimerSim {
    private List<Observer> observers = new ArrayList<>();
    private int seconden;
    private int minuten;
    private int uren;
    private int dagen;

    public TimerSim() {
        this.seconden = 0;
        this.minuten = 0;
        this.uren = 0;
        this.dagen = 0;
    }

    public void timeMethod(){
        Timer timer = new Timer(1000, e -> {
            seconden++;
            if (seconden >= 60) {
                seconden = 0;
                minuten++;
                if(minuten >= 60){
                    minuten = 0;
                    uren++;
                    if(uren >= 24){
                        uren = 0;
                        dagen++;
                    }
                }
            }
            notifyObservers(minuten + ":" + seconden);
        });
    }
    }

}