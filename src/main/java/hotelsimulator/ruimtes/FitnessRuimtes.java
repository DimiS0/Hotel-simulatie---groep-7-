package hotelsimulator.ruimtes;

import java.awt.*;

public class FitnessRuimtes extends HotelRuimte {

	public FitnessRuimtes(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
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
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //Tekst wit, tekst neerzetten
        g.setColor(Color.WHITE);
        g.drawString("FitnessRuimte", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }
    @Override
    public long getVerblijfMs() {
        return 30000;
    }
}
