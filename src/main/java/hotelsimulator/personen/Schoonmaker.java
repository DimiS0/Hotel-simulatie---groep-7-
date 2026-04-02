package hotelsimulator.personen;

import java.awt.*;

public class Schoonmaker extends Persoon {

    public Schoonmaker(int startX, int startY) {
        super(startX, startY);
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