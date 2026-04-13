package hotelsimulator.gui;

import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;
import hotelsimulator.ruimtes.HotelRuimte;

import javax.swing.*;
import java.awt.*;

public class HotelOverzicht extends JFrame {

    private final Hotel hotel;
    private final JTextArea tekst = new JTextArea();

    public HotelOverzicht(Hotel hotel) {
        this.hotel = hotel;

        setTitle("Hotel Overzicht");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tekst.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tekst.setEditable(false);

        add(new JScrollPane(tekst), BorderLayout.CENTER);

        setVisible(true);

        new Timer(500, e -> verversen()).start();
    }

    private void verversen() {
        StringBuilder sb = new StringBuilder();
        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            long aantal = hotel.getPersonen().stream()
                    .filter(p -> p.getHuidigeRuimte() == ruimte)
                    .count();
            if (aantal > 0)
                sb.append(ruimte.getAreaType()).append(": ").append(aantal).append("\n");
        }
        long totaal = hotel.getPersonen().stream()
                .filter(p -> p instanceof Gast).count();
        sb.append("\nTotaal gasten: ").append(totaal);
        tekst.setText(sb.toString());
    }
}