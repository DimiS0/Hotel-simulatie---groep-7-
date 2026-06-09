package hotelsimulator.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelevents.HotelEventManager;
import hotelsimulator.config.SimulatieConfig;
import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Persoon;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.*;
import hotelsimulator.ruimtes.HotelRuimteFactory;
import hotelsimulator.ruimtes.IRuimteFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Hotel {
    private HotelEventManager hotelEventManager;
    private SimulatieConfig simulatieConfig;
    private ArrayList<HotelRuimte> ruimtes;
    private LinkedList<Integer> liftOproepen = new LinkedList<Integer>();
    private ArrayList<Persoon> personen;
    private Lift lift;
    private Schacht schacht;
    private IRuimteFactory ruimteFactory;
    private final LinkedList<HotelKamer> schoonmaakWachtrij = new LinkedList<>();
    private int maxHoogte = 0;
    private int maxBreedte = 0;
    private int verdiepingen = 0;
    private int lobbyBreedte = 0;
    private Trap trap;

    //waarom? test
    public Hotel(SimulatieConfig config, HotelEventManager eventManager, SimulatieConfig simulatieConfig) {
        this(config, eventManager, simulatieConfig, new HotelRuimteFactory());
    }

    public Hotel(SimulatieConfig config, HotelEventManager eventManager,
                 SimulatieConfig simulatieConfig, IRuimteFactory factory) {
        this.hotelEventManager = eventManager;
        this.simulatieConfig = simulatieConfig;
        this.ruimteFactory = factory;
        this.ruimtes = new ArrayList<>();
        this.personen = new ArrayList<>();
    }
    public synchronized void voegToeAanSchoonmaakWachtrij(HotelKamer kamer) {
        if (!schoonmaakWachtrij.contains(kamer)) {
            schoonmaakWachtrij.addLast(kamer);
        }
    }
    public synchronized HotelKamer pakVolgendeSchoonmaakKamer() {
        return schoonmaakWachtrij.isEmpty() ? null : schoonmaakWachtrij.removeFirst();
    }

    //getter van de lijst van lift oproepen
    public LinkedList<Integer> getLiftOproepen() {
        return liftOproepen;
    }

    public void maakHotelLayout(String layoutJson) {
        //Gson-object dat de JSON tekst kan omzetten naar Java objecten
        Gson gson = new Gson();

        //Gson vertellen dat layoutJson een lijst van JsonItem-objecten bevat
        //Zonder deze TypeToken weet Gson niet dat het om een lijst van JsonItem gaat
        Type listType = new TypeToken<List<JsonItem>>() {}.getType();

        // Zet de JSON-layout (String) om naar een lijst met JsonItem objecten
        List<JsonItem> items = gson.fromJson(layoutJson, listType);

        //Lijst leegmaken zodat oude data niet overblijft bij hergebruik
        ruimtes.clear();

        //loop door de lijst elke item krijgt een AreaType, een positie een breedte en hoogte, en een cappaciteit en sterren
        // voor maxPersonen en SterrenAantal gebruiken we een if else maar dan korter geschreven (tenary)
        for (JsonItem item : items) {
            String areaType = item.AreaType;

            // Positie (x,y) opsplitsen en omzetten naar ints
            String[] pos = item.Position.split(",");
            int x = Integer.parseInt(pos[0].trim());
            int y = Integer.parseInt(pos[1].trim());

            // Afmetingen (breedte,hoogte) opsplitsen en omzetten naar ints
            String[] dim = item.Dimension.split(",");
            int dimX = Integer.parseInt(dim[0].trim());
            int dimY = Integer.parseInt(dim[1].trim());

            // Lees capaciteit uit, als die ontbreekt gebruiken we 0
            int maxPersonen = (item.Capacity != null) ? Integer.parseInt(item.Capacity.trim()) : 0;

            // Lees sterren uit, neem het eerste getal uit de classificatie-string, anders 0
            int sterrenAantal = (item.Classification != null && !item.Classification.isBlank()) ? Integer.parseInt(item.Classification.trim().split("\\s+")[0]) : 0;

            // Voor elke JSON item  een nieuwe object aan met al de vorige specificaties
            try {
                //factory maakt de juiste ruimte
                HotelRuimte r = ruimteFactory.maak(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);

                // voeg de aangemaakte ruimtes toe aan in de lijst
                ruimtes.add(r);
            } catch (IllegalArgumentException e) {

                // onbekende ruimtes slaan we over en we loggen het
                System.out.println("Onbekend ruimtetype overgeslagen: " + areaType);
            }
        }

        //forloop om de maximale hoogte te achterhalen
        for (HotelRuimte ruimte : ruimtes) {
            int top = ruimte.getOrgineleY() + ruimte.getHoogte();
            if (top > maxHoogte) maxHoogte = top;
        }
        //forloop om de maximale Breedte te achterhalen
        for (HotelRuimte ruimte : ruimtes) {
            int rechts = ruimte.getX() + ruimte.getBreedte();
            if (rechts > maxBreedte) maxBreedte = rechts;
        }

        // Herbereken Y voor alle kamers uit JSON zodat we elke layout kunnen gebruiken
        for (HotelRuimte ruimte : ruimtes) {
            ruimte.herberekenY(maxHoogte);
        }

        kiesVerdiepingen();

        //schacht object aanmaken staat niet in JSON komt altijd links van het hotel
        schacht = new Schacht("Schacht", 0, 0, 0, 1, maxHoogte, 0);

        //schacht herberekenen zodat het op alle layouts past
        schacht.herberekenY(maxHoogte);

        //Lobby object aanmaken staat niet in JSON komt altijd onder het hotel
        Lobby lobby = new Lobby("Lobby", 0, 0, 1, maxBreedte, 1, 0);

        //lobby herberekenen zodat het op alle layouts past
        lobby.herberekenY(maxHoogte);

        //Trap object aanmaken staat niet in JSON komt altijd rechts van het hotel
        trap = new Trap("trap", 0, 0, maxBreedte, 1, maxHoogte, 999, getverdiepingen());

        //Trap herberekenen zodat het op alle layouts past
        trap.herberekenY(maxHoogte);

        //lift aanmaken, geen herbereken methode nodig positie wordt bepaald
        lift = new Lift("Lift", 0, maxHoogte + 1, 0, 1, 1, 5, getverdiepingen());

        //alle ruimtes die we net hebben gemaakt toevoegen aan de lijst
        ruimtes.add(schacht);
        ruimtes.add(lobby);
        ruimtes.add(trap);
        ruimtes.add(lift);
    }

    public void kiesVerdiepingen(){
        int x = maxHoogte % 3;
        int p = maxHoogte - x;
        verdiepingen = p / 3;
    }
    //max breedte opvragen
    public int getMaxBreedte(){
        return maxBreedte;
    }

    //max hoogte opvragen
    public int getMaxHoogte(){
        return maxHoogte;
    }

    //verdiepingen opvragen
    public int getverdiepingen(){
        return verdiepingen;
    }

    //de lijst ophalen met de ruimtes
    public ArrayList<HotelRuimte> getRuimtes() {
        return ruimtes;
    }

    //lift ophalen
    public Lift getLift() {
        return lift;
    }

    // Hulpklasse die één item uit het bestand voorstelt.
    // Dit is  geen echte ruimte in het hotel, alleen data.
    public Trap getTrap(){
        return trap;
    }

    private class JsonItem {
        String AreaType, Position, Dimension, Capacity, Classification;
    }

    public void maakPersonen(int aantalGasten) {
        //personen die gemaakt worden opslaan
        personen = new ArrayList<>();

        //schoonmakers max aantal
        int aantalSchoonmakers = 2;

        //de huidige aantal gemaakte gasten/schoonmakers
        int gastIndex = 0;
        int schoonmakerIndex = 0;

        //de gasten en schoonmakers spawnen doorelkaar gemixed
        while (gastIndex < aantalGasten || schoonmakerIndex < aantalSchoonmakers) {

            // Voeg een groep van maximaal 5 gasten toe
            for (int i = 0; i < 5 && gastIndex < aantalGasten; i++, gastIndex++) {
                personen.add(new Gast(lift, schacht, this, hotelEventManager, simulatieConfig, gastIndex + 1, getMaxBreedte(),getMaxHoogte()));
            }

            // Voeg daarna maximaal 2 schoonmakers toe
            for (int i = 0; i < 2 && schoonmakerIndex < aantalSchoonmakers; i++, schoonmakerIndex++) {
                personen.add(new Schoonmaker(lift, schacht, this, hotelEventManager, simulatieConfig, getMaxBreedte(),getMaxHoogte()));
            }
        }
    }

    // Array van personen ophalen
    public ArrayList<Persoon> getPersonen() {
        return personen;
    }

    //gasten opzoeken als er een event wordt afgevuurd, anders niet
    public Gast zoekGastOpId(int guestId) {
        for (Persoon persoon : personen) {
            if (persoon instanceof Gast gast && gast.getGuestID() == guestId) {
                System.out.println(" GAST GEVONDEN " + guestId);
                return gast;
            }
        }
        return null;
    }

    // Zoekt naar een vrije kamer voor de gast
    public HotelKamer zoekVrijeHotelKamer(int minimalesterren) {

        // for loop  we lopen door de minimale sterren tot en met maimale sterren
        //als we geen kamer vinden voor gast met gewenste sterren geven we hem een bettere kamer
        for (int sterren = minimalesterren; sterren <= 5; sterren++) {
            List<HotelKamer> kandidaten = new ArrayList<>();
            for (HotelRuimte ruimte : getRuimtes()) {

                //kijkt naar alleen hotelkamers die niet vol zijn, niet gereserveerd en er geen cleaning emergency is
                if (ruimte instanceof HotelKamer kamer
                        && !kamer.isVol()
                        && kamer.getSterrenAantal() == sterren
                        && !kamer.isGereserveerd()
                        && !kamer.isCleaningEmergency()) {

                    //als er een kamer gevonden is dan voegen we hem toe in kanditaten arraylist
                    kandidaten.add(kamer);
                }
            }
            //als de array gevuld is
            if (!kandidaten.isEmpty()) {

                // random kamer kiezen uit die arraylist
                HotelKamer gekozen = kandidaten.get(new java.util.Random().nextInt(kandidaten.size()));
                //en reserveer hem
                gekozen.reserveer();
                return gekozen;
            }
        }
        //geen kamer gevonden? return null
        return null;
    }
    public int getLobbyVerdieping() {
        return maxHoogte;
    }
    public synchronized void voegToeAanSchoonmaakWachtrijVooraan(HotelKamer kamer) {
        if (!schoonmaakWachtrij.contains(kamer)) {
            schoonmaakWachtrij.addFirst(kamer);
        }
    }

    //methode om de hotel te reseten
    public void reset(){
        ruimtes.clear();
        personen.clear();
        liftOproepen.clear();
        schoonmaakWachtrij.clear();
        maxHoogte = 0;
        maxBreedte = 0;
        verdiepingen = 0;
        lift = null;
        schacht = null;
    }
}
