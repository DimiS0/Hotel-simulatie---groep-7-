package hotelsimulator.ruimtes;

import hotelsimulator.core.Hotel;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Schoonmaker;

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

    // Gasten in de lift
    private final List<Gast> gastenInLift = new ArrayList<>();
    private final Map<Integer, List<Gast>> gastWachtrij = new HashMap<>();

    // Schoonmakers in de lift
    private final List<Schoonmaker> schoonmakersInLift = new ArrayList<>();
    private final Map<Integer, List<Schoonmaker>> schoonmakerWachtrij = new HashMap<>();

    public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, Hotel hotel) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        this.hotel = hotel;
        for (int stop : verdiepingenY) {
            gastWachtrij.put(stop, new ArrayList<>());
            schoonmakerWachtrij.put(stop, new ArrayList<>());
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

        if (y > doelStopVerdieping) {
            y--;
            omhoog = true;
        } else if (y < doelStopVerdieping) {
            y++;
            omhoog = false;
        }

        if (y != doelStopVerdieping) {
            for (int stop : verdiepingenY) {
                if (y == stop) {
                    boolean uitstappersGast = gastenInLift.stream().anyMatch(g -> g.getDoelVerdieping() == stop);
                    boolean uitstappersSchoonmaker = schoonmakersInLift.stream().anyMatch(s -> s.getDoelVerdieping() == stop);
                    List<Gast> wachtendGast = gastWachtrij.getOrDefault(stop, Collections.emptyList());
                    List<Schoonmaker> wachtendSchoonmaker = schoonmakerWachtrij.getOrDefault(stop, Collections.emptyList());
                    boolean opDeWeg = omhoog
                            ? (stop >= doelStopVerdieping)
                            : (stop <= doelStopVerdieping);
                    if ((uitstappersGast || uitstappersSchoonmaker || !wachtendGast.isEmpty() || !wachtendSchoonmaker.isEmpty()) && opDeWeg) {
                        ladenEnLossen(stop);
                    }
                }
            }
        }

        if (y == doelStopVerdieping) {
            ladenEnLossen(doelStopVerdieping);
            stopPositie = doelStopVerdieping;

            boolean nogPassagiers = !gastenInLift.isEmpty() || !schoonmakersInLift.isEmpty();
            if (nogPassagiers) {
                int volgendeDoel;
                if (!gastenInLift.isEmpty()) {
                    volgendeDoel = gastenInLift.get(0).getDoelVerdieping();
                } else {
                    volgendeDoel = schoonmakersInLift.get(0).getDoelVerdieping();
                }
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
        // Gasten uitstappen
        Iterator<Gast> gastIt = gastenInLift.iterator();
        while (gastIt.hasNext()) {
            Gast g = gastIt.next();
            if (g.getDoelVerdieping() == verdieping) {
                g.stapUit((verdieping - 1) * 50 + 25);
                gastIt.remove();
            }
        }

        // Schoonmakers uitstappen
        Iterator<Schoonmaker> schoonmakerIt = schoonmakersInLift.iterator();
        while (schoonmakerIt.hasNext()) {
            Schoonmaker s = schoonmakerIt.next();
            if (s.getDoelVerdieping() == verdieping) {
                s.stapUit((verdieping - 1) * 50 + 25);
                schoonmakerIt.remove();
            }
        }

        // Gasten instappen
        List<Gast> wachtendGast = gastWachtrij.getOrDefault(verdieping, new ArrayList<>());
        for (Gast g : new ArrayList<>(wachtendGast)) {
            if (gastenInLift.size() + schoonmakersInLift.size() < maxPersonen) {
                g.stapIn();
                gastenInLift.add(g);
                wachtendGast.remove(g);

                int doel = g.getDoelVerdieping();
                if (!hotel.getLiftOproepen().contains(doel)) {
                    hotel.getLiftOproepen().add(doel);
                }
            }
        }

        // Schoonmakers instappen
        List<Schoonmaker> wachtendSchoonmaker = schoonmakerWachtrij.getOrDefault(verdieping, new ArrayList<>());
        for (Schoonmaker s : new ArrayList<>(wachtendSchoonmaker)) {
            if (gastenInLift.size() + schoonmakersInLift.size() < maxPersonen) {
                s.stapIn();
                schoonmakersInLift.add(s);
                wachtendSchoonmaker.remove(s);

                int doel = s.getDoelVerdieping();
                if (!hotel.getLiftOproepen().contains(doel)) {
                    hotel.getLiftOproepen().add(doel);
                }
            }
        }
    }

    public void voegWachtendeGastToe(Gast gast, int verdieping) {
        if (gast == null) return;
        gastWachtrij.computeIfAbsent(verdieping, k -> new ArrayList<>()).add(gast);

        if (beschikbaar) {
            hotel.getLiftOproepen().add(verdieping);
            roepLiftNaar(verdieping);
        } else if (!isOpDeWeg(verdieping)) {
            hotel.getLiftOproepen().add(verdieping);
        }
    }

    public void voegWachtendeSchoonmakerToe(Schoonmaker schoonmaker, int verdieping) {
        schoonmakerWachtrij.computeIfAbsent(verdieping, k -> new ArrayList<>()).add(schoonmaker);

        if (beschikbaar) {
            hotel.getLiftOproepen().add(verdieping);
            roepLiftNaar(verdieping);
        } else if (!isOpDeWeg(verdieping)) {
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
