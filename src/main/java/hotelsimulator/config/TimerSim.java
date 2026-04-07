package hotelsimulator.config;

import hotelsimulator.gui.HotelGui;
import hotelsimulator.ruimtes.Lift;

import javax.swing.Timer;

public class TimerSim {
    private int seconden;
    private int minuten;
    private int uren;
    private int dagen;
    private int factor;
    private Timer timer;


    public TimerSim() {
        this.seconden = 0;
        this.minuten = 0;
        this.uren = 0;
        this.dagen = 0;
        this.factor = 1;
    }
    public void updateTimerFactor(int factor){
        this.factor = factor;
        if (timer != null){
            timer.setDelay(1000/this.factor);
        }
    }

    public void timeMethod(Lift lift, HotelGui gui) {
        timer = new Timer((1000/factor), e -> {
            seconden++;
            if (seconden >= 60) {
                seconden = 0;
                minuten++;
                if (minuten == 60) {
                    minuten = 0;
                    uren++;
                    if (uren >= 24) {
                        uren = 0;
                        dagen++;
                    }
                }
            }
             lift.liftBwegen();
             gui.repaint();
            System.out.println(dagen + ":" + uren + ":" + minuten + ":" + seconden);
        });
        timer.start();
    }
}