package Simulator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Hotel {

    //lijst van alle ruimtes in hotel
    private ArrayList<HotelRuimte> ruimtes;
    private Gast gast;
    private Schoonmaker schoonmaker;

    public Hotel() {
        this.ruimtes = new ArrayList<>();
        this.gast = new Gast();
        this.schoonmaker = new Schoonmaker();
    }

    public ArrayList<HotelRuimte> getRuimtes() {
        return ruimtes;
    }

    public void maakHotelLayout(String layoutJson) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<JsonItem>>() {
        }.getType();
        List<JsonItem> items = gson.fromJson(layoutJson, listType);

        ruimtes.clear();

        for (JsonItem item : items) {
            String areaType = item.AreaType;

            String[] pos = item.Position.split(",");
            int x = Integer.parseInt(pos[0].trim());
            int y = Integer.parseInt(pos[1].trim());

            String[] dim = item.Dimension.split(",");
            int dimX = Integer.parseInt(dim[0].trim());
            int dimY = Integer.parseInt(dim[1].trim());

            // breedte = dimX * dimY (of wat jouw docent bedoelt met "keer elkaar")
            int breedte = dimX * dimY;

            int maxPersonen = item.Capacity != null
                    ? Integer.parseInt(item.Capacity.trim())
                    : 0;

            String sterrenAantal = item.Classification != null
                    ? item.Classification
                    : "";

            HotelRuimte r;

            switch (item.AreaType) {
                case "Cinema" -> r = new Bioscoop(areaType, sterrenAantal, y, x, breedte, maxPersonen);
                case "Fitness" -> r = new FitnessRuimtes(areaType, sterrenAantal, y, x, breedte, maxPersonen);
                case "Restaurant" -> r = new Restaurant(areaType, sterrenAantal, y, x, breedte, maxPersonen);
                case "Room" -> r = new HotelKamer(areaType, sterrenAantal, y, x, breedte, maxPersonen);
                default -> {
                    continue;
                }
            }

            ruimtes.add(r);
        }
    }

    private class JsonItem {
        String AreaType;
        String Position;
        String Dimension;
        String Capacity;
        String Classification;
    }
}
