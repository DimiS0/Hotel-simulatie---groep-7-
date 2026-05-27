package hotelsimulator.ruimtes;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.*;

public class HotelKamer extends HotelRuimte {
    private boolean gereserveerd = false;

	public HotelKamer(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
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
            g.drawString("Kamer", (x + 1) * cellSize + 5, (y - 1) * cellSize + 15);

        if (isCleaningEmergency()) {
            // Rood uitroepteken rechtsboven in de kamer
            g.setColor(Color.RED);
            g.drawString("⚠", (x + 1) * cellSize + breedte * cellSize - 15, (y - 1) * cellSize + 45);
        }
    }
    @Override
    public long getVerblijfMs() {
        return 15000;
    }

    public boolean isGereserveerd() {
        return gereserveerd;
    }

    public void reserveer() {
        gereserveerd = true;
    }

    public void maakReserveringVrij() {
        gereserveerd = false;
    }
}