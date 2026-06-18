package hotelsimulator.personen;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Persoon {

    // Huidige pixelpositie (int voor tekenen, double voor vloeiende beweging)
    protected int pixelX;
    protected int pixelY;
    protected double pixelXD;
    protected double pixelYD;
    protected double pixelYDouble;

    // Verwijzingen naar de simulatie-objecten die elke persoon nodig heeft
    protected Hotel hotel;
    protected SimulatieConfig simulatieConfig;
    protected Schacht schacht;
    protected Lift lift;
    protected HotelEventManager hotelEventManager;

    // Bewegingsconstanten en het huidige looppad
    protected LinkedList<Point> pad = new LinkedList<>();
    protected static final int SNELHEID = 2;

    // Vaste X-posities berekend uit de lay-out (schacht, trap, lift-midden)
    protected int    SCHACHT_PIXEL_X;
    protected int    TRAP_PIXEL_X;
    protected double Double_TRAP_PIXEL_X;
    protected int    LIFT_CENTER_X;

    protected static final Random random = new Random();

    // Bijhouden of de persoon al een liftverzoek heeft ingediend voor de huidige stop
    private boolean heeftVerzoekIngediend = false;

    // Wordt true wanneer de persoon het hotel moet verlaten
    private boolean moetVerwijderdWorden = false;

    // Sla alle referenties op en bereken de vaste X-posities uit de lay-out
    public Persoon(int startX, int startY, Lift lift, Schacht schacht,
                   Hotel hotel, HotelEventManager hotelEventManager,
                   SimulatieConfig simulatieConfig) {
        this.hotel             = hotel;
        this.simulatieConfig   = simulatieConfig;
        this.schacht           = schacht;
        this.lift              = lift;
        this.hotelEventManager = hotelEventManager;

        // Schacht begint 1 cel rechts van kolom 0, lift-centrum zit in het midden van de schachtkolom
        this.SCHACHT_PIXEL_X       = (schacht.getX() + 2) * 50;
        this.LIFT_CENTER_X         = (schacht.getX() + 1) * 50 + 25;

        // Trap staat aan het einde van het hotel; halve cel extra voor het midden
        this.Double_TRAP_PIXEL_X   = (hotel.getTrap().getX() + 1.5) * 50;
        this.TRAP_PIXEL_X          = (int) Double_TRAP_PIXEL_X;

        setPositie(startX, startY);
    }

    // ── Abstracte methodes ────────────────────────────────────────────────────
    // Elke subklasse (Gast, Schoonmaker) implementeert zijn eigen gedrag.

    // Elke frame aangeroepen vanuit SimulatieLus om toestand en positie bij te werken
    public abstract void update();

    // Zet de persoon op zijn startpositie en kiest een doelkamer
    public abstract void activeer();

    // Geeft terug of de persoon al zichtbaar in het hotel is (gespawnd)
    public abstract boolean isGespawnd();

    // Aangeroepen door de Lift wanneer hij aankomt op de verdieping van deze persoon
    public abstract void stapIn();

    // Aangeroepen door de Lift wanneer de doelverdieping bereikt is
    public abstract void stapUit(int pixelYPos);

    // Geeft de doelverdieping terug zodat de lift weet waar hij heen moet
    public abstract int getDoelVerdieping();

    // ── Verwijdering ─────────────────────────────────────────────────────────

    public void markeerVoorVerwijdering() {
        moetVerwijderdWorden = true;
    }

    public boolean moetVerwijderdWorden() {
        return moetVerwijderdWorden;
    }

    // ── Gedeelde hulpmethodes (ook gebruikt door subklassen) ──────────────────

    // Berekent het middelpunt van een kamer in pixels
    protected Point getTrueMidden(HotelRuimte kamer) {
        int middenX = (kamer.getX() + 1) * 50 + kamer.getBreedte() * 50 / 2;
        int middenY = (kamer.getY() - 1) * 50 + kamer.getHoogte()  * 50 / 2;
        return new Point(middenX, middenY);
    }

    // Geeft de dichtstbijzijnde liftstop terug voor een gegeven grid-Y positie
    protected int getNabijeStop(double gridY) {
        int[] stops = hotel.getLift().getVerdiepingenY();
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij))
                dichtstbij = stop;
        }
        return dichtstbij;
    }

    // Geeft de dichtstbijzijnde trapstop terug voor een gegeven grid-Y positie
    protected int getNabijeStopTrap(int gridY) {
        int[] stops = hotel.getTrap().getIngangen();
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij))
                dichtstbij = stop;
        }
        return dichtstbij;
    }

    // Kiest willekeurig tussen schacht (lift) of trap om naar een andere verdieping te gaan
    protected void loopNaarSchachtOfTrapGemeen(int huidigeVerdieping,
                                               Runnable zetStatusSchacht,
                                               Runnable zetStatusTrap) {
        int kies = random.nextInt(1, 3);

        if (kies == 1) {
            // Route via de liftschacht: wacht op de juiste rij voor de huidige verdieping
            int wachtY = (huidigeVerdieping - 1) * 50;
            List<Point> schachtPad = Pathfinder.vindPad(
                    pixelX, pixelY, SCHACHT_PIXEL_X, wachtY, hotel.getRuimtes(), hotel);
            if (!schachtPad.isEmpty()) {
                setPad(schachtPad);
                zetStatusSchacht.run();
            }
        } else {
            // Route via de trap: loop naar de dichtstbijzijnde trapstop
            int wachtY = getNabijeStopTrap(huidigeVerdieping - 1) * 50;
            List<Point> trapPad = Pathfinder.vindPad(
                    pixelX, pixelY, TRAP_PIXEL_X, wachtY, hotel.getRuimtes(), hotel);
            if (!trapPad.isEmpty()) {
                setPad(trapPad);
                zetStatusTrap.run();
            }
        }
    }

    // Beweegt de persoon mee met de lift terwijl die rijdt
    protected void updateInLift() {
        // Eerst horizontaal naar het liftmidden schuiven, daarna meebewegen met de lift
        if (pixelX != LIFT_CENTER_X) {
            if (pixelX > LIFT_CENTER_X) pixelX = Math.max(pixelX - SNELHEID, LIFT_CENTER_X);
            else                         pixelX = Math.min(pixelX + SNELHEID, LIFT_CENTER_X);
        } else {
            // Volg de werkelijke pixel-positie van de lift mee
            pixelY = (int)hotel.getLift().getCurrentLiftPixelY() + 25;
        }
    }

    // Beweegt de persoon langs de trap; geeft true terug wanneer de doelverdieping bereikt is
    protected boolean updateInTrap(int doelVerdieping) {
        int doelY = doelVerdieping * 50;

        // Aangekomen als de afwijking klein genoeg is
        if (Math.abs(pixelY - doelY) <= 1) {
            setPositie(TRAP_PIXEL_X, doelY);
            pixelX  = (TRAP_PIXEL_X / 50) * 50;
            pixelXD = pixelX;
            return true;
        }

        // Beweeg stap voor stap omhoog of omlaag
        if (doelY > pixelYDouble) {
            pixelYDouble += (2.0 * getFactor()) / 3;
        } else {
            pixelYDouble -= (2.0 * getFactor()) / 3;
        }
        pixelY = (int) pixelYDouble;
        return false;
    }

    // Beweegt de persoon één stap langs het berekende pad (eerst X, dan Y — geen diagonaal)
    public void beweeg() {
        if (pad.isEmpty()) return;

        double stap = SNELHEID * simulatieConfig.getSnelheid().getFactor();
        Point doel  = pad.peek();

        if (pixelXD != doel.x) {
            if (pixelXD < doel.x) pixelXD = Math.min(pixelXD + stap, doel.x);
            else                   pixelXD = Math.max(pixelXD - stap, doel.x);
        } else if (pixelYD != doel.y) {
            if (pixelYD < doel.y) pixelYD = Math.min(pixelYD + stap, doel.y);
            else                   pixelYD = Math.max(pixelYD - stap, doel.y);
        } else {
            pad.poll(); // punt bereikt, verwijder het uit het pad
        }

        pixelX = (int) pixelXD;
        pixelY = (int) pixelYD;

        // Als de persoon op een verdiepingsstop van de schacht staat, liftverzoek indienen
        int gridX = pixelX / 50;
        int gridY = pixelY / 50;
        if (gridX == schacht.getX()) {
            for (int verdieping : lift.getVerdiepingenY()) {
                if (gridY == verdieping) liftVerzoek(gridY);
            }
        } else {
            heeftVerzoekIngediend = false;
        }
    }

    // Voegt een verzoek toe aan de liftoproepenwachtrij (één keer per stop)
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

    // ── Setters / getters ─────────────────────────────────────────────────────

    protected void setPositie(int x, int y) {
        this.pixelX  = x;
        this.pixelY  = y;
        this.pixelXD = x;
        this.pixelYD = y;
    }

    public void setPad(List<Point> nieuwPad) {
        this.pad = new LinkedList<>(nieuwPad);
    }

    // Snelheidsfactor ophalen uit de config (gebruikt door subklassen)
    public double getFactor() {
        return simulatieConfig.getSnelheid().getFactor();
    }

    // Geeft de kamer terug waar de persoon zich nu in bevindt (standaard null)
    public HotelRuimte getHuidigeRuimte() {
        return null;
    }

    public abstract void print(Graphics g);

    public int getPixelY() { return pixelY; }
}