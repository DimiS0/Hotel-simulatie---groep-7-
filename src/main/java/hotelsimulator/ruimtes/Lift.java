package hotelsimulator.ruimtes;

import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Schoonmaker;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Lift extends HotelRuimte {

    // ── Verdiepingen waar de lift kan stoppen ────────────────
    private int[] verdiepingenY = {8, 5, 2};

    // ── Rijgedrag ────────────────────────────────────────────
    private int  doelVerdieping = 8;
    private int  stopPositie    = 8;
    private boolean beschikbaar = true;
    private boolean omhoog      = true;

    // ── Passagiers ───────────────────────────────────────────
    private final List<Gast>       gastenInLift        = new ArrayList<>();
    private final List<Schoonmaker> schoonmakersInLift  = new ArrayList<>();

    // ── Wachtrijen per verdieping ────────────────────────────
    private final Map<Integer, List<Gast>>       gastWachtrij        = new HashMap<>();
    private final Map<Integer, List<Schoonmaker>> schoonmakerWachtrij = new HashMap<>();

    // ── Verzoekenwachtrij: verdiepingen die nog bezocht moeten worden ──
    // Dit vervangt hotel.getLiftOproepen() als bron van waarheid.
    private final LinkedList<Integer> verzoeken = new LinkedList<>();

    public Lift(String areaType, int sterrenAantal, int y, int x,
                int breedte, int hoogte, int maxPersonen) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        for (int stop : verdiepingenY) {
            gastWachtrij.put(stop, new ArrayList<>());
            schoonmakerWachtrij.put(stop, new ArrayList<>());
        }
    }

    // ────────────────────────────────────────────────────────
    // Wordt elke frame aangeroepen vanuit HotelGui / SimulatieLus
    // ────────────────────────────────────────────────────────
    public void liftBwegen() {
        if (beschikbaar) return;

        // ── Beweeg één stap richting doel ────────────────────
        if (y > doelVerdieping) {
            y--;
            omhoog = true;
        } else if (y < doelVerdieping) {
            y++;
            omhoog = false;
        }

        // ── Tussenstop: iemand moet hier in of uit ───────────
        if (y != doelVerdieping) {
            for (int stop : verdiepingenY) {
                if (y == stop && isRelevanteStop(stop)) {
                    ladenEnLossen(stop);
                }
            }
            return;
        }

        // ── Eindstop bereikt ─────────────────────────────────
        ladenEnLossen(doelVerdieping);
        stopPositie = doelVerdieping;

        // Verwijder dit verzoek uit de wachtrij
        verzoeken.remove(Integer.valueOf(doelVerdieping));

        // Zijn er nog passagiers in de lift met een doel?
        if (!gastenInLift.isEmpty() || !schoonmakersInLift.isEmpty()) {
            int volgendeDoel = !gastenInLift.isEmpty()
                    ? gastenInLift.get(0).getDoelVerdieping()
                    : schoonmakersInLift.get(0).getDoelVerdieping();
            voegVerzoekToe(volgendeDoel);
            rijNaar(volgendeDoel);
            return;
        }

        // Lift is leeg — pak het volgende verzoek uit de wachtrij
        if (!verzoeken.isEmpty()) {
            int volgend = verzoeken.getFirst();
            rijNaar(volgend);
        } else {
            // Geen verzoeken meer — lift staat stil
            beschikbaar = true;
        }
    }

    // ────────────────────────────────────────────────────────
    // Instappen en uitstappen op een verdieping
    // ────────────────────────────────────────────────────────
    private void ladenEnLossen(int verdieping) {

        // Gasten uitstappen
        Iterator<Gast> gi = gastenInLift.iterator();
        while (gi.hasNext()) {
            Gast g = gi.next();
            if (g.getDoelVerdieping() == verdieping) {
                g.stapUit((verdieping - 1) * 50 + 25);
                gi.remove();
            }
        }

        // Schoonmakers uitstappen
        Iterator<Schoonmaker> si = schoonmakersInLift.iterator();
        while (si.hasNext()) {
            Schoonmaker s = si.next();
            if (s.getDoelVerdieping() == verdieping) {
                s.stapUit((verdieping - 1) * 50 + 25);
                si.remove();
            }
        }

        // Gasten instappen
        List<Gast> wG = gastWachtrij.getOrDefault(verdieping, new ArrayList<>());
        for (Gast g : new ArrayList<>(wG)) {
            if (gastenInLift.size() + schoonmakersInLift.size() < maxPersonen) {
                g.stapIn();
                gastenInLift.add(g);
                wG.remove(g);
                // Zorg dat het doel van deze gast ook in de wachtrij zit
                voegVerzoekToe(g.getDoelVerdieping());
            }
        }

        // Schoonmakers instappen
        List<Schoonmaker> wS = schoonmakerWachtrij.getOrDefault(verdieping, new ArrayList<>());
        for (Schoonmaker s : new ArrayList<>(wS)) {
            if (gastenInLift.size() + schoonmakersInLift.size() < maxPersonen) {
                s.stapIn();
                schoonmakersInLift.add(s);
                wS.remove(s);
                voegVerzoekToe(s.getDoelVerdieping());
            }
        }
    }

    // ────────────────────────────────────────────────────────
    // Publieke aanroep-methodes (vanuit Gast / Schoonmaker)
    // ────────────────────────────────────────────────────────

    /**
     * Voeg een wachtende gast toe op de opgegeven verdieping.
     * Lift wordt automatisch ingeschakeld als hij stilstaat.
     */
    public void voegWachtendeGastToe(Gast gast, int verdieping) {
        if (gast == null) return;
        gastWachtrij.computeIfAbsent(verdieping, k -> new ArrayList<>()).add(gast);
        voegVerzoekToe(verdieping);
        startAlsNodig(verdieping);
    }

    /**
     * Voeg een wachtende schoonmaker toe op de opgegeven verdieping.
     */
    public void voegWachtendeSchoonmakerToe(Schoonmaker schoonmaker, int verdieping) {
        if (schoonmaker == null) return;
        schoonmakerWachtrij.computeIfAbsent(verdieping, k -> new ArrayList<>()).add(schoonmaker);
        voegVerzoekToe(verdieping);
        startAlsNodig(verdieping);
    }

    /**
     * Directe aanroep: lift moet naar deze verdieping.
     * Gebruikt door Persoon.liftVerzoek().
     */
    public void roepLiftNaar(int verdieping) {
        voegVerzoekToe(verdieping);
        startAlsNodig(verdieping);
    }

    // ────────────────────────────────────────────────────────
    // Interne hulpmethodes
    // ────────────────────────────────────────────────────────

    /** Voegt verdieping toe aan de wachtrij als hij er nog niet in zit. */
    private void voegVerzoekToe(int verdieping) {
        if (!verzoeken.contains(verdieping)) {
            verzoeken.addLast(verdieping);
        }
    }

    /** Start de lift richting de opgegeven verdieping als hij stilstaat. */
    private void startAlsNodig(int verdieping) {
        if (beschikbaar) {
            rijNaar(verdieping);
        }
    }

    /** Zet de lift in beweging richting een verdieping. */
    private void rijNaar(int verdieping) {
        doelVerdieping = verdieping;
        beschikbaar    = false;
        omhoog         = verdieping < y;
    }

    /**
     * Checkt of er een reden is om op deze tussenstop te stoppen:
     * iemand wil hier uitstappen OF iemand staat hier te wachten
     * in de richting die de lift op gaat.
     */
    private boolean isRelevanteStop(int stop) {
        boolean uitstapper = gastenInLift.stream().anyMatch(g -> g.getDoelVerdieping() == stop)
                || schoonmakersInLift.stream().anyMatch(s -> s.getDoelVerdieping() == stop);

        boolean wachtend = !gastWachtrij.getOrDefault(stop, Collections.emptyList()).isEmpty()
                || !schoonmakerWachtrij.getOrDefault(stop, Collections.emptyList()).isEmpty();

        boolean opDeWeg = omhoog
                ? (stop <= y && stop >= doelVerdieping)
                : (stop >= y && stop <= doelVerdieping);

        return (uitstapper || wachtend) && opDeWeg;
    }

    // ────────────────────────────────────────────────────────
    // Tekenen
    // ────────────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────
    // Getters
    // ────────────────────────────────────────────────────────
    public int     getCurrentLiftY()  { return y; }
    public int[]   getVerdiepingenY() { return verdiepingenY; }
    public boolean getBeschikbaar()   { return beschikbaar; }
    public int     getStopPositie()   { return stopPositie; }

    // Geeft de interne verzoekenwachtrij terug (voor Hotel als die
    // nog ergens getLiftOproepen() gebruikt — anders mag dit weg)
    public LinkedList<Integer> getVerzoeken() { return verzoeken; }
}