package hotelsimulator.ruimtes;

import java.awt.*;

public class Lift extends HotelRuimte {
	public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur, kleur neeretten
        g.setColor(Color.GRAY);
        g.fillRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline, outline neerzetten
        g.setColor(Color.BLACK); // outline
        g.drawRect((x-1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //tekst kleur, tekst locatie
        g.setColor(Color.WHITE);
        g.drawString("Lift", (x-1)*cellSize + 5, (y-1)*cellSize + 15);
}}
