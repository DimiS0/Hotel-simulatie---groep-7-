package hotelsimulator.ruimtes;

public class HotelRuimteFactory implements IRuimteFactory  {
@Override
    public HotelRuimte maak(String type, String sterrenAantal,
                            int y, int x, int breedte, int hoogte, int maxPersonen) {
        return switch (type) {
            case "Cinema"     -> new Bioscoop(type, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
            case "Fitness"    -> new FitnessRuimtes(type, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
            case "Restaurant" -> new Restaurant(type, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
            case "Room"       -> new HotelKamer(type, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
            default           -> throw new IllegalArgumentException("Onbekend ruimtetype: " + type);
        };
    }
}
