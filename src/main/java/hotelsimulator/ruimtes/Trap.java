package hotelsimulator.ruimtes;

import java.awt.*;

public class Trap extends HotelRuimte {
    private int verdiepingen;
	public Trap(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, int verdiepingen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        this.verdiepingen = verdiepingen;
	}
    @Override
    public void print(Graphics g, int cellSize) {
        //vulkleur, vulkleur zetten
        Color color = new Color(233,196,106);
        g.setColor(color);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline kleur, outline zetten
        g.setColor(Color.BLACK); // outline
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //tekst kleur, kleur neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Trap", (x+1)*cellSize + 5, (y-1)*cellSize + 15);}

    @Override
    public boolean isBeloopbaar(int gridX, int gridY) {
        switch(verdiepingen){
            case 1:
                return gridY == 8;
            case 2:
                return gridY == 8 || gridY == 5;
            default:
                return gridY == 8 || gridY == 5 || gridY == 2;
        }
    }

        @Override
        public int[] getIngangen () {
            return new int[]{2, 5, 8};
        }
    }

