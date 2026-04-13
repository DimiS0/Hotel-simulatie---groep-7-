package hotelsimulator.personen;

import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Persoon {

    protected int pixelX;
    protected int pixelY;
    protected Hotel hotel;
    protected Schacht schacht;
    protected Lift lift;

    protected LinkedList<Point> pad = new LinkedList<>();

    protected static final int SNELHEID = 2;
    protected static final Random random = new Random();

    private boolean heeftVerzoekIngediend = false;

    public Persoon(int startX, int startY, Lift lift, Schacht schacht, Hotel hotel) {
        this.hotel   = hotel;
        this.schacht = schacht;
        this.lift    = lift;
        this.pixelX  = startX;
        this.pixelY  = startY;
    }

    /**
     * Beweegt één stap richting het volgende waypoint.
     * Beweegt ALLEEN horizontaal OF verticaal — nooit diagonaal.
     */
    public void beweeg() {
        if (pad.isEmpty()) return;

        Point doelPunt = pad.peek();

        if (pixelX != doelPunt.x) {
            // Eerst horizontaal bewegen
            if (pixelX < doelPunt.x) pixelX = Math.min(pixelX + SNELHEID, doelPunt.x);
            else                      pixelX = Math.max(pixelX - SNELHEID, doelPunt.x);
        } else if (pixelY != doelPunt.y) {
            // Dan verticaal bewegen
            if (pixelY < doelPunt.y) pixelY = Math.min(pixelY + SNELHEID, doelPunt.y);
            else                      pixelY = Math.max(pixelY - SNELHEID, doelPunt.y);
        } else {
            // Waypoint bereikt, verwijder het en ga naar de volgende
            pad.poll();
        }

        // Lift verzoek-logica
        int gridX = pixelX / 50;
        int gridY = pixelY / 50;

        if (gridX == schacht.getX()) {
            for (int verdieping : lift.getVerdiepingenY()) {
                if (gridY == verdieping) {
                    liftVerzoek(gridY);
                }
            }
        } else {
            heeftVerzoekIngediend = false;
        }
    }

    /** Geeft aan of alle waypoints bereikt zijn */
    public boolean isOpDoel() {
        return pad.isEmpty();
    }

    /** Stelt een nieuw pad in (lijst van pixel-waypoints) */
    public void setPad(List<Point> nieuwPad) {
        this.pad = new LinkedList<>(nieuwPad);
    }

    public void liftVerzoek(int stopVerdieping) {
        if (!heeftVerzoekIngediend) {
            heeftVerzoekIngediend = true;
            if (lift.getY() != stopVerdieping) {
                hotel.getLiftOproepen().addLast(stopVerdieping);
                if (lift.getBeschikbaar()) {
                    lift.roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
        }
    }
    public HotelRuimte getHuidigeRuimte() {
        return null; // Standaard: persoon zit nergens in
    }
    public abstract void print(Graphics g);

    public int getPixelX() { return pixelX; }
    public int getPixelY() { return pixelY; }
}