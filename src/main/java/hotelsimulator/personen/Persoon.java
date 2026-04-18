package hotelsimulator.personen;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Persoon {

    // Huidige positie van de persoon in pixels (int, voor tekenen)
    protected int pixelX;
    protected int pixelY;

    // Dezelfde positie als doubles — nodig voor vloeiende beweging bij lagere snelheden
    // BELANGRIJK: deze moeten altijd synchroon lopen met pixelX en pixelY
    protected double pixelXD;
    protected double pixelYD;

    // Verwijzingen naar de kernonderdelen van de simulatie
    protected Hotel hotel;
    SimulatieConfig simulatieConfig;
    protected Schacht schacht;
    protected Lift lift;
    protected HotelEventManager hotelEventManager;

    // De reeks waypoints die de persoon moet doorlopen
    protected LinkedList<Point> pad = new LinkedList<>();

    // Bewegingssnelheid in pixels per stap (basis)
    protected static final int SNELHEID = 2;
    protected static final Random random = new Random();
    protected double pixelYDouble;
    protected double factor;

    // Voorkomt dat een liftverzoek meerdere keren wordt ingediend
    private boolean heeftVerzoekIngediend = false;

    public Persoon(int startX, int startY, Lift lift, Schacht schacht, Hotel hotel, HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig) {
        this.hotel            = hotel;
        this.simulatieConfig  = simulatieConfig;
        this.schacht          = schacht;
        this.lift             = lift;
        this.hotelEventManager = hotelEventManager;
        // Initialiseer alle vier posities tegelijk zodat ze vanaf het begin synchroon zijn
        this.pixelX  = startX;
        this.pixelY  = startY;
        this.pixelXD = startX;
        this.pixelYD = startY;
    }

    /**
     * Zet de positie van de persoon en houdt int én double velden synchroon.
     * Gebruik deze methode altijd als je de persoon naar een nieuwe positie teleporteert
     * (bijv. na uitstappen uit de lift), anders loopt pixelXD/YD uit de pas en
     * beweegt de persoon via de verkeerde beginpositie.
     */
    protected void setPositie(int x, int y) {
        this.pixelX  = x;
        this.pixelY  = y;
        this.pixelXD = x;
        this.pixelYD = y;
    }

    /**
     * Beweegt één stap richting het volgende waypoint.
     * Gebruikt doubles voor vloeiende beweging bij lagere HTE-snelheden.
     * Beweegt ALLEEN horizontaal OF verticaal — nooit diagonaal.
     */
    public void beweeg() {
        if (pad.isEmpty()) return;

        // Stapgrootte schalen op basis van de HTE-snelheid (bijv. 0.25x → 0.5 pixels/frame)
        double stap = SNELHEID * simulatieConfig.getSnelheid().getFactor();

        Point doelPunt = pad.peek();

        if (pixelXD != doelPunt.x) {
            // Horizontaal bewegen
            if (pixelXD < doelPunt.x) pixelXD = Math.min(pixelXD + stap, doelPunt.x);
            else                       pixelXD = Math.max(pixelXD - stap, doelPunt.x);
        } else if (pixelYD != doelPunt.y) {
            // Verticaal bewegen
            if (pixelYD < doelPunt.y) pixelYD = Math.min(pixelYD + stap, doelPunt.y);
            else                       pixelYD = Math.max(pixelYD - stap, doelPunt.y);
        } else {
            // Waypoint bereikt: verwijder het en ga naar het volgende
            pad.poll();
        }

        // Zet de int-velden bij vanuit de doubles (voor tekenen en positiecontroles)
        pixelX = (int) pixelXD;
        pixelY = (int) pixelYD;

        // Controleer of de persoon bij de liftschacht staat op een geldige verdieping
        int gridX = pixelX / 50;
        int gridY = pixelY / 50;

        if (gridX == schacht.getX()) {
            for (int verdieping : lift.getVerdiepingenY()) {
                if (gridY == verdieping) {
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

            if (lift.getY() != stopVerdieping) {
                hotel.getLiftOproepen().addLast(stopVerdieping);

                if (lift.getBeschikbaar()) {
                    lift.roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
        }
    }

    public double getFactor() {
        factor = simulatieConfig.getSnelheid().getFactor();
        return factor;
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
