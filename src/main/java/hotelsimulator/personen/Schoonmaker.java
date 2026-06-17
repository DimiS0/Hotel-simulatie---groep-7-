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

    private Status status = Status.WACHT_OP_SPAWN;
    // de kamer die de schoonmaker aan het schoonmaken is
    private HotelKamer doelKamer;
    private long verblijfEinde = 0;
    // hoelang de schoonmaker in een kamer blijft om te schoonmaken
    private static final long VERBLIJF_MS = 7000;
    // vlag om bij te houden of de schoonmaker na lift/trap naar de post moet
    private boolean gaatTerugNaarPost = false;
    private int doelVerdieping;
    private int huidigeVerdieping;

    // Wachtpositie: naast de lobby (rechts van de lift/schacht, op de lobbyverdieping)
    protected int WACHT_Y;
    protected int WACHT_X;

    public Schoonmaker(Lift lift, Schacht schacht, Hotel hotel,
                       HotelEventManager hotelEventManager,
                       SimulatieConfig simulatieConfig, int maxBreedte, int maxHoogte) {
        super(berekenSchoonmakerPauzePositie(maxBreedte), maxHoogte * 50, lift, schacht, hotel, hotelEventManager, simulatieConfig);
        this.WACHT_X = berekenSchoonmakerPauzePositie(maxBreedte);
        this.WACHT_Y = maxHoogte * 50;
        // verdieping instellen op de lobbyverdieping (eerste stop = hoogste gridY = onderste verdieping)
        int[] stops = lift.getVerdiepingenY();
        this.doelVerdieping = stops[0];
        this.huidigeVerdieping = stops[0];
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

                    // controleer of de ingang boven het midden ligt, anders is de kamer ongeldig
                    if (midden.y >= ingang.y) {
                        // Ongeldige kamer — terugzetten en verder wachten
                        doelKamer.verlaatAlsSchoonmaker();
                        doelKamer = null;
                        loopTerugNaarPost();
                        return;
                    }

                    // pad berekenen van ingang naar midden van de kamer om binnen te lopen
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
                    // schoonmaker aanmelden bij de lift op de huidige verdieping
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
                    // kamer betreden en schoonmaaktimer starten
                    doelKamer.betreedAlsSchoonmaker();
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis() + VERBLIJF_MS;
                }
            }

            case IN_KAMER -> {
                // schoonmaaktijd verstreken, kamer verlaten
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
                    // schoonmaker op de ingangspositie van de verlaten kamer zetten
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    // cleaning emergency uitzetten en kamer vrijgeven voor nieuwe gasten
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
                    // schoonmaker op de trapkolom zetten en trap beweging starten
                    pixelX = TRAP_PIXEL_X;
                    pixelYDouble = pixelY;
                    status = Status.LOOP_DOOR_TRAP;
                }
            }

            case LOOP_DOOR_TRAP -> {
                boolean aangekomen = updateInTrap(doelVerdieping);
                if (aangekomen) {
                    huidigeVerdieping = doelVerdieping;

                    // als de schoonmaker een doelkamer heeft, daarheen lopen
                    if (doelKamer != null) {
                        Point ingang = Pathfinder.getKamerIngang(doelKamer);
                        List<Point> p = Pathfinder.vindPad(
                                pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);
                        if (!p.isEmpty()) {
                            setPad(p);
                            status = Status.LOOPT_NAAR_INGANG;
                            break;
                        }
                        doelKamer.verlaatAlsSchoonmaker();
                        doelKamer = null;
                    }

                    // vlag resetten als we op de lobbyverdieping zijn aangekomen
                    if (gaatTerugNaarPost) {
                        gaatTerugNaarPost = false;
                    }
                    // terugkeren naar de wachtpost
                    loopTerugNaarPost();
                }
            }

            case LOOP_TERUG_NAAR_POST -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    // verdieping bijwerken naar de lobbyverdieping na aankomst op de post
                    int[] stops = hotel.getLift().getVerdiepingenY();
                    huidigeVerdieping = stops[0];
                    status = Status.WACHT_OP_WERK;
                }
            }
        }
    }

    private void startSchoonmaakRonde(HotelKamer kamer) {
        doelKamer = kamer;
        kamer.betreedAlsSchoonmaker();

        // dichtstbijzijnde liftstop bepalen voor de doelkamer
        int kamerVerdieping = getNabijeStop(kamer.getY());

        // als de kamer op de huidige verdieping zit, direct een pad berekenen
        if (kamerVerdieping == huidigeVerdieping) {
            Point ingang = Pathfinder.getKamerIngang(kamer);
            List<Point> p = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);
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
            // kamer op andere verdieping, lift of trap nemen
            doelVerdieping = kamerVerdieping;
            loopNaarSchachtOfTrap();
        }
    }

    // wachtpositie berekenen op basis van de breedte van het hotel
    private static int berekenSchoonmakerPauzePositie(int maxBreedte) {
        return (maxBreedte) * 50;
    }

    private void loopTerugNaarPost() {
        int[] stops = hotel.getLift().getVerdiepingenY();
        int lobbyVerdieping = stops[0];

        // als de schoonmaker niet op de lobbyverdieping zit, eerst daarheen via lift of trap
        if (huidigeVerdieping != lobbyVerdieping) {
            doelVerdieping = lobbyVerdieping;
            gaatTerugNaarPost = true;
            loopNaarSchachtOfTrap();
            return;
        }

        // pad berekenen naar de wachtpost op de lobbyverdieping
        List<Point> terugPad = Pathfinder.vindPad(
                pixelX, pixelY, WACHT_X, WACHT_Y, hotel.getRuimtes(), hotel);
        if (!terugPad.isEmpty()) {
            setPad(terugPad);
            status = Status.LOOP_TERUG_NAAR_POST;
        } else {
            // Direct naar post teleporteren als pad niet gevonden
            setPositie(WACHT_X, WACHT_Y);
            huidigeVerdieping = lobbyVerdieping;
            status = Status.WACHT_OP_WERK;
        }
    }

    public void berekenHuidigeVerdiepingEnDoelVerdieping() {
        huidigeVerdieping = hotel.getMaxHoogte() - 1; // middelste rij van begane grond
        doelVerdieping = huidigeVerdieping;
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

        // als de schoonmaker een doelkamer heeft, daarheen lopen na uitstappen
        if (doelKamer != null) {
            Point ingang = Pathfinder.getKamerIngang(doelKamer);
            List<Point> p = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);
            if (!p.isEmpty()) {
                setPad(p);
                status = Status.LOOPT_NAAR_INGANG;
                return;
            }
            doelKamer.verlaatAlsSchoonmaker();
            doelKamer = null;
        }

        // vlag resetten als we op de lobbyverdieping zijn aangekomen via lift
        if (gaatTerugNaarPost) {
            gaatTerugNaarPost = false;
        }
        // terugkeren naar de wachtpost
        loopTerugNaarPost();
    }

    @Override
    public int getDoelVerdieping() { return doelVerdieping; }

    @Override
    public void activeer() {
        setPositie(WACHT_X, WACHT_Y);
        pad.clear();
        doelKamer = null;
        // verdieping instellen op de lobbyverdieping bij activeren
        int[] stops = hotel.getLift().getVerdiepingenY();
        huidigeVerdieping = stops[0];
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
            case WACHT_OP_WERK -> new Color(180, 180, 180); // grijs = wacht
            case WACHT_OP_LIFT -> new Color(200, 100, 255);
            case BETREEDT_KAMER, VERLAAT_KAMER, IN_KAMER -> new Color(180, 50, 220);
            default -> new Color(160, 32, 240);
        };

        g.setColor(kleur);
        g.fillOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(pixelX - 10, pixelY - 10, 20, 20);
        g.setColor(Color.WHITE);
        g.drawString("S", pixelX - 4, pixelY + 5);

        // emoji boven de schoonmaker tonen op basis van de huidige status
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