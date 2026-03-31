package hotelsimulator.ruimtes;

import javax.swing.*;
import java.awt.*;

public class Lift extends HotelRuimte {

    public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
    }
    @Override
    public void print(Graphics g, int cellSize) {
        //vul kleur, waar hij moet vullen, x en y-1 omdat de grid bij 0 begint en de JSON bij 1 , breedte en hoogte ook want anders is elk kamer 1 pixel
        Color zachtrose = new Color(255,128,139);
        g.setColor(zachtrose);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline, waar hij een outline moet tekenen
        g.setColor(Color.BLACK);
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //text kleur, waar hij de string moet neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Lift", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }
}