package hotelsimulator.ruimtes;

import java.awt.*;

public class HotelKamer extends HotelRuimte {
	public HotelKamer(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
	}

    @Override
    public void print(Graphics g, int cellSize) {
        //vullkleur, zetten waar de kleur komt
        Color blauw = new Color(74,111,165);
        g.setColor(blauw);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline, outline neerzetten
        g.setColor(Color.BLACK); // outline
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //tekst kleur, tekst neeretten
        g.setColor(Color.WHITE);
        g.drawString("Kamer", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }
    @Override
    public long getVerblijfMs() {
        return 15000;
    }
}