package hotelsimulator.ruimtes;

import java.awt.*;
import java.util.ArrayList;

public class Trap extends HotelRuimte {
    private ArrayList<Integer> mogelijkeVerdiepingen = new ArrayList();
    private int [] verdiepingenY;
    private int verdiepingen;
	public Trap(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, int verdiepingen) {
		super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        this.verdiepingen = verdiepingen;
        kiesVerdiepingenY();
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

    public void kiesVerdiepingenY(){
        int p = 2;
        for(int i = 0; i < verdiepingen; i++){
            mogelijkeVerdiepingen.add(p);
            p +=3;
        }
        verdiepingenY = mogelijkeVerdiepingen.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public boolean isBeloopbaar(int gridX, int gridY) {
        boolean isVerdieping = false;
        for(int i = 0; i < verdiepingen; i++){
            if(gridY == mogelijkeVerdiepingen.get(i)){
                isVerdieping = true;
            }
        }
        return isVerdieping;
    }

        @Override
        public int[] getIngangen () {
            return verdiepingenY;
        }
    }

