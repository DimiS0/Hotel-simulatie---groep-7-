package hotelsimulator.ruimtes;

import java.awt.*;

public class Restaurant extends HotelRuimte {
	public Restaurant(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        g.setColor(Color.MAGENTA); // vulkleur
        g.fillRect((x - 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
        g.setColor(Color.BLACK); // outline
        g.drawRect((x - 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
        g.setColor(Color.WHITE);
        g.drawString("Restaurant", (x - 1) * cellSize + 5, (y - 1) * cellSize + 15);
    }
}
