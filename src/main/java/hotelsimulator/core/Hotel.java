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

    public boolean heeftSchoonmaakWerk() {
        return !schoonmaakWachtrij.isEmpty();
    }

    public LinkedList<Integer> getLiftOproepen() {
        return liftOproepen;
    }

    public void maakHotelLayout(String layoutJson) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonItem>>() {
        }.getType();
        List<JsonItem> items = gson.fromJson(layoutJson, listType);

        //Lijst leegmaken zodat oude data niet overblijft bij hergebruik
        ruimtes.clear();

        //loop door de lijst elke item krijgt een AreaType, een positie een breedte en hoogte, en een cappaciteit en sterren
        for (JsonItem item : items) {
            String areaType = item.AreaType;

            String[] pos = item.Position.split(",");
            int x = Integer.parseInt(pos[0].trim());
            int y = Integer.parseInt(pos[1].trim());

            String[] dim = item.Dimension.split(",");
            int dimX = Integer.parseInt(dim[0].trim());
            int dimY = Integer.parseInt(dim[1].trim());

            // waarom tenary if? Omdat het een kortere if - else is
            int maxPersonen = (item.Capacity != null) ? Integer.parseInt(item.Capacity.trim()) : 0;
            int sterrenAantal = (item.Classification != null && !item.Classification.isBlank()) ? Integer.parseInt(item.Classification.trim().split("\\s+")[0]) : 0;

            //elke item maakt een nieuwe object aan met die specificaties
            try {
                HotelRuimte r = ruimteFactory.maak(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
                ruimtes.add(r);
            } catch (IllegalArgumentException e) {
                System.out.println("Onbekend ruimtetype overgeslagen: " + areaType);
            }
        }
        //deze zelf aanmaken, want zitten niet in json
        schacht = new Schacht("Schacht", 0, 0, 0, 1, 9, 0);
        Lobby lobby = new Lobby("Lobby", 0, 0, 1, 6, 1, 0);
        Trap trap = new Trap("trap", 0, 0, 7, 1, 9, 999);
        lift = new Lift("Lift", 0, 0, 0, 1, 1, 5);

        //toevoegen aan arraylist
        ruimtes.add(schacht);
        ruimtes.add(lobby);
        ruimtes.add(trap);
        ruimtes.add(lift);

    }

    public ArrayList<HotelRuimte> getRuimtes() {
        return ruimtes;
    }

    public Lift getLift() {
        return lift;
    }

    private class JsonItem {
        String AreaType, Position, Dimension, Capacity, Classification;
    }

    public void maakPersonen(int aantalGasten) {
        //mensen opslaan
        personen = new ArrayList<>();

        //schoonmakers
        int aantalSchoonmakers = 2;


        int gastIndex = 0;
        int schoonmakerIndex = 0;

        //de gasten en schoonmakers spawnen doorelkaar gemixed
        while (gastIndex < aantalGasten || schoonmakerIndex < aantalSchoonmakers) {

            // Voeg een groep van maximaal 5 gasten toe
            for (int i = 0; i < 5 && gastIndex < aantalGasten; i++, gastIndex++) {
                personen.add(new Gast(lift, schacht, this, hotelEventManager, simulatieConfig, gastIndex + 1));
            }

            // Voeg daarna maximaal 2 schoonmakers toe
            for (int i = 0; i < 2 && schoonmakerIndex < aantalSchoonmakers; i++, schoonmakerIndex++) {
                personen.add(new Schoonmaker(lift, schacht, this, hotelEventManager, simulatieConfig));
            }
        }
    }
    //waar gasten op mogen lopen in dit geval dus niet in lobby
    public List<HotelRuimte> getKamers() {
        List<HotelRuimte> result = new ArrayList<>();
        for (HotelRuimte r : ruimtes) {
            if (r instanceof HotelKamer || r instanceof Restaurant ||
                    r instanceof Bioscoop || r instanceof FitnessRuimtes) {
                result.add(r);
            }
        }
        return result;
    }

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
        //zoeken naar een vrije kamer met de minimale aantal sterren
        for(int sterren = minimalesterren; sterren <= 5; sterren++) {
            for (HotelRuimte ruimte : getRuimtes()) {
                if (ruimte instanceof HotelKamer kamer
                        && !kamer.isVol()
                        && kamer.getSterrenAantal() == sterren
                        && !kamer.isGereserveerd()) {

                    //als er een kamer gevonden is dan reserveren we hem, want we willen niet dat andere gasten erin kunnen
                    kamer.reserveer();
                    return kamer;
                }
            }
        }
        //if overgeslagen dan geen kamer beschikbaar of bestaat niet?
        return null;
    }
    //verwijderd persoon van de lijst
    public void verwijderPersoon(Persoon persoon) {
        personen.remove(persoon);
    }

}
