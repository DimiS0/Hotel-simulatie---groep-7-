package hotelsimulator.personen;

import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;

public class Schoonmaker extends Persoon {
    private Schacht schacht;
    private Lift lift;


    public Schoonmaker(int startX, int startY, Lift lift, Schacht schacht) {
        super(startX, startY, lift,schacht);
        this.schacht = schacht;
        this.lift = lift;
    }

    @Override
    public void print(Graphics g) {
        // Teken cirkel
        g.setColor(Color.ORANGE);
        g.fillOval(pixelX, pixelY, 20, 20);
        // Teken outline
        g.setColor(Color.BLACK);
        g.drawOval(pixelX, pixelY, 20, 20);
        // Teken letter
        g.setColor(Color.WHITE);
        g.drawString("S", pixelX + 6, pixelY + 14);
    }
}