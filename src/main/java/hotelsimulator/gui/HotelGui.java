package hotelsimulator.gui;

import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;

public class HotelGui extends JPanel {

    private Hotel hotel;
    private final int cellSize = 50; // grootte van elke “cel”

    public HotelGui(Hotel hotel) {
        this.hotel = hotel;
        setPreferredSize(new Dimension(10*cellSize, 10*cellSize)); // grid 10x10
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // teken achtergrond grid
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 10; i++) {
            g.drawLine(0, i*cellSize, 10*cellSize, i*cellSize);
            g.drawLine(i*cellSize, 0, i*cellSize, 10*cellSize);
        }

        // teken kamers via hun eigen print() methode
        for (HotelRuimte r : hotel.getRuimtes()) {
            r.print(g, cellSize);
        }
    }

    public void showGui() {
        JFrame frame = new JFrame("Hotel Layout");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }
}