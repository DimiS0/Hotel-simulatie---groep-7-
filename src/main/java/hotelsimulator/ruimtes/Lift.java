package hotelsimulator.ruimtes;

import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Lift extends HotelRuimte {
    private int[] verdiepingenY = {8, 5, 2};
    private int stopPositie = 8;
    private int doelStopVerdieping = 8;
    private boolean beschikbaar = true;
    private boolean omhoog = true;
    private Hotel hotel;

    private final List<Gast> gastenInLift = new ArrayList<>();
    private final Map<Integer, List<Gast>> wachtrij = new HashMap<>();

    public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, Hotel hotel) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        this.hotel = hotel;
        for (int stop : verdiepingenY) {
            wachtrij.put(stop, new ArrayList<>());
        }
    }

    @Override
    public void print(Graphics g, int cellSize) {
        Color zachtrose = new Color(255, 128, 139);
        g.setColor(zachtrose);
        g.fillRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
        g.setColor(Color.BLACK);
        g.drawRect((x + 1) * cellSize, (y - 1) * cellSize, breedte * cellSize, hoogte * cellSize);
        g.setColor(Color.WHITE);
        g.drawString("Lift", (x + 1) * cellSize + 5, (y - 1) * cellSize + 15);
    }

    public void liftBwegen() {
        if (beschikbaar) return;

        // Beweeg één stap richting doel
        if (y > doelStopVerdieping) {
            y--;
            omhoog = true;
        } else if (y < doelStopVerdieping) {
            y++;
            omhoog = false;
        }

        // Tussenhalte: stop als iemand hier wil in/uitstappen ÉN het ligt op de weg
        if (y != doelStopVerdieping) {
            for (int stop : verdiepingenY) {
                if (y == stop) {
                    boolean uitstappers = gastenInLift.stream().anyMatch(g -> g.getDoelVerdieping() == stop);
                    List<Gast> wachtend = wachtrij.getOrDefault(stop, Collections.emptyList());
                    boolean opDeWeg = omhoog
                            ? (stop >= doelStopVerdieping)   // omhoog: stop moet hoger of gelijk aan doel liggen
                            : (stop <= doelStopVerdieping);  // omlaag: stop moet lager of gelijk liggen
                    if ((uitstappers || !wachtend.isEmpty()) && opDeWeg) {
                        ladenEnLossen(stop);
                    }
                }
            }
        }

        // Aankomst bij bestemming
        if (y == doelStopVerdieping) {
            ladenEnLossen(doelStopVerdieping);
            stopPositie = doelStopVerdieping;

            // Zijn er nog passagiers die ergens heen willen?
            if (!gastenInLift.isEmpty()) {
                int volgendeDoel = gastenInLift.get(0).getDoelVerdieping();
                if (!hotel.getLiftOproepen().isEmpty()) {
                    hotel.getLiftOproepen().removeFirst();
                }
                roepLiftNaar(volgendeDoel);
            } else {
                if (!hotel.getLiftOproepen().isEmpty()) {
                    hotel.getLiftOproepen().removeFirst();
                }
                beschikbaar = true;
                if (!hotel.getLiftOproepen().isEmpty()) {
                    roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
        }
    }

    private void ladenEnLossen(int verdieping) {
        // Uitstappen
        Iterator<Gast> it = gastenInLift.iterator();
        while (it.hasNext()) {
            Gast g = it.next();
            if (g.getDoelVerdieping() == verdieping) {
                g.stapUit((verdieping - 1) * 50 + 25);
                it.remove();
            }
        }

        // Instappen (maximaal maxPersonen)
        List<Gast> wachtend = wachtrij.getOrDefault(verdieping, new ArrayList<>());
        for (Gast g : new ArrayList<>(wachtend)) {
            if (gastenInLift.size() < maxPersonen) {
                g.stapIn();
                gastenInLift.add(g);
                wachtend.remove(g);

                // Voeg bestemming van instappende gast toe aan wachtrij
                int doel = g.getDoelVerdieping();
                if (!hotel.getLiftOproepen().contains(doel)) {
                    hotel.getLiftOproepen().add(doel);
                }
            }
        }
    }

    /**
     * Registreer een wachtende gast. Roep lift op als logisch qua richting.
     */
    public void voegWachtendeGastToe(Gast gast, int verdieping) {
        wachtrij.computeIfAbsent(verdieping, k -> new ArrayList<>()).add(gast);

        if (beschikbaar) {
            hotel.getLiftOproepen().add(verdieping);
            roepLiftNaar(verdieping);
        } else if (isOpDeWeg(verdieping)) {
            // Lift komt hier langs — wordt afgehandeld in liftBwegen
        } else {
            // Niet op de weg: plan voor na de huidige rit
            hotel.getLiftOproepen().add(verdieping);
        }
    }

    private boolean isOpDeWeg(int verdieping) {
        if (omhoog) {
            return verdieping <= y && verdieping >= doelStopVerdieping;
        } else {
            return verdieping >= y && verdieping <= doelStopVerdieping;
        }
    }

    public void roepLiftNaar(int verdieping) {
        doelStopVerdieping = verdieping;
        beschikbaar = false;
        omhoog = verdieping < y;
    }

    public int getCurrentLiftY() { return y; }
    public int[] getVerdiepingenY() { return verdiepingenY; }
    public boolean getBeschikbaar() { return beschikbaar; }
    public int getStopPositie() { return stopPositie; }
}