package hotelsimulator.personen;

import hotelsimulator.ruimtes.Lift;

import java.awt.*;

public class Gast extends Persoon {
    private Lift lift;
    private int stopVerdieping;

    public Gast(int startX, int startY,int stopVerdieping, Lift lift) {
        super(startX, startY);
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
    public void liftVerzoek(){
        lift.roepLiftNaar(stopVerdieping);
    }
    public int getStopVerdieping(){
        return stopVerdieping;
    }
    public void setStopVerdieping(int stopVerdieping){
        this.stopVerdieping = stopVerdieping;
    }

    public void inChecken() {}
    public void uitChecken() {}
}