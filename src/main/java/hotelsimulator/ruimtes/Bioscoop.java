package hotelsimulator.ruimtes;

import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;

public class Bioscoop extends HotelRuimte {

	public Bioscoop(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vul kleur, waar hij moet vullen, x en y-1 omdat de grid bij 0 begint en de JSON bij 1 , breedte en hoogte ook want anders is elk kamer 1 pixel
        Color donkerrood = new Color(46,139,87);
        g.setColor(donkerrood);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        Graphics2D g2d = (Graphics2D) g;
        if (isCleaningEmergency()) {
            // Dikke rode rand als teken van cleaning emergency
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
            g2d.setStroke(new BasicStroke(1));
        } else {
            //outline, waar hij een outline moet tekenen
            g.setColor(Color.BLACK);
            g.drawRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
        }
        if (isCleaningEmergency()) {
            // Rood uitroepteken rechtsboven in de kamer
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("!", (x + 1) * cellSize + breedte * cellSize - 10, (y - 1) * cellSize + 14);
        }
        //text kleur, waar hij de string moet neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Bioscoop", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }

    @Override
    public long getVerblijfMs() {
        return 60000;
    }
}
