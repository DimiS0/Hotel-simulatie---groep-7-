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

    // Huidige positie van de persoon in pixels
    protected int pixelX;
    protected int pixelY;
    private double pixelXD;
    private double pixelYD;

    // Verwijzingen naar de kernonderdelen van de simulatie
    protected Hotel hotel;
    SimulatieConfig simulatieConfig;
    protected Schacht schacht;
    protected Lift lift;
    protected  HotelEventManager hotelEventManager;

    // De reeks waypoints die de persoon moet doorlopen
    protected LinkedList<Point> pad = new LinkedList<>();

    // Bewegingssnelheid in pixels per stap
    protected static final int SNELHEID = 2;
    protected static final Random random = new Random();

    // Voorkomt dat een liftverzoek meerdere keren wordt ingediend
    private boolean heeftVerzoekIngediend = false;

    public Persoon(int startX, int startY, Lift lift, Schacht schacht, Hotel hotel, HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig) {
        this.hotel   = hotel;
        this.simulatieConfig = simulatieConfig;
        this.schacht = schacht;
        this.lift    = lift;
        this.hotelEventManager = hotelEventManager;
        this.pixelX  = startX;
        this.pixelY  = startY;
        this.pixelXD = pixelX;
        this.pixelYD = pixelY;
    }

    /**
     * Beweegt één stap richting het volgende waypoint.
     * Beweegt ALLEEN horizontaal OF verticaal — nooit diagonaal.
     */
    public void beweeg() {
        //pixelXD en pixelYD zijn gewoon pixelX en PixelY alleen dan in een double want ik wist niet of ik die waardes naar int mocht veranderen

        double stapFactor = simulatieConfig.getSnelheid().getFactor();

        //de stap berekenen met 2 * de factor in HTE snelheid enum. 2 * 0,25 = 0,5 stap per frame
        double stap = SNELHEID * stapFactor;

        if (pad.isEmpty()) return;

        Point doelPunt = pad.peek();

        if (pixelXD != doelPunt.x) {
            if (pixelXD < doelPunt.x) pixelXD = Math.min(pixelXD + stap, doelPunt.x);
            else                       pixelXD = Math.max(pixelXD - stap, doelPunt.x);
        } else if (pixelYD != doelPunt.y) {
            if (pixelYD < doelPunt.y) pixelYD = Math.min(pixelYD + stap, doelPunt.y);
            else                       pixelYD = Math.max(pixelYD - stap, doelPunt.y);
        } else {
            pad.poll();
        }

        //de orginelewaardes updaten anders geven ze steeds dezelfde waardes aan pixelXD en pixelYD
        pixelX = (int) pixelXD;
        pixelY = (int) pixelYD;

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