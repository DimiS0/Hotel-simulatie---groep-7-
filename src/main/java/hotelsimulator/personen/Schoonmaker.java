package hotelsimulator.personen;

import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Schoonmaker extends Persoon {

    public enum Status {
        WACHT_OP_SPAWN,
        LOOPT_NAAR_INGANG,
        LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT,
        IN_LIFT,
        BETREEDT_KAMER,
        IN_KAMER,
        VERLAAT_KAMER
    }

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;
    private static final long VERBLIJF_MS = 7000;

    public static final int SPAWN_X = 200;
    public static final int SPAWN_Y = 400;
    private static final int LIFT_CENTER_X   = 75;
    private static final int SCHACHT_PIXEL_X = 100;

    private int doelVerdieping   = 8;
    private int huidigeVerdieping = 8;

    public Schoonmaker(int startX, int startY, hotelsimulator.ruimtes.Lift lift, hotelsimulator.ruimtes.Schacht schacht, Hotel hotel) {
        super(startX, startY, lift, schacht, hotel);
    }

    public void update() {
        switch (status) {

            case WACHT_OP_SPAWN:
                break;

            case LOOPT_NAAR_INGANG:
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
                    if (midden.y >= ingang.y) {
                        doelKamer.verlaatAlsSchoonmaker();
                        doelKamer = null;
                        kiesEnLoopNaarKamer();
                        return;
                    }
                    List<Point> instapPad = new ArrayList<>();
                    instapPad.add(new Point(midden.x, ingang.y));
                    instapPad.add(midden);
                    setPad(instapPad);
                    status = Status.BETREEDT_KAMER;
                }
                break;

            case LOOPT_NAAR_SCHACHT:
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    hotel.getLift().voegWachtendeGastToe(null, huidigeVerdieping);
                    hotel.getLift().voegWachtendeSchoonmakerToe(this, huidigeVerdieping);
                    status = Status.WACHT_OP_LIFT;
                }
                break;

            case WACHT_OP_LIFT:
                break;

            case IN_LIFT:
                if (pixelX != LIFT_CENTER_X) {
                    if (pixelX > LIFT_CENTER_X) pixelX = Math.max(pixelX - SNELHEID, LIFT_CENTER_X);
                    else                         pixelX = Math.min(pixelX + SNELHEID, LIFT_CENTER_X);
                } else {
                    pixelY = (hotel.getLift().getCurrentLiftY() - 1) * 50 + 25;
                }
                break;

            case BETREEDT_KAMER:
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis() + VERBLIJF_MS;
                }
                break;

            case IN_KAMER:
                if (System.currentTimeMillis() >= verblijfEinde) {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
                    List<Point> naarIngang = new ArrayList<>();
                    naarIngang.add(new Point(midden.x, ingang.y));
                    naarIngang.add(ingang);
                    setPad(naarIngang);
                    status = Status.VERLAAT_KAMER;
                }
                break;

            case VERLAAT_KAMER:
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    doelKamer.verlaatAlsSchoonmaker();
                    doelKamer = null;
                    kiesEnLoopNaarKamer();
                }
                break;
        }
    }

    private void kiesEnLoopNaarKamer() {
        List<HotelRuimte> kamers = hotel.getKamers();
        if (kamers.isEmpty()) return;

        for (int poging = 0; poging < 15; poging++) {
            HotelRuimte kandidaat = kamers.get(random.nextInt(kamers.size()));
            if (kandidaat.isVol()) continue;

            Point ingang = Pathfinder.getKamerIngang(kandidaat);
            Point midden = getTrueMidden(kandidaat);
            if (midden.y >= ingang.y) continue;

            int kamerVerdieping = getNabijeStop(kandidaat.getY());

            if (kamerVerdieping == huidigeVerdieping) {
                List<Point> nieuwPad = Pathfinder.vindPad(
                        pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
                if (!nieuwPad.isEmpty()) {
                    kandidaat.betreedAlsSchoonmaker();
                    doelKamer = kandidaat;
                    setPad(nieuwPad);
                    status = Status.LOOPT_NAAR_INGANG;
                    return;
                }
            } else {
                kandidaat.betreedAlsSchoonmaker();
                doelKamer = kandidaat;
                doelVerdieping = kamerVerdieping;
                loopNaarSchacht();
                if (status == Status.LOOPT_NAAR_SCHACHT) return;
                doelKamer.verlaatAlsSchoonmaker();
                doelKamer = null;
            }
        }
    }

    private void loopNaarSchacht() {
        int wachtPixelY = (huidigeVerdieping - 1) * 50;
        List<Point> schachtPad = Pathfinder.vindPad(
                pixelX, pixelY, SCHACHT_PIXEL_X, wachtPixelY, hotel.getRuimtes());
        if (!schachtPad.isEmpty()) {
            setPad(schachtPad);
            status = Status.LOOPT_NAAR_SCHACHT;
        }
    }

    private int getNabijeStop(int gridY) {
        int[] stops = hotel.getLift().getVerdiepingenY();
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij))
                dichtstbij = stop;
        }
        return dichtstbij;
    }

    private Point getTrueMidden(HotelRuimte kamer) {
        int middenX = (kamer.getX() + 1) * 50 + kamer.getBreedte() * 50 / 2;
        int middenY = (kamer.getY() - 1) * 50 + kamer.getHoogte()  * 50 / 2;
        return new Point(middenX, middenY);
    }

    public void stapIn() {
        status = Status.IN_LIFT;
    }

    public void stapUit(int pixelYPos) {
        pixelX = SCHACHT_PIXEL_X;
        pixelY = pixelYPos;
        huidigeVerdieping = doelVerdieping;

        if (doelKamer != null) {
            Point ingang = Pathfinder.getKamerIngang(doelKamer);
            List<Point> padNaarKamer = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
            if (!padNaarKamer.isEmpty()) {
                setPad(padNaarKamer);
                status = Status.LOOPT_NAAR_INGANG;
                return;
            }
            doelKamer.verlaatAlsSchoonmaker();
            doelKamer = null;
        }
        kiesEnLoopNaarKamer();
    }

    public int getDoelVerdieping() { return doelVerdieping; }

    public void activeer() {
        this.pixelX = SPAWN_X;
        this.pixelY = SPAWN_Y;
        this.pad.clear();
        this.doelKamer = null;
        this.huidigeVerdieping = 8;
        this.status = Status.WACHT_OP_SPAWN;
        kiesEnLoopNaarKamer();
    }

    public boolean isGespawnd() {
        return status != Status.WACHT_OP_SPAWN;
    }

    @Override
    public HotelRuimte getHuidigeRuimte() {
        if (status == Status.IN_KAMER ||
                status == Status.BETREEDT_KAMER ||
                status == Status.VERLAAT_KAMER) return doelKamer;
        return null;
    }

    @Override
    public void print(Graphics g) {
        if (status == Status.WACHT_OP_SPAWN) return;

        Color kleur = switch (status) {
            case IN_KAMER           -> new Color(148, 0, 211);
            case WACHT_OP_LIFT      -> new Color(200, 100, 255);
            case IN_LIFT            -> new Color(100, 0, 150);
            case BETREEDT_KAMER,
                 VERLAAT_KAMER      -> new Color(180, 50, 220);
            default                 -> new Color(160, 32, 240);
        };

        g.setColor(kleur);
        g.fillOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("S", pixelX - 4, pixelY + 5);
    }
}
