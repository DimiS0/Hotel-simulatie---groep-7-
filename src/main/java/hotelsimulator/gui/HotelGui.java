package hotelsimulator.gui;

import hotelsimulator.config.HTE;
import hotelsimulator.core.Hotel;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;

import static hotelsimulator.config.HTE.*;

public class HotelGui extends JPanel {

    private Hotel hotel;
    private SimulatieConfig config;
    private final int cellSize = 50;
    private ConfigGui configGui;
    private JFrame frame;
    private JLabel speed;
    boolean setDefaultSpeed;

    public HotelGui(Hotel hotel, SimulatieConfig config) {
        this.frame = new JFrame("Hotel Layout");
        this.hotel = hotel;
        this.config = config;
        this.setDefaultSpeed = false;
        this.speed = new JLabel("1x");
        setPreferredSize(new Dimension(10 * cellSize, 10 * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 10; i++) {
            g.drawLine(0, i * cellSize, 10 * cellSize, i * cellSize);
            g.drawLine(i * cellSize, 0, i * cellSize, 10 * cellSize);
        }

        for (HotelRuimte r : hotel.getRuimtes()) {
            r.print(g, cellSize);
        }
    }

    public void showGui() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(this, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        JButton instellingenBtn = new JButton("Instellingen");

        topPanel.add(instellingenBtn);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(speed, BorderLayout.SOUTH);

        instellingenBtn.addActionListener(e -> {
            new ConfigGui(config, value -> updateSpeedLabel(value));
        });

        //frame pack past grootte aan van venster zodat alle knoppen precies passen
        frame.pack();

        //zet het venster in het midden van de scherm, null -> in het midden van je hele beeldscherm
        frame.setLocationRelativeTo(null);

        //bij het openen van hotelGui lezen we snelheid van config, want zodat snelheid in starterGui meegnomen wordt.
        updateSpeedLabel(mapHTEToSlider(config.getSnelheid()));

        frame.setVisible(true);
    }

    public void updateSpeedLabel(int value) {
        switch (value) {
            case 1 :
                frame.remove(speed);
                frame.revalidate();
                frame.repaint();
                speed.setText("0.25x");
                break;
            case 2:
                frame.remove(speed);
                frame.revalidate();
                frame.repaint();
                speed.setText("0.50x");
                break;
            case 3:
                frame.remove(speed);
                frame.revalidate();
                frame.repaint();
                speed.setText("1.0x");
                break;
            case 4:
                frame.remove(speed);
                frame.revalidate();
                frame.repaint();
                speed.setText("2.0x");
                break;
            case 5:
                frame.remove(speed);
                frame.revalidate();
                frame.repaint();
                speed.setText("4.0x");
                break;
        }
        frame.add(speed, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // vertaalt HTE waarde naar getal, dus snel -> 4 en die wordt 2.0x
    private int mapHTEToSlider(HTE hte) {
        return switch (hte) {
            case LANGZAMER -> 1;
            case LANGZAAM  -> 2;
            case NORMAAL   -> 3;
            case SNEL      -> 4;
            case VIER_X    -> 5;
            default        -> 3;
        };
    }
}