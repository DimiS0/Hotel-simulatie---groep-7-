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

    // Huidige positie van de persoon in pixels
    protected int pixelX;
    protected int pixelY;

    // Verwijzingen naar de kernonderdelen van de simulatie
    protected Hotel hotel;
    protected Schacht schacht;
    protected Lift lift;

    // De reeks waypoints die de persoon moet doorlopen
    protected LinkedList<Point> pad = new LinkedList<>();

    // Bewegingssnelheid in pixels per stap
    protected static final int SNELHEID = 2;
    protected static final Random random = new Random();

    // Voorkomt dat een liftverzoek meerdere keren wordt ingediend
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
            // Eerst horizontaal bewegen totdat X overeenkomt
            if (pixelX < doelPunt.x) pixelX = Math.min(pixelX + SNELHEID, doelPunt.x);
            else                      pixelX = Math.max(pixelX - SNELHEID, doelPunt.x);
        } else if (pixelY != doelPunt.y) {
            // Dan verticaal bewegen totdat Y overeenkomt
            if (pixelY < doelPunt.y) pixelY = Math.min(pixelY + SNELHEID, doelPunt.y);
            else                      pixelY = Math.max(pixelY - SNELHEID, doelPunt.y);
        } else {
            // Waypoint bereikt: verwijder het en ga naar het volgende
            pad.poll();
        }

        // Bereken de huidige rasterpositie op basis van pixelcoördinaten
        int gridX = pixelX / 50;
        int gridY = pixelY / 50;

        // Controleer of de persoon bij de liftschacht staat op een geldige verdieping
        if (gridX == schacht.getX()) {
            for (int verdieping : lift.getVerdiepingenY()) {
                if (gridY == verdieping) {
                    // Dien een liftverzoek in voor deze verdieping
                    liftVerzoek(gridY);
                }
            }
        } else {
            // Persoon staat niet meer bij de schacht: reset de verzoekstatus
            heeftVerzoekIngediend = false;
        }
    }

    // Geeft aan of alle waypoints bereikt zijn
    public boolean isOpDoel() {
        return pad.isEmpty();
    }

    // Stelt een nieuw pad in (lijst van pixel-waypoints)
    public void setPad(List<Point> nieuwPad) {
        this.pad = new LinkedList<>(nieuwPad);
    }

    /**
     * Dient éénmalig een liftverzoek in voor de opgegeven verdieping.
     * Wordt genegeerd als de lift al op die verdieping staat of als
     * er al eerder een verzoek is ingediend.
     */
    public void liftVerzoek(int stopVerdieping) {
        if (!heeftVerzoekIngediend) {
            heeftVerzoekIngediend = true;

            // Voeg verzoek toe als de lift niet al op deze verdieping staat
            if (lift.getY() != stopVerdieping) {
                hotel.getLiftOproepen().addLast(stopVerdieping);

                // Stuur de lift meteen als die beschikbaar is
                if (lift.getBeschikbaar()) {
                    lift.roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
        }
    }

    // Geeft de huidige ruimte terug; standaard null (persoon zit nergens in)
    public HotelRuimte getHuidigeRuimte() {
        return null;
    }

    // Tekent de persoon op het scherm
    public abstract void print(Graphics g);

    public int getPixelX() { return pixelX; }
    public int getPixelY() { return pixelY; }
}