package hotelsimulator.gui;

import hotelsimulator.core.Hotel;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;

public class HotelGui extends JPanel {

    private Hotel hotel;
    private SimulatieConfig config;
    private final int cellSize = 50;

    public HotelGui(Hotel hotel, SimulatieConfig config) {
        this.hotel = hotel;
        this.config = config;
        setPreferredSize(new Dimension(10 * cellSize, 10 * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // grid tekenen
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 10; i++) {
            g.drawLine(0, i * cellSize, 10 * cellSize, i * cellSize);
            g.drawLine(i * cellSize, 0, i * cellSize, 10 * cellSize);
        }

        // kamers tekenen
        for (HotelRuimte r : hotel.getRuimtes()) {
            r.print(g, cellSize);
        }
    }

    public void showGui() {
        JFrame frame = new JFrame("Hotel Layout");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());


        frame.add(this, BorderLayout.CENTER);


        JPanel topPanel = new JPanel();
        JButton instellingenBtn = new JButton("Instellingen");

        topPanel.add(instellingenBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        instellingenBtn.addActionListener(e -> {
            new ConfigGui(config);
        });

        frame.pack();
        frame.setLocationRelativeTo(null); // center scherm
        frame.setVisible(true);
    }
}