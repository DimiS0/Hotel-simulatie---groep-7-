package hotelsimulator.personen;

import java.awt.*;

public class Gast extends Persoon {

    public Gast(int startX, int startY) {
        super(startX, startY);
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

    public void inChecken() {}
    public void uitChecken() {}
}