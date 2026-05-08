package hotelsimulator.ruimtes;

import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;

public class FitnessRuimtes extends HotelRuimte {

	public FitnessRuimtes(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur van het vakje, neerzetten waar hij moet vullen
        Color color = new Color(244,162,97);
        g.setColor(color);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);


            //outline, neeretten waar de outline moet komen.
            g.setColor(Color.BLACK);
            g.drawRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);

        if (isCleaningEmergency()) {
            // Rood uitroepteken rechtsboven in de kamer
            g.setColor(Color.RED);
            g.drawString("!", (x + 1) * cellSize + breedte * cellSize - 25, (y - 1) * cellSize + 25);
        }
        //Tekst wit, tekst neerzetten
        g.setColor(Color.WHITE);
        g.drawString("FitnessRuimte", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }
    @Override
    public long getVerblijfMs() {
        return 30000;
    }
}
