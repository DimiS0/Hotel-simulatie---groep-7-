package hotelsimulator.ruimtes;

import java.awt.*;

public class Lobby extends HotelRuimte {

	public Lobby(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur, kleur neerzetten
        g.setColor(Color.darkGray);
        g.fillRect((x+1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);

        //outline kleur, outline neerzetten
        g.setColor(Color.BLACK);
        g.drawRect((x+1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);

        //tekst kleur, tekst neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Lobby", (x+1) * cellSize + 5, (y - 1) * cellSize + 15);
    }

}
