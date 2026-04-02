package hotelsimulator.ruimtes;

import hotelsimulator.config.TimerSim;

import java.awt.*;

public class Lift extends HotelRuimte {
    private boolean omhoog = true;

    public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, TimerSim timerSim) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen, timerSim);
    }
    @Override
    public void print(Graphics g, int cellSize) {
        //vul kleur, waar hij moet vullen, x en y-1 omdat de grid bij 0 begint en de JSON bij 1 , breedte en hoogte ook want anders is elk kamer 1 pixel
        Color zachtrose = new Color(255,128,139);
        g.setColor(zachtrose);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline, waar hij een outline moet tekenen
        g.setColor(Color.BLACK);
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //text kleur, waar hij de string moet neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Lift", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }

        public void liftBwegen() {
            if (omhoog) {
                y--;
                if (y <= 2) omhoog = false;
            } else {
                y++;
                if (y >= 10) omhoog = true;
            }
            System.out.println("y na beweging: " + y);
        }
    }