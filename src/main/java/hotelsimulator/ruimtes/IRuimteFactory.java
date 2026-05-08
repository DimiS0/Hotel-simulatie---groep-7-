package hotelsimulator.ruimtes;

public interface IRuimteFactory {

     // Maakt een HotelRuimte aan voor het gegeven type en afmetingen.
    HotelRuimte maak(String type, int sterrenAantal,
                     int y, int x, int breedte, int hoogte, int maxPersonen);
}
