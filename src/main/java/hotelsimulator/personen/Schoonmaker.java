package hotelsimulator.personen;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Schoonmaker extends Persoon {

    public enum Status {
        WACHT_OP_SPAWN, LOOPT_NAAR_INGANG, LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT, IN_LIFT, BETREEDT_KAMER, IN_KAMER,
        VERLAAT_KAMER, LOOP_NAAR_TRAP, LOOP_DOOR_TRAP
    }

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;
    private static final long VERBLIJF_MS = 7000;

    public static final int SPAWN_X = 200;
    public static final int SPAWN_Y = 400;

    private int doelVerdieping    = 8;
    private int huidigeVerdieping = 8;

    public Schoonmaker(int startX, int startY, Lift lift, Schacht schacht,
                       Hotel hotel, HotelEventManager hotelEventManager,
                       SimulatieConfig simulatieConfig) {
        super(startX, startY, lift, schacht, hotel, hotelEventManager, simulatieConfig);
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
                    Point midden = getTrueMidden(doelKamer);
                    if (midden.y >= ingang.y) {
                        doelKamer.verlaatAlsSchoonmaker();
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
                    hotel.getLift().voegWachtendeGastToe(null, huidigeVerdieping);
                    hotel.getLift().voegWachtendeSchoonmakerToe(this, huidigeVerdieping);
                    status = Status.WACHT_OP_LIFT;
                }
            }

            case WACHT_OP_LIFT -> {}

            case IN_LIFT -> updateInLift();

            case BETREEDT_KAMER -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis() + VERBLIJF_MS;
                }
            }

            case IN_KAMER -> {
                if (System.currentTimeMillis() >= verblijfEinde) {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
                    List<Point> naarIngang = new ArrayList<>();
                    naarIngang.add(new Point(midden.x, ingang.y));
                    naarIngang.add(ingang);
                    setPad(naarIngang);
                    status = Status.VERLAAT_KAMER;
                }
            }

            // ── VERLAAT_KAMER: emergency-vlag resetten na schoonmaken ────
            case VERLAAT_KAMER -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    doelKamer.setCleaningEmergency(false); // kamer is nu schoon
                    doelKamer.verlaatAlsSchoonmaker();
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
                boolean aangekomen = updateInTrap(doelVerdieping);
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

    // ── kiesEnLoopNaarKamer: eerst emergency-kamers, dan willekeurig ─────
    private void kiesEnLoopNaarKamer() {
        List<HotelRuimte> alleKamers = hotel.getKamers();
        if (alleKamers.isEmpty()) return;

        // Stap 1: zoek een kamer met cleaning emergency
        for (HotelRuimte kandidaat : alleKamers) {
            boolean heeftEmergency   = kandidaat.isCleaningEmergency();
            boolean isNietVol        = !kandidaat.isVol();
            if (!heeftEmergency || !isNietVol) continue;

            Point ingang             = Pathfinder.getKamerIngang(kandidaat);
            Point midden             = getTrueMidden(kandidaat);
            boolean ongeldigeKamer   = midden.y >= ingang.y;
            if (ongeldigeKamer) continue;

            int kamerVerdieping      = getNabijeStop(kandidaat.getY());
            boolean zelfdeVerdieping = kamerVerdieping == huidigeVerdieping;

            if (zelfdeVerdieping) {
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
                doelKamer      = kandidaat;
                doelVerdieping = kamerVerdieping;
                loopNaarSchachtOfTrap();
                boolean onderweg = status == Status.LOOPT_NAAR_SCHACHT
                        || status == Status.LOOP_NAAR_TRAP;
                if (onderweg) return;
                doelKamer.verlaatAlsSchoonmaker();
                doelKamer = null;
            }
        }

        // Stap 2: geen emergency, kies willekeurige kamer
        for (int poging = 0; poging < 15; poging++) {
            HotelRuimte kandidaat    = alleKamers.get(random.nextInt(alleKamers.size()));
            if (kandidaat.isVol()) continue;

            Point ingang             = Pathfinder.getKamerIngang(kandidaat);
            Point midden             = getTrueMidden(kandidaat);
            boolean ongeldigeKamer   = midden.y >= ingang.y;
            if (ongeldigeKamer) continue;

            int kamerVerdieping      = getNabijeStop(kandidaat.getY());
            boolean zelfdeVerdieping = kamerVerdieping == huidigeVerdieping;

            if (zelfdeVerdieping) {
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
                doelKamer      = kandidaat;
                doelVerdieping = kamerVerdieping;
                loopNaarSchachtOfTrap();
                boolean onderweg = status == Status.LOOPT_NAAR_SCHACHT
                        || status == Status.LOOP_NAAR_TRAP;
                if (onderweg) return;
                doelKamer.verlaatAlsSchoonmaker();
                doelKamer = null;
            }
        }
    }

    private void loopNaarSchachtOfTrap() {
        loopNaarSchachtOfTrapGemeen(
                huidigeVerdieping,
                () -> status = Status.LOOPT_NAAR_SCHACHT,
                () -> status = Status.LOOP_NAAR_TRAP
        );
    }

    // ── isInGang: true als schoonmaker vrij is voor een emergency ────────
    public boolean isInGang() {
        boolean looptNaarKamer = status == Status.LOOPT_NAAR_INGANG;
        boolean looptNaarLift  = status == Status.LOOPT_NAAR_SCHACHT;
        boolean looptNaarTrap  = status == Status.LOOP_NAAR_TRAP;
        boolean looptDoorTrap  = status == Status.LOOP_DOOR_TRAP;
        boolean wachtOpLift    = status == Status.WACHT_OP_LIFT;
        boolean zitInLift      = status == Status.IN_LIFT;
        return looptNaarKamer || looptNaarLift || looptNaarTrap
                || looptDoorTrap  || wachtOpLift   || zitInLift;
    }

    // ── reageerOpEmergency: onderbreek huidige taak, ga naar noodkamer ───
    public void reageerOpEmergency(HotelRuimte noodkamer) {
        if (doelKamer != null) {
            doelKamer.verlaatAlsSchoonmaker();
        }

        noodkamer.betreedAlsSchoonmaker();
        doelKamer = noodkamer;

        int kamerVerdieping      = getNabijeStop(noodkamer.getY());
        boolean zelfdeVerdieping = kamerVerdieping == huidigeVerdieping;

        if (zelfdeVerdieping) {
            Point ingang         = Pathfinder.getKamerIngang(noodkamer);
            List<Point> padNaarKamer = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());

            boolean padGevonden  = !padNaarKamer.isEmpty();
            if (padGevonden) {
                setPad(padNaarKamer);
                status = Status.LOOPT_NAAR_INGANG;
            }
        } else {
            doelVerdieping = kamerVerdieping;
            loopNaarSchachtOfTrap();
        }
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
            doelKamer.verlaatAlsSchoonmaker();
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
            case WACHT_OP_LIFT                           -> new Color(200, 100, 255);
            case BETREEDT_KAMER, VERLAAT_KAMER, IN_KAMER -> new Color(180, 50, 220);
            default                                       -> new Color(160, 32, 240);
        };

        g.setColor(kleur);
        g.fillOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("S", pixelX - 4, pixelY + 5);

        if (doelKamer != null && (status == Status.BETREEDT_KAMER
                || status == Status.IN_KAMER || status == Status.VERLAAT_KAMER)) {
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            g.setColor(Color.BLACK);
            g.drawString("🧹", pixelX - 8, pixelY - 13);
        }
    }
}