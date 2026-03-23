package hotelsimulator.ruimtes;

import java.awt.*;

public class Trap extends HotelRuimte {
	public Trap(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur, vulkleur zetten
        g.setColor(Color.ORANGE);
        g.fillRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline kleur, outline zetten
        g.setColor(Color.BLACK); // outline
        g.drawRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //tekst kleur, kleur neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Trap", (x-1)*cellSize + 5, (y-1)*cellSize + 15);}
}
