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
		//gson object aanmaken
		Gson gson = new Gson();

		//Onthoudt dat de lijst JsonItem-objecten bevat, anders vergeet Java het type
		Type listType = new TypeToken<List<JsonItem>>() {}.getType();

		//zet de JSON tekst om naar een lijst van json objecten
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

			int maxPersonen = (item.Capacity != null) ? Integer.parseInt(item.Capacity.trim()) : 0;
			String sterrenAantal = (item.Classification != null) ? item.Classification : "";

			//elke item maakt een nieuwe object aan met die specificaties
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
				//onthoudt waar de genoemde kamer is in de arraylijst ruimtes
				ruimtes.add(r);
		}
	}

	private class JsonItem {
		String AreaType, Position, Dimension, Capacity, Classification;
	}
}