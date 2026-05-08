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
        WACHT_OP_SPAWN, WACHT_IN_LOBBY, LOOPT_NAAR_INGANG, LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT, IN_LIFT, LOOP_DOOR_TRAP, LOOP_NAAR_TRAP,
        BETREEDT_KAMER, IN_KAMER, VERLAAT_KAMER
    }

    private int guestID;
    private boolean wachtOpCheckIn = false;
    private HotelKamer toegewezenKamer;
    private int gewildeSterren = 0;

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;

    public static final int SPAWN_X = 200;
    public static final int SPAWN_Y = 500;

    private int doelVerdieping    = 8;
    private int huidigeVerdieping = 8;

    public Gast(Lift lift, Schacht schacht, Hotel hotel,
                HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig, int guestID) {
        super(SPAWN_X, SPAWN_Y, lift, schacht, hotel, hotelEventManager, simulatieConfig);
        this.guestID = guestID;
    }

    public int getGuestID() {
        return guestID;
    }

    @Override
    public void update() {
        switch (status) {

            case WACHT_OP_SPAWN -> {
            }

            case WACHT_IN_LOBBY -> {
                //
                if (wachtOpCheckIn) {
                    wachtOpCheckIn = false;

                    //vrije kamer vinden met de aantal sterren
                    HotelKamer kamer = hotel.zoekVrijeHotelKamer(gewildeSterren);

                    //kamer is niet gevonden, verwijderen van gast
                    if (kamer == null) {
                        System.out.println("Geen kamer beschikbaar voor gast "
                                + guestID + " — gast verwijderd");
                        hotel.verwijderPersoon(this);
                        return;
                    }
                    //kamer gevonden, gast naar zijn kamer bewegen
                    startCheckInNaarKamer(kamer);
                }
            }
            case LOOPT_NAAR_INGANG -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);       // ← uit Persoon
                    if (midden.y >= ingang.y) {
                        doelKamer.verlaat();
                        doelKamer = null;
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
                    doelKamer.betreedAlsGast();
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis()
                            + (long)(doelKamer.getVerblijfMs() / simulatieConfig.getSnelheid().getFactor());
                }
            }

            case IN_KAMER -> {
                if (doelKamer instanceof  HotelKamer){
                    return;
                }
                if  (System.currentTimeMillis() >= verblijfEinde) {
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

                    HotelRuimte verlatenRuimte = doelKamer;
                    doelKamer = null;

                    // later terug naar eigen kamer:
                    if (!(verlatenRuimte instanceof HotelKamer) && toegewezenKamer != null) {
                        startCheckInNaarKamer(toegewezenKamer);
                    }
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

                }
            }
        }
    }

    private void gaNaarRuimte(HotelRuimte ruimte) {
        //algemene methode om een gast naar een ruimte te sturen
        //wordt door alle toekomstige events gebruikt die te maken hebben met lopen bijvoorbeeld

        //geen ruimte niet lopen
            if (ruimte == null) return;

            doelKamer = ruimte;

            Point ingang = Pathfinder.getKamerIngang(ruimte);
            Point midden = getTrueMidden(ruimte);

            //zonder ingang of doelpositie kan er geen pad worden berekend
            if (ingang == null || midden == null) return;

            //voorkomt dat een gast op een andere manier de kamer binnen komt
            if (midden.y >= ingang.y) return;

            int verdieping = getNabijeStop(ruimte.getY());

            //besteming op de verdieping, direct een pad berekenen
            if (verdieping == huidigeVerdieping) {
                List<Point> nieuwPad = Pathfinder.vindPad(
                        pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());

                if (!nieuwPad.isEmpty()) {
                    setPad(nieuwPad);
                    status = Status.LOOPT_NAAR_INGANG;
                }
            } else {
                //als bestemming op andere verdieping dan kiezen we tussen lift of trap
                doelVerdieping = verdieping;
                loopNaarSchachtOfTrap();
            }
        }

    // Roept de gedeelde methode in Persoon aan met Gast-specifieke status-setters
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
            if (!p.isEmpty()) { setPad(p); status = Status.LOOPT_NAAR_INGANG; return; }
            doelKamer.verlaat();
            doelKamer = null;
        }
    }

    @Override
    public int getDoelVerdieping() { return doelVerdieping; }

    @Override
    public void activeer() {
        pixelX = SPAWN_X; pixelY = SPAWN_Y;
        pad.clear();
        doelKamer = null;
        huidigeVerdieping = 8;
        status = Status.WACHT_IN_LOBBY;
    }

    public void handleCheckIn(int gewildeSterren) {
        //sterren opslaan bij de gast om te gebruiken om kamer te vinden
        this.gewildeSterren = gewildeSterren;

        //boolean zodat lobby weet dat gast mag inchecken
        wachtOpCheckIn = true;
    }

    private void startCheckInNaarKamer(HotelRuimte kamer) {
        //geen kamer? doen we niks dan
        if (kamer == null) return;

        //zorgen dat we alleen HotelKamers hebben
        if (!(kamer instanceof HotelKamer)) return;

        //Kamer bewaren  zodat de gast zijn eigen kamer weet
        toegewezenKamer = (HotelKamer) kamer;

        //start de route naar de kamer
        gaNaarRuimte(toegewezenKamer);
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