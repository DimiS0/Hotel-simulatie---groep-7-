package hotelsimulator.personen;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Gast extends Persoon {

    public enum Status {
        WACHT_OP_SPAWN, LOOPT_NAAR_INGANG, LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT, IN_LIFT, LOOP_DOOR_TRAP, LOOP_NAAR_TRAP,
        BETREEDT_KAMER, IN_KAMER, VERLAAT_KAMER
    }

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;

    public static final int SPAWN_X = 200;
    public static final int SPAWN_Y = 500;

    private int doelVerdieping    = 8;
    private int huidigeVerdieping = 8;

    public Gast(Lift lift, Schacht schacht, Hotel hotel,
                HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig) {
        super(SPAWN_X, SPAWN_Y, lift, schacht, hotel, hotelEventManager, simulatieConfig);
    }

    @Override
    public void update() {
        switch (status) {

            case WACHT_OP_SPAWN -> {}

            case LOOPT_NAAR_INGANG -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);       // ← uit Persoon
                    if (midden.y >= ingang.y) {
                        doelKamer.verlaat();
                        doelKamer = null;
                        kiesEnLoopNaarKamer();
                        return;
                    }
                    List<Point> instap = new ArrayList<>();
                    instap.add(new Point(midden.x, ingang.y));
                    instap.add(midden);
                    setPad(instap);
                    status = Status.BETREEDT_KAMER;
                }
            }

            case LOOPT_NAAR_SCHACHT -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    hotel.getLift().voegWachtendeGastToe(this, huidigeVerdieping);
                    status = Status.WACHT_OP_LIFT;
                }
            }

            case WACHT_OP_LIFT -> {}

            case IN_LIFT -> updateInLift();    // ← uit Persoon

            case BETREEDT_KAMER -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis()
                            + (long)(doelKamer.getVerblijfMs() / simulatieConfig.getSnelheid().getFactor());
                }
            }

            case IN_KAMER -> {
                if (System.currentTimeMillis() >= verblijfEinde) {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);       // ← uit Persoon
                    List<Point> naarIngang = new ArrayList<>();
                    naarIngang.add(new Point(midden.x, ingang.y));
                    naarIngang.add(ingang);
                    setPad(naarIngang);
                    status = Status.VERLAAT_KAMER;
                }
            }

            case VERLAAT_KAMER -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    doelKamer.verlaat();
                    doelKamer = null;
                    kiesEnLoopNaarKamer();
                }
            }

            case LOOP_NAAR_TRAP -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    pixelX = TRAP_PIXEL_X;
                    pixelYDouble = pixelY;
                    status = Status.LOOP_DOOR_TRAP;
                }
            }

            case LOOP_DOOR_TRAP -> {
                boolean aangekomen = updateInTrap(doelVerdieping);  // ← uit Persoon
                if (aangekomen) {
                    huidigeVerdieping = doelVerdieping;
                    if (doelKamer != null) {
                        Point ingang = Pathfinder.getKamerIngang(doelKamer);
                        List<Point> p = Pathfinder.vindPad(
                                pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
                        if (!p.isEmpty()) { setPad(p); status = Status.LOOPT_NAAR_INGANG; break; }
                        doelKamer.verlaat();
                        doelKamer = null;
                    }
                    kiesEnLoopNaarKamer();
                }
            }
        }
    }

    private void kiesEnLoopNaarKamer() {
        List<HotelRuimte> kamers = hotel.getKamers();
        if (kamers.isEmpty()) return;

        for (int poging = 0; poging < 15; poging++) {
            HotelRuimte kandidaat = kamers.get(random.nextInt(kamers.size()));
            if (kandidaat.isVol()) continue;

            Point ingang = Pathfinder.getKamerIngang(kandidaat);
            Point midden = getTrueMidden(kandidaat);               // ← uit Persoon
            if (midden.y >= ingang.y) continue;

            int kamerVerdieping = getNabijeStop(kandidaat.getY()); // ← uit Persoon

            if (kamerVerdieping == huidigeVerdieping) {
                List<Point> nieuwPad = Pathfinder.vindPad(
                        pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
                if (!nieuwPad.isEmpty()) {
                    kandidaat.betreedAlsGast();
                    doelKamer = kandidaat;
                    setPad(nieuwPad);
                    status = Status.LOOPT_NAAR_INGANG;
                    return;
                }
            } else {
                kandidaat.betreedAlsGast();
                doelKamer = kandidaat;
                doelVerdieping = kamerVerdieping;
                loopNaarSchachtOfTrap();
                if (status == Status.LOOPT_NAAR_SCHACHT || status == Status.LOOP_NAAR_TRAP) return;
                doelKamer.verlaat();
                doelKamer = null;
            }
        }
    }

    // Roept de gedeelde methode in Persoon aan met Gast-specifieke status-setters
    private void loopNaarSchachtOfTrap() {
        loopNaarSchachtOfTrapGemeen(                               // ← uit Persoon
                huidigeVerdieping,
                () -> status = Status.LOOPT_NAAR_SCHACHT,
                () -> status = Status.LOOP_NAAR_TRAP
        );
    }

    @Override
    public void stapIn() { status = Status.IN_LIFT; }

    @Override
    public void stapUit(int pixelYPos) {
        setPositie(SCHACHT_PIXEL_X, pixelYPos);
        huidigeVerdieping = doelVerdieping;
        if (doelKamer != null) {
            Point ingang = Pathfinder.getKamerIngang(doelKamer);
            List<Point> p = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
            if (!p.isEmpty()) { setPad(p); status = Status.LOOPT_NAAR_INGANG; return; }
            doelKamer.verlaat();
            doelKamer = null;
        }
        kiesEnLoopNaarKamer();
    }

    @Override
    public int getDoelVerdieping() { return doelVerdieping; }

    @Override
    public void activeer() {
        pixelX = SPAWN_X; pixelY = SPAWN_Y;
        pad.clear();
        doelKamer = null;
        huidigeVerdieping = 8;
        status = Status.WACHT_OP_SPAWN;
        kiesEnLoopNaarKamer();
    }

    @Override
    public boolean isGespawnd() { return status != Status.WACHT_OP_SPAWN; }

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
            case WACHT_OP_LIFT                          -> new Color(100, 255, 150);
            case BETREEDT_KAMER, VERLAAT_KAMER, IN_KAMER -> new Color(150, 255, 0);
            default                                      -> new Color(50, 255, 50);
        };

        g.setColor(kleur);
        g.fillOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("G", pixelX - 4, pixelY + 5);

        if (doelKamer != null && (status == Status.BETREEDT_KAMER
                || status == Status.IN_KAMER || status == Status.VERLAAT_KAMER)) {
            String emoji = switch (doelKamer.getAreaType()) {
                case "Cinema"     -> "🎬";
                case "Restaurant" -> "🍽";
                case "Fitness"    -> "💪";
                case "Room"       -> "🛏";
                default           -> "";
            };
            if (!emoji.isEmpty()) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                g.setColor(Color.BLACK);
                g.drawString(emoji, pixelX - 8, pixelY - 13);
            }
        }
    }
}