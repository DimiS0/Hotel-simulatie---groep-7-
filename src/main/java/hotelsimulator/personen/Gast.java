package hotelsimulator.personen;

import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Gast extends Persoon {

    public enum Status {
        WACHT_OP_SPAWN, WACHT_IN_LOBBY, LOOPT_NAAR_INGANG, LOOPT_NAAR_SCHACHT,
        WACHT_OP_LIFT, IN_LIFT, LOOP_DOOR_TRAP, LOOP_NAAR_TRAP,
        BETREEDT_KAMER, IN_KAMER, VERLAAT_KAMER,LOOP_NAAR_LOBBY
    }

    private int guestID;
    private boolean wachtOpCheckIn = false;
    private HotelKamer toegewezenKamer;
    private int gewildeSterren = 0;
    private boolean isWachtOpCheckOut;
    private String huidigeKamerEmoji = "";

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;
    private final LinkedList<HotelRuimte> eventWachtrij = new LinkedList<>();
    private boolean gaatNaarLobby = false;
    private int doelVerdieping;
    private int huidigeVerdieping;
    private int SPAWN_X;
    private int SPAWN_Y;

    public Gast(Lift lift, Schacht schacht, Hotel hotel,
                HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig, int guestID, int maxBreedte, int maxHoogte) {
        super(berekenGastSpawnLocatie(maxBreedte), (maxHoogte + 1) * 50 - 25, lift, schacht, hotel, hotelEventManager, simulatieConfig);
        this.guestID = guestID;
        this.SPAWN_X = berekenGastSpawnLocatie(maxBreedte);
        this.SPAWN_Y = (maxHoogte + 1) * 50 - 25;
        berekenHuidigeVerdiepingEnDoelVerdieping();
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
                        markeerVoorVerwijdering();
                        return;
                    }
                    //kamer gevonden, gast naar zijn kamer bewegen
                    startCheckInNaarKamer(kamer);
                }
            }
            case LOOPT_NAAR_INGANG -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else {  System.out.println("Gast " + guestID + " aangekomen bij ingang van: "
                        + (doelKamer != null ? doelKamer.getAreaType() : "null")
                        + " | wachtrij: " + eventWachtrij.size());
                    if (doelKamer == null) {
                        status = Status.WACHT_IN_LOBBY;  // ← eerst status resetten
                        if (!eventWachtrij.isEmpty()) {
                            HotelRuimte volgend = eventWachtrij.poll();
                            gaNaarRuimte(volgend);
                        } else if (toegewezenKamer != null) {
                            gaNaarRuimte(toegewezenKamer);
                        }
                        return;
                    }

                    if (!eventWachtrij.isEmpty()) {
                        HotelRuimte volgend = eventWachtrij.peek();
                        Point volgendIngang = Pathfinder.getKamerIngang(volgend);
                        List<Point> testPad = Pathfinder.vindPad(
                                pixelX, pixelY, volgendIngang.x, volgendIngang.y, hotel.getRuimtes(), hotel);
                        if (!testPad.isEmpty()) {
                            eventWachtrij.poll();
                            doelKamer.verlaat();
                            doelKamer = null;
                            gaNaarRuimte(volgend);
                            return;
                        } else {
                            eventWachtrij.clear();
                        }
                    }

                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
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
                    huidigeKamerEmoji = doelKamer.getAreaType();
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis()
                            + (long)(doelKamer.getVerblijfMs() / simulatieConfig.getSnelheid().getFactor());
                }
            }
            case LOOP_NAAR_LOBBY ->{

                if (!pad.isEmpty()) {
                    beweeg();
                } else {
                    // aangekomen in lobby
                    if (doelKamer != null) {
                        doelKamer.verlaat();
                        doelKamer = null;
                    }

                    if (toegewezenKamer != null) {
                        toegewezenKamer.verlaat();
                        toegewezenKamer = null;
                    }
                    markeerVoorVerwijdering();
                }
            }

            case IN_KAMER -> {
                if (isWachtOpCheckOut) {
                    isWachtOpCheckOut = false;

                    if (toegewezenKamer != null) {
                        hotel.voegToeAanSchoonmaakWachtrij(toegewezenKamer);
                    }

                    doelKamer = null;
                    int lobbyVerdieping = hotel.getLobbyVerdieping();

                    if (huidigeVerdieping == lobbyVerdieping) {
                        Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
                        List<Point> pad = Pathfinder.vindPad(
                                pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
                        if (!pad.isEmpty()) {
                            setPad(pad);
                            status = Status.LOOP_NAAR_LOBBY;
                        }
                    } else {
                        gaatNaarLobby = true;
                        doelVerdieping = lobbyVerdieping;
                        loopNaarSchachtOfTrap();
                    }
                    return;
                }


                if (doelKamer instanceof HotelKamer) {
                    return;
                }

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
            case VERLAAT_KAMER -> {
                if (!pad.isEmpty()) {
                    beweeg();
                } else { System.out.println("Gast " + guestID + " verlaat: "
                        + (doelKamer != null ? doelKamer.getAreaType() : "null")
                        + " | toegewezenKamer: " + (toegewezenKamer != null ? toegewezenKamer.getAreaType() : "null")
                        + " | wachtrij: " + eventWachtrij.size());
                    if (doelKamer == null) { status = Status.WACHT_IN_LOBBY; return; }
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    doelKamer.verlaat();
                    HotelRuimte verlatenRuimte = doelKamer;
                    doelKamer = null;
                    huidigeKamerEmoji = "";

                    // Wachtrij eerst checken
                    if (!eventWachtrij.isEmpty()) {
                        HotelRuimte volgend = eventWachtrij.poll();
                        gaNaarRuimte(volgend);
                        return;
                    }

                    if (!(verlatenRuimte instanceof HotelKamer) && toegewezenKamer != null) {
                        gaNaarRuimte(toegewezenKamer);
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
                boolean aangekomen = updateInTrap(doelVerdieping);
                if (aangekomen) {
                    huidigeVerdieping = doelVerdieping;
                    if (doelKamer != null) {
                        Point ingang = Pathfinder.getKamerIngang(doelKamer);
                        List<Point> p = Pathfinder.vindPad(
                                pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);
                        if (!p.isEmpty()) { setPad(p); status = Status.LOOPT_NAAR_INGANG; break; }
                        doelKamer.verlaat();
                        doelKamer = null;
                    }
                    // Na aankomst op verdieping
                    if (gaatNaarLobby && huidigeVerdieping == doelVerdieping) {
                        gaatNaarLobby = false;
                        Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
                        List<Point> naarLobby = Pathfinder.vindPad(
                                pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
                        if (!naarLobby.isEmpty()) {
                            setPad(naarLobby);
                            status = Status.LOOP_NAAR_LOBBY;
                        } else {
                            markeerVoorVerwijdering();
                        }
                        return;
                    }
                    // Geen doelkamer — check of we naar lobby moeten
                    if (doelKamer == null) {
                        int lobbyVerdieping = hotel.getLift().getVerdiepingenY()[hotel.getLift().getVerdiepingenY().length - 1];
                        if (huidigeVerdieping == lobbyVerdieping) {
                            Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
                            List<Point> naarLobby = Pathfinder.vindPad(
                                    pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
                            if (!naarLobby.isEmpty()) {
                                setPad(naarLobby);
                                status = Status.LOOP_NAAR_LOBBY;
                            } else {
                                markeerVoorVerwijdering();
                            }
                        }
                    }
                }
            }
        }
    }

    public void berekenHuidigeVerdiepingEnDoelVerdieping(){
        huidigeVerdieping = 2;
        for(int i = 1; i < hotel.getverdiepingen(); i++){
            huidigeVerdieping += 3;
        }
        doelVerdieping = huidigeVerdieping;
    }

    public void gaNaarRuimte(HotelRuimte ruimte) {
        //algemene methode om een gast naar een ruimte te sturen
        //wordt door alle toekomstige events gebruikt die te maken hebben met lopen bijvoorbeeld

        //geen ruimte niet lopen
        if (ruimte == null) return;
        if (ruimte.isCleaningEmergency()) {
            System.out.println("Gast " + guestID + " kan niet naar " + ruimte.getAreaType() + " — cleaning emergency!");
            return;
        }
        System.out.println("Gast " + guestID + " gaNaarRuimte: " + ruimte.getAreaType()
                + " | status: " + status
                + " | doelKamer: " + (doelKamer != null ? doelKamer.getAreaType() : "null")
                + " | wachtrij: " + eventWachtrij.size());

        // Als gast bezig is met lopen of in kamer zit, event bewaren voor later
        boolean isBezig = status == Status.LOOPT_NAAR_INGANG
                || status == Status.LOOPT_NAAR_SCHACHT
                || status == Status.LOOP_NAAR_TRAP
                || status == Status.LOOP_DOOR_TRAP
                || status == Status.IN_LIFT
                || status == Status.WACHT_OP_LIFT
                || status == Status.BETREEDT_KAMER;

        if (isBezig) {
            // Alleen bewaren als het een speciale kamer is, geen hotelkamer
            if (!(ruimte instanceof HotelKamer)) {
                eventWachtrij.clear();
                eventWachtrij.add(ruimte);
            }
            return;
        }

        // Als gast in kamer zit, eerst verlaten
        if (status == Status.IN_KAMER && doelKamer != null) {
            if (!(ruimte instanceof HotelKamer)) {
                eventWachtrij.clear();
                eventWachtrij.add(ruimte);
                Point ingang = Pathfinder.getKamerIngang(doelKamer);
                Point midden = getTrueMidden(doelKamer);
                List<Point> naarIngang = new ArrayList<>();
                naarIngang.add(new Point(midden.x, ingang.y));
                naarIngang.add(ingang);
                setPad(naarIngang);
                status = Status.VERLAAT_KAMER;
            }
            return;
        }
        doelKamer = ruimte;

        Point ingang = Pathfinder.getKamerIngang(ruimte);
        Point midden = getTrueMidden(ruimte);

        //zonder ingang of doelpositie kan er geen pad worden berekend
        if (ingang == null || midden == null) return;

        //voorkomt dat een gast op een andere manier de kamer binnen komt
        if (midden.y >= ingang.y) {
            return;}

        int verdieping = getNabijeStop(ruimte.getY());

        //besteming op de verdieping, direct een pad berekenen
        if (verdieping == huidigeVerdieping) {
            List<Point> nieuwPad = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);

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

    private static int berekenGastSpawnLocatie(int maxBreedte){
        if (maxBreedte > 4){
            return (maxBreedte - 3) * 50;
        }
        else{
            return 150;
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
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes(), hotel);
            if (!p.isEmpty()) { setPad(p); status = Status.LOOPT_NAAR_INGANG; return; }
            doelKamer.verlaat();
            doelKamer = null;
        }
        // Na aankomst op verdieping
        if (gaatNaarLobby && huidigeVerdieping == doelVerdieping) {
            gaatNaarLobby = false;
            Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
            List<Point> naarLobby = Pathfinder.vindPad(
                    pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
            if (!naarLobby.isEmpty()) {
                setPad(naarLobby);
                status = Status.LOOP_NAAR_LOBBY;
            } else {
                markeerVoorVerwijdering();
            }
            return;
        }
        // Geen doelkamer — check of we naar lobby moeten
        int lobbyVerdieping = hotel.getLift().getVerdiepingenY()[hotel.getLift().getVerdiepingenY().length - 1];
        if (huidigeVerdieping == lobbyVerdieping) {
            Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
            List<Point> naarLobby = Pathfinder.vindPad(
                    pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
            if (!naarLobby.isEmpty()) {
                setPad(naarLobby);
                status = Status.LOOP_NAAR_LOBBY;
            } else {
                markeerVoorVerwijdering();
            }
        }
    }

    @Override
    public int getDoelVerdieping() { return doelVerdieping; }

    @Override
    public void activeer() {
        pixelX = SPAWN_X; pixelY = SPAWN_Y;
        pad.clear();
        doelKamer = null;
        status = Status.WACHT_IN_LOBBY;
    }

    public void handleCheckIn(int gewildeSterren) {
        //sterren opslaan bij de gast om te gebruiken om kamer te vinden
        this.gewildeSterren = gewildeSterren;

        //boolean zodat lobby weet dat gast mag inchecken
        wachtOpCheckIn = true;
    }
    public void checkOutHandleIn() {
        if (status == Status.IN_KAMER && doelKamer instanceof HotelKamer) {
            isWachtOpCheckOut = true;
            return;
        }

        if (doelKamer != null) {
            doelKamer.verlaat();
            doelKamer = null;
        }

        if (toegewezenKamer != null) {
            hotel.voegToeAanSchoonmaakWachtrij(toegewezenKamer);
            toegewezenKamer.verlaat();
            toegewezenKamer = null;
        }

        int lobbyVerdieping = hotel.getLobbyVerdieping();

        if (huidigeVerdieping == lobbyVerdieping) {
            Point lobbyPunt = new Point(SPAWN_X, SPAWN_Y);
            List<Point> naarLobby = Pathfinder.vindPad(
                    pixelX, pixelY, lobbyPunt.x, lobbyPunt.y, hotel.getRuimtes(), hotel);
            if (!naarLobby.isEmpty()) {
                setPad(naarLobby);
                status = Status.LOOP_NAAR_LOBBY;
            } else {
                markeerVoorVerwijdering();
            }
        } else {
            gaatNaarLobby = true;
            doelVerdieping = lobbyVerdieping;
            loopNaarSchachtOfTrap();
        }
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

    // In Gast.java — verander de drie methodes:

    public void startGoToBioscoop() {
        List<HotelRuimte> kandidaten = new ArrayList<>();
        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            if (ruimte instanceof Bioscoop && !ruimte.isVol() && !ruimte.isCleaningEmergency()) {
                kandidaten.add(ruimte);
            }
        }
        if (kandidaten.isEmpty()) {
            System.out.println("Geen beschikbare bioscoop gevonden!");
            return;
        }
        gaNaarRuimte(kandidaten.get(random.nextInt(kandidaten.size())));
    }

    public void startGoToRestaurant() {
        List<HotelRuimte> kandidaten = new ArrayList<>();
        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            if (ruimte instanceof Restaurant && !ruimte.isVol() && !ruimte.isCleaningEmergency()) {
                kandidaten.add(ruimte);
            }
        }
        if (kandidaten.isEmpty()) {
            System.out.println("Geen beschikbaar restaurant gevonden!");
            return;
        }
        gaNaarRuimte(kandidaten.get(random.nextInt(kandidaten.size())));
    }

    public void startGoToFitness() {
        List<HotelRuimte> kandidaten = new ArrayList<>();
        for (HotelRuimte ruimte : hotel.getRuimtes()) {
            if (ruimte instanceof FitnessRuimtes && !ruimte.isVol() && !ruimte.isCleaningEmergency()) {
                kandidaten.add(ruimte);
            }
        }
        if (kandidaten.isEmpty()) {
            System.out.println("Geen beschikbare fitness gevonden!");
            return;
        }
        gaNaarRuimte(kandidaten.get(random.nextInt(kandidaten.size())));
    }

    public int getHuidigeVerdieping() {
        return huidigeVerdieping;
    }

    public Status getStatus() {
        return status;
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
    public HotelKamer getToegwezenKamer() {
        return toegewezenKamer;
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
            String emoji = switch (huidigeKamerEmoji) {
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