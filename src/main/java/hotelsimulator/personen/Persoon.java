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

    // Positie
    protected int pixelX;
    protected int pixelY;
    protected double pixelXD;
    protected double pixelYD;
    protected double pixelYDouble;

    // Simulatie-referenties
    protected Hotel hotel;
    protected SimulatieConfig simulatieConfig;
    protected Schacht schacht;
    protected Lift lift;
    protected HotelEventManager hotelEventManager;

    //  Beweging
    protected LinkedList<Point> pad = new LinkedList<>();
    private double doubleTRAP_PIXEL_X;
    protected static final int    SNELHEID         = 2;
    protected static final int    SCHACHT_PIXEL_X  = 100;
    protected int TRAP_PIXEL_X;
    protected static final int    LIFT_CENTER_X    = 75;
    protected static final Random random           = new Random();
    private boolean heeftVerzoekIngediend = false;
    private boolean moetVerwijderdWorden = false;

    // Constructor
    public Persoon(int startX, int startY, Lift lift, Schacht schacht,
                   Hotel hotel, HotelEventManager hotelEventManager,
                   SimulatieConfig simulatieConfig) {
        this.hotel             = hotel;
        this.simulatieConfig   = simulatieConfig;
        this.schacht           = schacht;
        this.lift              = lift;
        this.hotelEventManager = hotelEventManager;
        this.doubleTRAP_PIXEL_X = (hotel.getMaxBreedte() + 2.5) * 50;
        this.TRAP_PIXEL_X = (int) doubleTRAP_PIXEL_X;
        setPositie(startX, startY);
    }


    // Abstracte methodes — elke subklasse implementeert deze

    // Wordt elke frame aangeroepen vanuit SimulatieLus.
    public abstract void update();

    // Zet de persoon klaar op zijn startpositie en kiest een doelkamer.
    public abstract void activeer();

    // Geeft terug of de persoon al gespawnd is.
    public abstract boolean isGespawnd();

    // Aangeroepen door de Lift wanneer hij aankomt.
    public abstract void stapIn();

    // Aangeroepen door de Lift wanneer de doelverdieping bereikt is.
    public abstract void stapUit(int pixelYPos);

    // Geeft de doelverdieping terug (nodig voor de lift).
    public abstract int getDoelVerdieping();

    public void markeerVoorVerwijdering() {
        moetVerwijderdWorden = true;
    }

    public boolean moetVerwijderdWorden() {
        return moetVerwijderdWorden;
    }

    // Gedeelde methodes (waren duplicaat in Gast + Schoonmaker)



    protected Point getTrueMidden(HotelRuimte kamer) {
        int middenX = (kamer.getX() + 1) * 50 + kamer.getBreedte() * 50 / 2;
        int middenY = (kamer.getY() - 1) * 50 + kamer.getHoogte()  * 50 / 2;
        return new Point(middenX, middenY);
    }


    protected int getNabijeStop(int gridY) {
        int[] stops = hotel.getLift().getVerdiepingenY();
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij))
                dichtstbij = stop;
        }
        return dichtstbij;
    }


    protected void loopNaarSchachtOfTrapGemeen(int huidigeVerdieping,
                                               Runnable zetStatusSchacht,
                                               Runnable zetStatusTrap) {
        int kies = random.nextInt(1, 3);
        if (kies == 1) {
            int wachtY = (huidigeVerdieping - 1) * 50;
            List<Point> schachtPad = Pathfinder.vindPad(
                    pixelX, pixelY, SCHACHT_PIXEL_X, wachtY, hotel.getRuimtes());
            if (!schachtPad.isEmpty()) {
                setPad(schachtPad);
                zetStatusSchacht.run();
            }
        } else {
            int wachtY = getNabijeStop(huidigeVerdieping) * 50;
            List<Point> trapPad = Pathfinder.vindPad(
                    pixelX, pixelY, TRAP_PIXEL_X, wachtY, hotel.getRuimtes());
            if (!trapPad.isEmpty()) {
                setPad(trapPad);
                zetStatusTrap.run();
            }
        }
    }


    protected void updateInLift() {
        if (pixelX != LIFT_CENTER_X) {
            if (pixelX > LIFT_CENTER_X) pixelX = Math.max(pixelX - SNELHEID, LIFT_CENTER_X);
            else                         pixelX = Math.min(pixelX + SNELHEID, LIFT_CENTER_X);
        } else {
            pixelY = (hotel.getLift().getCurrentLiftY() - 1) * 50 + 25;
        }
    }


    protected boolean updateInTrap(int doelVerdieping) {
        int doelY = doelVerdieping * 50;
        if (Math.abs(pixelY - doelY) <= SNELHEID) {
            setPositie(TRAP_PIXEL_X, doelY);
            pixelX  = (TRAP_PIXEL_X / 50) * 50;
            pixelXD = pixelX;
            return true; // aangekomen
        }
        if (doelY > pixelYDouble) {
            pixelYDouble += (2.0 * getFactor()) / 3;
        } else {
            pixelYDouble -= (2.0 * getFactor()) / 3;
        }
        pixelY = (int) pixelYDouble;
        return false; // nog onderweg
    }

    public void beweeg() {
        if (pad.isEmpty()) return;

        //point doel eerstvolgende punt waar je gaat zonder het te verwijderen
        double stap = SNELHEID * simulatieConfig.getSnelheid().getFactor();
        Point doel  = pad.peek();

        //bewegen eerst met x en dan Y geen diagonaal, poll verwijdert de pad
        if (pixelXD != doel.x) {
            if (pixelXD < doel.x) pixelXD = Math.min(pixelXD + stap, doel.x);
            else                   pixelXD = Math.max(pixelXD - stap, doel.x);
        } else if (pixelYD != doel.y) {
            if (pixelYD < doel.y) pixelYD = Math.min(pixelYD + stap, doel.y);
            else                   pixelYD = Math.max(pixelYD - stap, doel.y);
        } else {
            pad.poll();
        }

        pixelX = (int) pixelXD;
        pixelY = (int) pixelYD;

        //pixels omrekenen naar grid
        int gridX = pixelX / 50;
        int gridY = pixelY / 50;

        //persoon op dezelfde X en Y als schacht lift verzoek indienen
        if (gridX == schacht.getX()) {
            for (int verdieping : lift.getVerdiepingenY()) {
                if (gridY == verdieping) liftVerzoek(gridY);
            }
        } else {
            heeftVerzoekIngediend = false;
        }
    }

    //zet de verdieping in een linkedlist dit heet liftoproepen, als lift beschikbaar is gaat de lift daar naartoe
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

    protected void setPositie(int x, int y) {
        this.pixelX  = x;
        this.pixelY  = y;
        this.pixelXD = x;
        this.pixelYD = y;
    }

    public void setPad(List<Point> nieuwPad) {
        this.pad = new LinkedList<>(nieuwPad);
    }

    public boolean isOpDoel() {
        return pad.isEmpty();
    }

    public double getFactor() {
        return simulatieConfig.getSnelheid().getFactor();
    }

    public HotelRuimte getHuidigeRuimte() {
        return null;
    }

    public abstract void print(Graphics g);

    public int getPixelX() { return pixelX; }
    public int getPixelY() { return pixelY; }
}