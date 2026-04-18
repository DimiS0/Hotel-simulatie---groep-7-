package hotelsimulator.personen;

import com.sun.source.tree.BinaryTree;
import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.core.Hotel;
import hotelsimulator.pathfinding.Pathfinder;
import hotelsimulator.ruimtes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Gast extends Persoon {

    // Alle mogelijke toestanden van een gast — hij zit altijd in precies één toestand
    public enum Status {
        WACHT_OP_SPAWN,     // Nog niet actief, wacht op spawn
        LOOPT_NAAR_INGANG,  // Loopt via de gang naar de kamerdeur
        LOOPT_NAAR_SCHACHT, // Loopt naar de wachtplek bij de lift
        WACHT_OP_LIFT,      // Staat stil, wacht tot de lift aankomt
        IN_LIFT,            // Rijdt mee in de lift
        LOOP_DOOR_TRAP,
        LOOP_NAAR_TRAP,
        BETREEDT_KAMER,     // Loopt de kamer in (van deur naar midden)
        IN_KAMER,           // Zit in de kamer, wacht 5 seconden
        VERLAAT_KAMER       // Loopt de kamer uit terug naar de gang
    }

    private Status status = Status.WACHT_OP_SPAWN;
    private HotelRuimte doelKamer;
    private long verblijfEinde = 0;
    static final long VERBLIJF_MS = 5000; // 5 seconden in de kamer

    public static final int SPAWN_X = 200; // Startpositie in pixels (lobby)
    public static final int SPAWN_Y = 500;
    private static final int LIFT_CENTER_X   = 75;  // Horizontaal midden van de liftcel
    private static final int SCHACHT_PIXEL_X = 100; // Gangzijde naast de schacht
    private static final int TRAP_PIXEL_X = 425;

    private int doelVerdieping    = 8; // Verdieping waar de gast naartoe wil
    private int huidigeVerdieping = 8; // Verdieping waar de gast nu is (start altijd op 8)

    public Gast(Lift lift, Schacht schacht, Hotel hotel, HotelEventManager hotelEventManager, SimulatieConfig simulatieConfig) {
        super(SPAWN_X, SPAWN_Y, lift, schacht, hotel,hotelEventManager,simulatieConfig);
        this.simulatieConfig = simulatieConfig;
    }


    // Wordt elke frame (~60x per seconde) aangeroepen vanuit HotelGui
    // Voert het gedrag uit dat hoort bij de huidige toestand
    public void update() {
        switch (status) {

            case WACHT_OP_SPAWN:
                // Niets doen — activeer() wordt later aangeroepen
                break;

            case LOOPT_NAAR_INGANG:
                if (!pad.isEmpty()) {
                    beweeg(); // Loop stap voor stap naar de kamerdeur
                } else {
                    // Deur bereikt — controleer of het midden boven de ingang ligt (geldige kamer)
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    Point midden = getTrueMidden(doelKamer);
                    if (midden.y >= ingang.y) {
                        // Kamer is onbereikbaar of ongeldig — vrijgeven en opnieuw proberen
                        doelKamer.verlaat();
                        doelKamer = null;
                        kiesEnLoopNaarKamer();
                        return;
                    }
                    // Maak een pad van de deur naar het midden van de kamer
                    List<Point> instapPad = new ArrayList<>();
                    instapPad.add(new Point(midden.x, ingang.y));
                    instapPad.add(midden);
                    setPad(instapPad);
                    status = Status.BETREEDT_KAMER;
                }
                break;

            case LOOPT_NAAR_SCHACHT:
                if (!pad.isEmpty()) {
                    beweeg(); // Loop naar de wachtplek bij de lift
                } else {
                    // Aangekomen bij de lift — zet de gast in de wachtrij van de lift
                    hotel.getLift().voegWachtendeGastToe(this, huidigeVerdieping);
                    status = Status.WACHT_OP_LIFT;
                }
                break;

            case WACHT_OP_LIFT:
                // Staat stil — de Lift roept stapIn() aan zodra hij aankomt
                break;

            case IN_LIFT:
                if (pixelX != LIFT_CENTER_X) {
                    // Stap 1: loop horizontaal naar het midden van de liftcel
                    if (pixelX > LIFT_CENTER_X) pixelX = Math.max(pixelX - SNELHEID, LIFT_CENTER_X);
                    else                         pixelX = Math.min(pixelX + SNELHEID, LIFT_CENTER_X);
                } else {
                    // Stap 2: in het midden — volg de lift omhoog of omlaag
                    // +25 centreert de gast verticaal in het liftblokje (helft van 50px)
                    pixelY = (hotel.getLift().getCurrentLiftY() - 1) * 50 + 25;
                }
                break;

            case BETREEDT_KAMER:
                if (!pad.isEmpty()) {
                    beweeg(); // Loop van de deur naar het midden van de kamer
                } else {
                    // In het midden aangekomen — start de 5-seconden verblijftimer
                    status = Status.IN_KAMER;
                    verblijfEinde = System.currentTimeMillis() + (long)(doelKamer.getVerblijfMs() / simulatieConfig.getSnelheid().getFactor());
                }
                break;

            case IN_KAMER:
                // Wacht tot de verblijftijd voorbij is
                if (System.currentTimeMillis() >= verblijfEinde) {
                    // Maak een pad terug van midden naar de deur
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
                    beweeg(); // Loop van het midden terug naar de deur
                } else {
                    // Buiten de kamer — kamer vrijgeven en volgende kamer kiezen
                    Point ingang = Pathfinder.getKamerIngang(doelKamer);
                    pixelX = ingang.x;
                    pixelY = ingang.y;
                    doelKamer.verlaat(); // Verlaagt aantalGasten in de kamer
                    doelKamer = null;
                    kiesEnLoopNaarKamer();
                }
                break;
            case LOOP_NAAR_TRAP:
                if (!pad.isEmpty()) {
                    beweeg();
                }
                else {
                    pixelX = TRAP_PIXEL_X;
                    pixelYDouble = pixelY;
                    status = Status.LOOP_DOOR_TRAP;
                }
                break;


            case LOOP_DOOR_TRAP:
                int doelY = doelVerdieping  * 50;


                // Aangekomen op doelverdieping?
                if (Math.abs(this.pixelY - doelY) <= SNELHEID) {
                    setPositie(TRAP_PIXEL_X, doelY);

                    pixelX  = (TRAP_PIXEL_X / 50) * 50;
                    pixelXD = pixelX;
                    huidigeVerdieping = doelVerdieping;

                    // Loop naar de kamer
                    if (doelKamer != null) {
                        Point ingang = Pathfinder.getKamerIngang(doelKamer);
                        List<Point> padNaarKamer = Pathfinder.vindPad(
                                pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
                        if (!padNaarKamer.isEmpty()) {
                            setPad(padNaarKamer);
                            status = Status.LOOPT_NAAR_INGANG;
                            break;
                        }
                        doelKamer.verlaat();
                        doelKamer = null;
                    }
                    kiesEnLoopNaarKamer();
                } else {
                    // Loop omhoog of omlaag door de trap
                    if (doelY > this.pixelYDouble) {
                        this.pixelYDouble += (2.0*getFactor())/3;  // Omlaag
                    } else {
                        this.pixelYDouble -= (2.0*getFactor())/3;  // Omhoog
                    }
                    this.pixelY = (int)this.pixelYDouble;
                }
                break;

        }
    }


    // Kies willekeurig een beschikbare kamer en begin de route erheen
    // Als de kamer op een andere verdieping staat → lift gebruiken
    // Als de kamer op dezelfde verdieping staat → direct lopen via A*
    private void kiesEnLoopNaarKamer() {
        List<HotelRuimte> kamers = hotel.getKamers();
        if (kamers.isEmpty()) return;

        for (int poging = 0; poging < 15; poging++) {
            HotelRuimte kandidaat = kamers.get(random.nextInt(kamers.size()));

            // Sla volle kamers over
            if (kandidaat.isVol()) continue;

            Point ingang = Pathfinder.getKamerIngang(kandidaat);
            Point midden = getTrueMidden(kandidaat);

            // Sla ongeldige kamers over (midden moet boven de ingang liggen)
            if (midden.y >= ingang.y) continue;

            int kamerVerdieping = getNabijeStop(kandidaat.getY());

            if (kamerVerdieping == huidigeVerdieping) {
                // Zelfde verdieping: zoek direct een pad via A*
                List<Point> nieuwPad = Pathfinder.vindPad(
                        pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
                if (!nieuwPad.isEmpty()) {
                    // Registreer de gast als aanwezig in de kamer
                    kandidaat.betreedAlsGast(); // ← was: betreed()
                    doelKamer = kandidaat;
                    setPad(nieuwPad);
                    status = Status.LOOPT_NAAR_INGANG;
                    return;
                }
            } else {
                // Andere verdieping: lift gebruiken
                kandidaat.betreedAlsGast(); // ← was: betreed()
                doelKamer = kandidaat;
                doelVerdieping = kamerVerdieping;
                loopNaarSchachtOfTrap();
                if (status == Status.LOOPT_NAAR_SCHACHT) return;
                if (status == Status.LOOP_NAAR_TRAP) return;
                // Pad naar schacht niet gevonden — kamer vrijgeven en opnieuw proberen
                doelKamer.verlaat();
                doelKamer = null;
            }
        }
    }


    // Zoek een pad naar de wachtplek bij de lift op de huidige verdieping of stuur gast naar trapingang
    private void loopNaarSchachtOfTrap() {

        //kiest random of gasten lift/trap nemen
        int kiesTransport = random.nextInt(1,3);
        if(kiesTransport == 1){
            int wachtPixelY = (huidigeVerdieping - 1) * 50;
            List<Point> schachtPad = Pathfinder.vindPad(
                    pixelX, pixelY, SCHACHT_PIXEL_X, wachtPixelY, hotel.getRuimtes());
            if (!schachtPad.isEmpty()) {
                setPad(schachtPad);
                status = Status.LOOPT_NAAR_SCHACHT;
            }
        }
        if(kiesTransport == 2){
            int wachtPixelY = getNabijeStop(huidigeVerdieping) * 50; // dichtstbijzijnde ingang
            List<Point> trapPad = Pathfinder.vindPad(
                    pixelX, pixelY, TRAP_PIXEL_X, wachtPixelY, hotel.getRuimtes());
            if (!trapPad.isEmpty()) {
                setPad(trapPad);
                status = Status.LOOP_NAAR_TRAP;
            }
        }

    }


    // Geeft de dichtstbijzijnde lifthalte terug voor een gegeven grid-rij
    // Lifthaltes zijn op rij 8 (beneden), 5 (midden), 2 (boven)
    private int getNabijeStop(int gridY) {
        int[] stops = hotel.getLift().getVerdiepingenY();
        int dichtstbij = stops[0];
        for (int stop : stops) {
            if (Math.abs(gridY - stop) < Math.abs(gridY - dichtstbij))
                dichtstbij = stop;
        }
        return dichtstbij;
    }


    // Geeft het exacte pixel-midden van een kamer terug (niet afgerond op grid)
    private Point getTrueMidden(HotelRuimte kamer) {
        int middenX = (kamer.getX() + 1) * 50 + kamer.getBreedte() * 50 / 2;
        int middenY = (kamer.getY() - 1) * 50 + kamer.getHoogte()  * 50 / 2;
        return new Point(middenX, middenY);
    }


    // Aangeroepen door de Lift wanneer hij aankomt bij de wachtende gast
    public void stapIn() {
        status = Status.IN_LIFT;
    }


    // Aangeroepen door de Lift wanneer de gast zijn doelverdieping bereikt
    public void stapUit(int pixelYPos) {
        // setPositie() updates pixelX, pixelY, pixelXD én pixelYD tegelijk.
        // Zonder dit gebruikte beweeg() nog de oude double-positie (de instaapplek)
        // en liep de persoon door verdiepingen heen naar zijn bestemming.
        setPositie(SCHACHT_PIXEL_X, pixelYPos);
        huidigeVerdieping = doelVerdieping;

        // Zoek direct een pad naar de kamer op de nieuwe verdieping
        if (doelKamer != null) {
            Point ingang = Pathfinder.getKamerIngang(doelKamer);
            List<Point> padNaarKamer = Pathfinder.vindPad(
                    pixelX, pixelY, ingang.x, ingang.y, hotel.getRuimtes());
            if (!padNaarKamer.isEmpty()) {
                setPad(padNaarKamer);
                status = Status.LOOPT_NAAR_INGANG;
                return;
            }
            // Pad niet gevonden — kamer vrijgeven en opnieuw zoeken
            doelKamer.verlaat();
            doelKamer = null;
        }
        kiesEnLoopNaarKamer();
    }

    public int getDoelVerdieping() { return doelVerdieping; }


    // Zet de gast klaar op de startpositie en kies meteen een doelkamer
    public void activeer() {
        this.pixelX = SPAWN_X;
        this.pixelY = SPAWN_Y;
        this.pad.clear();
        this.doelKamer = null;
        this.huidigeVerdieping = 8;
        this.status = Status.WACHT_OP_SPAWN;
        kiesEnLoopNaarKamer();
    }

    // Geeft terug of de gast al actief is (niet meer in wachtstatus)
    public boolean isGespawnd() {
        return status != Status.WACHT_OP_SPAWN;
    }


    // Geeft de kamer terug waar de gast op dit moment in zit (of null als hij er niet in zit)
    @Override
    public HotelRuimte getHuidigeRuimte() {
        if (status == Status.IN_KAMER ||
                status == Status.BETREEDT_KAMER ||
                status == Status.VERLAAT_KAMER) return doelKamer;
        return null;
    }


    // Teken de gast als een gekleurde cirkel — kleur toont de huidige toestand
    @Override
    public void print(Graphics g) {
        // Teken niets zolang de gast nog niet gespawnd is
        if (status == Status.WACHT_OP_SPAWN) return;

        Color kleur = switch (status) {
            case WACHT_OP_LIFT      -> new Color(100, 255, 150);         // Oranje = wacht op lift
            case BETREEDT_KAMER,
                 VERLAAT_KAMER,
                 IN_KAMER           -> new Color(150, 255, 0);  // Blauw = loopt kamer in/uit
            default                 -> new Color(50, 255, 50);          // Blauw = loopt in de gang
        };

        // Vul de cirkel met de statuskleur
        g.setColor(kleur);
        g.fillOval(pixelX - 10, pixelY - 10, 20, 20);
        // Teken een zwarte rand om de cirkel
        g.setColor(Color.BLACK);
        g.drawOval(pixelX - 10, pixelY - 10, 20, 20);
        // Teken de letter G in het midden
        g.setColor(Color.WHITE);
        g.drawString("G", pixelX - 4, pixelY + 5);

        if (doelKamer != null && (status == Status.BETREEDT_KAMER
                || status == Status.IN_KAMER
                || status == Status.VERLAAT_KAMER)) {
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
