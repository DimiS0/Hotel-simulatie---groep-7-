package hotelsimulator.personen;

import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;

public class Gast extends Persoon {
    private Schacht schacht;
    private Lift lift;
    private int stopVerdieping;


    public Gast(int startX, int startY,int stopVerdieping, Lift lift, Schacht schacht) {
        super(startX, startY,lift,schacht);
        this.schacht = schacht;
        this.lift = lift;
        this.stopVerdieping = stopVerdieping;
    }

    @Override
    public void print(Graphics g) {
        // Teken cirkel
        g.setColor(Color.BLUE);
        g.fillOval(pixelX, pixelY, 20, 20);
        // Teken outline
        g.setColor(Color.BLACK);
        g.drawOval(pixelX, pixelY, 20, 20);
        // Teken letter
        g.setColor(Color.WHITE);
        g.drawString("G", pixelX + 6, pixelY + 14);
    }
}