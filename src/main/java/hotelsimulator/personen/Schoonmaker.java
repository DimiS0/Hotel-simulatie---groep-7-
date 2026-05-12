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
        WACHT_OP_SPAWN,
        WACHT_OP_WERK,        // staat stil naast lobby, wacht op kamer in wachtrij
        LOOPT_NAAR_INGANG,
        LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT,
        IN_LIFT,
        BETREEDT_KAMER,
        IN_KAMER,
        VERLAAT_KAMER,
        LOOP_NAAR_TRAP,
        LOOP_DOOR_TRAP,
        LOOP_TERUG_NAAR_POST  // loopt terug naar wachtpositie
    }

    // Wachtpositie: naast de lobby (rechts van de lift/schacht, op de lobbyverdieping)
    public static final int WACHT_X = 350;  // midden van de lobby
    public static final int WACHT_Y = 450;  // lobbyverdieping (rij 9 = y=450)

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelKamer doelKamer;
    private long verblijfEinde = 0;
    private static final long VERBLIJF_MS = 7000;

    private int doelVerdieping    = 8;
    private int huidigeVerdieping = 8;

    public Schoonmaker(Lift lift, Schacht schacht, Hotel hotel,
                       HotelEventManager hotelEventManager,
                       SimulatieConfig simulatieConfig) {
        super(WACHT_X, WACHT_Y, lift, schacht, hotel, hotelEventManager, simulatieConfig);
    }

    @Override
    public void update() {
        switch (status) {

            case WACHT_OP_SPAWN -> {}

            // Stilstaan op wachtpost — controleer elke frame of er werk is
            case WACHT_OP_WERK -> {
                HotelKamer volgende = hotel.pakVolgendeSchoonmaakKamer();
                if (volgende != null) {
                    startSchoonmaakRonde(volgende);
                }
            }

            case LOOPT_NAAR_INGANG -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
                    if (midden.y >= ingang.y) {
                        // Ongeldige kamer — terugzetten en verder wachten
                        doelKamer.verlaatAlsSchoonmaker();
                        doelKamer = null;
                        loopTerugNaarPost();
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
                    doelKamer.betreedAlsSchoonmaker();
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis() + VERBLIJF_MS;
                }
            }

            case IN_KAMER -> {
                if (System.currentTimeMillis() >= verblijfEinde) {
                    // Klaar met schoonmaken
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
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
                    doelKamer.setCleaningEmergency(false);
                    doelKamer.maakReserveringVrij();   // kamer vrijgeven zodat nieuwe gasten kunnen inchecken
                    doelKamer.verlaatAlsSchoonmaker();
                    doelKamer = null;
                    // Loop terug naar wachtpost en wacht op volgend werk
                    loopTerugNaarPost();
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
                        if (!p.isEmpty()) {
                            setPad(p);
                            status = Status.LOOPT_NAAR_INGANG;
                            break;
                        }
                        doelKamer.verlaatAlsSchoonmaker();
                        doelKamer = null;
                    }
                    loopTerugNaarPost();
                }
            }

            case LOOP_TERUG_NAAR_POST -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    huidigeVerdieping = 8;
                    status = Status.WACHT_OP_WERK;
                }
            }
        }
    }

    // Reageer direct op een emergency (aangeroepen vanuit CleaningEmergency)
    public void reageerOpEmergency(HotelKamer noodkamer) {
        if (status != Status.WACHT_OP_WERK) return; // alleen als hij vrij staat te wachten

        noodkamer.betreedAlsSchoonmaker();
        doelKamer = noodkamer;

        int kamerVerdieping = getNabijeStop(noodkamer.getY());
        if (kamerVerdieping == huidigeVerdieping) {
            Point ingang = Pathfinder.getKamerIngang(noodkamer);
            List<Point> padNaarKamer = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
            if (!padNaarKamer.isEmpty()) {
                setPad(padNaarKamer);
                status = Status.LOOPT_NAAR_INGANG;
            }
        } else {
            doelVerdieping = kamerVerdieping;
            loopNaarSchachtOfTrap();
        }
    }

    public boolean isVrij() {
        return status == Status.WACHT_OP_WERK;
    }

    private void startSchoonmaakRonde(HotelKamer kamer) {
        doelKamer = kamer;
        kamer.betreedAlsSchoonmaker();

        int kamerVerdieping = getNabijeStop(kamer.getY());

        if (kamerVerdieping == huidigeVerdieping) {
            Point ingang = Pathfinder.getKamerIngang(kamer);
            List<Point> p = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
            if (!p.isEmpty()) {
                setPad(p);
                status = Status.LOOPT_NAAR_INGANG;
            } else {
                // Pad niet gevonden — zet kamer terug en wacht
                kamer.verlaatAlsSchoonmaker();
                hotel.voegToeAanSchoonmaakWachtrij(kamer);
                doelKamer = null;
            }
        } else {
            doelVerdieping = kamerVerdieping;
            loopNaarSchachtOfTrap();
        }
    }

    private void loopTerugNaarPost() {
        List<Point> terugPad = Pathfinder.vindPad(
                pixelX, pixelY, WACHT_X, WACHT_Y, hotel.getRuimtes());
        if (!terugPad.isEmpty()) {
            setPad(terugPad);
            status = Status.LOOP_TERUG_NAAR_POST;
        } else {
            // Direct naar post teleporteren als pad niet gevonden
            setPositie(WACHT_X, WACHT_Y);
            huidigeVerdieping = 8;
            status = Status.WACHT_OP_WERK;
        }
    }

    private void loopNaarSchachtOfTrap() {
        loopNaarSchachtOfTrapGemeen(
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
            if (!p.isEmpty()) {
                setPad(p);
                status = Status.LOOPT_NAAR_INGANG;
                return;
            }
            doelKamer.verlaatAlsSchoonmaker();
            doelKamer = null;
        }
        loopTerugNaarPost();
    }

    @Override
    public int getDoelVerdieping() { return doelVerdieping; }

    @Override
    public void activeer() {
        setPositie(WACHT_X, WACHT_Y);
        pad.clear();
        doelKamer = null;
        huidigeVerdieping = 8;
        status = Status.WACHT_OP_WERK;
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
            case WACHT_OP_WERK                           -> new Color(180, 180, 180); // grijs = wacht
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

        if (status == Status.WACHT_OP_WERK) {
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
            g.setColor(Color.BLACK);
            g.drawString("💤", pixelX - 8, pixelY - 13);
        } else if (doelKamer != null && (status == Status.BETREEDT_KAMER
                || status == Status.IN_KAMER || status == Status.VERLAAT_KAMER)) {
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            g.setColor(Color.BLACK);
            g.drawString("🧹", pixelX - 8, pixelY - 13);
        }
    }
}