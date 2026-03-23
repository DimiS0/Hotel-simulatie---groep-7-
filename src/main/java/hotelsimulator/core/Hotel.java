package hotelsimulator.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hotelsimulator.personen.Gast;
import hotelsimulator.personen.Schoonmaker;
import hotelsimulator.ruimtes.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Hotel {
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

			int maxPersonen = (item.Capacity != null) ? Integer.parseInt(item.Capacity.trim()) : 0;
			String sterrenAantal = (item.Classification != null) ? item.Classification : "";

			HotelRuimte r = switch (item.AreaType) {
				case "Cinema" -> new Bioscoop(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Fitness" -> new FitnessRuimtes(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Restaurant" -> new Restaurant(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Room" -> new HotelKamer(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Lift" -> new Lift(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Trap" -> new Trap(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				case "Lobby" -> new Lobby(areaType, sterrenAantal, y, x, dimX, dimY, maxPersonen);
				default -> null;
			};

			if (r != null)
				ruimtes.add(r);
		}
	}

    public void genereerLayout() {
        int maxX = 10;
        int maxY = 10;
        char[][] grid = new char[maxY][maxX];

        //vullen met .
        for (int rij = 0; rij < maxY; rij++) {
            for (int kolom = 0; kolom < maxX; kolom++) {
                grid[rij][kolom] = '.';
            }
        }

        //kamers vullen
        for (HotelRuimte r : getRuimtes()) {
            int startRij = r.getY() - 1;     // Y
            int startKolom = r.getX() - 1;   // X

            char type = switch (r.getAreaType()) {
                case "Room" -> 'K';
                case "Restaurant" -> 'R';
                case "Fitness" -> 'F';
                case "Cinema" -> 'B';
                case "Lift" -> 'L';
                case "Trap" -> 'T';
                default -> '.';
            };

            for (int l = 0; l < r.getHoogte(); l++) {
                for (int k = 0; k < r.getBreedte(); k++) {
                    int rij = startRij + l;
                    int kolom = startKolom + k;

                    if (rij >= 0 && rij < maxY && kolom >= 0 && kolom < maxX) {
                        grid[rij][kolom] = type;
                    }
                }
            }
        }


        for (int rij = 0; rij < maxY; rij++) {
            for (int kolom = 0; kolom < maxX; kolom++) {
                System.out.print(grid[rij][kolom] + " ");
            }
            System.out.println();
        }
    }

	private class JsonItem {
		String AreaType, Position, Dimension, Capacity, Classification;
	}
}