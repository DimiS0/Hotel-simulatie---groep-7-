package hotelsimulator.ruimtes;

import java.awt.*;

public class Restaurant extends HotelRuimte {
	public Restaurant(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur, kleur neerzetten
        Color color = new Color(209,73,91);
        g.setColor(color);
        g.fillRect((x+1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);


            //outline kleur, outline neerzetten
            g.setColor(Color.BLACK);
            g.drawRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);

        if (isCleaningEmergency()) {
            // Rood uitroepteken rechtsboven in de kamer
            g.setColor(Color.RED);
            g.drawString("⚠", (x + 1) * cellSize + breedte * cellSize - 15, (y - 1) * cellSize + 45);
        }
        //teksten kleur, tekst neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Restaurant", (x+1) * cellSize + 5, (y - 1) * cellSize + 15);
    }
    @Override
    public long getVerblijfMs() {
        return 45000;
    }
}
