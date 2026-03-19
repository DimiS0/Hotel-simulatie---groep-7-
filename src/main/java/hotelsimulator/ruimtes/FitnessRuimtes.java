package hotelsimulator.ruimtes;

import java.awt.*;

public class FitnessRuimtes extends HotelRuimte {

	public FitnessRuimtes(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        g.setColor(Color.ORANGE); // vulkleur
        g.fillRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);
        g.setColor(Color.BLACK); // outline
        g.drawRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);
        g.setColor(Color.WHITE);
        g.drawString("FitnessRuimte", (x-1)*cellSize + 5, (y-1)*cellSize + 15);
}}
