package hotelsimulator.pathfinding;

import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.HotelKamer;
import hotelsimulator.ruimtes.Bioscoop;
import hotelsimulator.ruimtes.FitnessRuimtes;
import hotelsimulator.ruimtes.Restaurant;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/*
  Zoekt de kortste looproute van A naar B met het A*-algoritme.
  Het hotel wordt gezien als een dynamisch grid van vakjes (elk 50x50 pixels).
  Werkt met verschillende hotel layouts.
 */
public class Pathfinder {

    // Het hotel is dynamisch, elk vakje is 50 pixels breed/hoog
    public static final int PIXELS_PER_VAKJE = 50;



    // vindPad: zoekt de kortste route en geeft die terug als pixelpunten

    public static List<Point> vindPad(int startPixelX, int startPixelY,
                                      int eindPixelX,  int eindPixelY,
                                      List<HotelRuimte> alleRuimtes, Hotel hotel) {

        int aantalVakjes = Math.max(hotel.getMaxBreedte(), hotel.getMaxHoogte()) + 3;
        int arrayGrootte = aantalVakjes + 1;

        // Pixels omzetten naar grid-vakjes (pixel 200 / 50 = vakje 4)
        int startKolom = pixelNaarGrid(startPixelX, aantalVakjes);
        int startRij   = pixelNaarGrid(startPixelY, aantalVakjes);
        int eindKolom  = pixelNaarGrid(eindPixelX, aantalVakjes);
        int eindRij    = pixelNaarGrid(eindPixelY, aantalVakjes);

        boolean[][] loopbareVakjes = berekenLoopbareKaart(alleRuimtes, hotel);

        boolean startIsGeblokkeerd = !loopbareVakjes[startKolom][startRij];
        boolean eindIsGeblokkeerd  = !loopbareVakjes[eindKolom][eindRij];

        if (startIsGeblokkeerd || eindIsGeblokkeerd) {
            System.out.println("Start of einde zit in een geblokkeerd vakje!");
            return new ArrayList<>();
        }

        // aantalStappenNaarVakje = hoeveel stappen kostte het om hier te komen (gScore)
        // geschatTotaal = stappen + schatting naar doel (fScore), A* pakt laagste eerst
        int[][] aantalStappenNaarVakje = new int[arrayGrootte][arrayGrootte];
        int[][] geschatTotaal          = new int[arrayGrootte][arrayGrootte];

        for (int[] rij : aantalStappenNaarVakje) {
            Arrays.fill(rij, Integer.MAX_VALUE);
        }
        for (int[] rij : geschatTotaal) {
            Arrays.fill(rij, Integer.MAX_VALUE);
        }

        // ouderVakje onthoudt via welk vakje we ergens kwamen (voor padreconstructie)
        Point[][] ouderVakje  = new Point[arrayGrootte][arrayGrootte];
        boolean[][] alVerwerkt = new boolean[arrayGrootte][arrayGrootte];

        int afstandStartNaarDoel = berekenManhattanAfstand(startKolom, startRij, eindKolom, eindRij);

        aantalStappenNaarVakje[startKolom][startRij] = 0;
        geschatTotaal[startKolom][startRij]          = afstandStartNaarDoel;

        // PriorityQueue sorteert automatisch: het meest veelbelovende vakje staat vooraan
        PriorityQueue<Point> teVerkennen = new PriorityQueue<>(
                (vakjeA, vakjeB) -> {
                    int kostenA = geschatTotaal[vakjeA.x][vakjeA.y];
                    int kostenB = geschatTotaal[vakjeB.x][vakjeB.y];
                    return kostenA - kostenB;
                }
        );
        teVerkennen.add(new Point(startKolom, startRij));

        // Vier looprichtingen: geen diagonaal
        int[][] richtingen = {
                {0,  1},
                {0, -1},
                {1,  0},
                {-1, 0}
        };

        while (!teVerkennen.isEmpty()) {
            Point huidig  = teVerkennen.poll();
            int huidigX   = huidig.x;
            int huidigY   = huidig.y;

            boolean doelBereikt = huidigX == eindKolom && huidigY == eindRij;
            if (doelBereikt) {
                return bouwPadTerug(ouderVakje, huidig);
            }

            if (alVerwerkt[huidigX][huidigY]) {
                continue;
            }
            alVerwerkt[huidigX][huidigY] = true;

            for (int[] richting : richtingen) {
                int buurKolom = huidigX + richting[0];
                int buurRij   = huidigY + richting[1];

                boolean buurValtBuitenGrid = buurKolom < 0 || buurKolom > aantalVakjes
                        || buurRij   < 0 || buurRij   > aantalVakjes;
                if (buurValtBuitenGrid) {
                    continue;
                }

                boolean buurIsGeblokkeerd = !loopbareVakjes[buurKolom][buurRij];
                if (buurIsGeblokkeerd) {
                    continue;
                }

                boolean buurAlVerwerkt = alVerwerkt[buurKolom][buurRij];
                if (buurAlVerwerkt) {
                    continue;
                }

                boolean stapIsOngeldig = !isStapGeldig(huidigX, huidigY, buurKolom, buurRij, alleRuimtes);
                if (stapIsOngeldig) {
                    continue;
                }

                int huidigAantalStappen = aantalStappenNaarVakje[huidigX][huidigY];
                int nieuwAantalStappen  = huidigAantalStappen + 1;

                int oudeAantalStappenNaarBuur = aantalStappenNaarVakje[buurKolom][buurRij];
                boolean betereRouteGevonden   = nieuwAantalStappen < oudeAantalStappenNaarBuur;

                // Betere route gevonden naar deze buur? Dan opslaan en toevoegen
                if (betereRouteGevonden) {
                    int afstandBuurNaarDoel = berekenManhattanAfstand(buurKolom, buurRij, eindKolom, eindRij);
                    int nieuwGeschatTotaal  = nieuwAantalStappen + afstandBuurNaarDoel;

                    ouderVakje[buurKolom][buurRij]             = huidig;
                    aantalStappenNaarVakje[buurKolom][buurRij]  = nieuwAantalStappen;
                    geschatTotaal[buurKolom][buurRij]           = nieuwGeschatTotaal;

                    teVerkennen.add(new Point(buurKolom, buurRij));
                }
            }
        }

        System.out.println("Geen pad gevonden!");
        return new ArrayList<>();
    }



    // berekenLoopbareKaart: true = mag lopen, false = geblokkeerd (kamerinterieur)
    public static boolean[][] berekenLoopbareKaart(List<HotelRuimte> alleRuimtes, Hotel hotel) {
        int aantalVakjes = Math.max(hotel.getMaxBreedte(), hotel.getMaxHoogte()) + 3;
        int arrayGrootte = aantalVakjes + 1;

        boolean[][] loopbareKaart = new boolean[arrayGrootte][arrayGrootte];
        for (boolean[] rij : loopbareKaart) {
            Arrays.fill(rij, true);
        }

        for (HotelRuimte ruimte : alleRuimtes) {

            boolean isHotelKamer    = ruimte instanceof HotelKamer;
            boolean isBioscoop      = ruimte instanceof Bioscoop;
            boolean isFitnessRuimte = ruimte instanceof FitnessRuimtes;
            boolean isRestaurant    = ruimte instanceof Restaurant;
            boolean isEenKamer      = isHotelKamer || isBioscoop || isFitnessRuimte || isRestaurant;

            if (isEenKamer) {
                // Blokkeer het interieur van de kamer (+1 zodat de rand vrij blijft)
                int interiorLinks  = ruimte.getX() + 2;
                int interiorRechts = ruimte.getX() + 1 + ruimte.getBreedte();
                int interiorBoven  = ruimte.getY();
                int interiorOnder  = ruimte.getY() - 1 + ruimte.getHoogte();

                for (int kolom = interiorLinks; kolom < interiorRechts; kolom++) {
                    for (int rij = interiorBoven; rij < interiorOnder; rij++) {
                        boolean kolomBinnenGrid = kolom >= 0 && kolom < arrayGrootte;
                        boolean rijBinnenGrid   = rij   >= 0 && rij   < arrayGrootte;

                        if (kolomBinnenGrid && rijBinnenGrid) {
                            loopbareKaart[kolom][rij] = false;
                        }
                    }
                }
            }

            // Ruimtes met ingangen (zoals Trap): blokkeer kolom, open alleen de ingangen
            int[] ingangen         = ruimte.getIngangen();
            boolean heeftIngangen  = ingangen.length > 0;

            if (heeftIngangen) {
                int ruimteKolom = ruimte.getX() + 1;

                for (int rij = 0; rij <= aantalVakjes; rij++) {
                    loopbareKaart[ruimteKolom][rij] = false;
                }
                for (int ingang : ingangen) {
                    loopbareKaart[ruimteKolom][ingang] = true;
                }
            }
        }

        return loopbareKaart;
    }



    // isStapGeldig: extra check zodat personen niet door dunne kamers lopen
    private static boolean isStapGeldig(int vanKolom, int vanRij,
                                        int naarKolom, int naarRij,
                                        List<HotelRuimte> alleRuimtes) {
        for (HotelRuimte ruimte : alleRuimtes) {

            // Trap: alleen betreden via officiële ingang
            int[] ingangen        = ruimte.getIngangen();
            boolean heeftIngangen = ingangen.length > 0;

            if (heeftIngangen) {
                int     ruimteKolom        = ruimte.getX() + 1;
                boolean stapNaarDezeKolom  = naarKolom == ruimteKolom;
                boolean komtVanBuiten      = vanKolom  != ruimteKolom;
                boolean probeertInTeStappen = stapNaarDezeKolom && komtVanBuiten;

                boolean isGeenGeldigeIngang = !ruimte.isBeloopbaar(naarKolom, naarRij);

                if (probeertInTeStappen && isGeenGeldigeIngang) {
                    return false;
                }
                continue;
            }

            boolean isHotelKamer    = ruimte instanceof HotelKamer;
            boolean isBioscoop      = ruimte instanceof Bioscoop;
            boolean isFitnessRuimte = ruimte instanceof FitnessRuimtes;
            boolean isRestaurant    = ruimte instanceof Restaurant;
            boolean isEenKamer      = isHotelKamer || isBioscoop || isFitnessRuimte || isRestaurant;

            if (!isEenKamer) {
                continue;
            }

            int kamerLinks  = ruimte.getX() + 1;
            int kamerRechts = ruimte.getX() + 1 + ruimte.getBreedte();
            int kamerBoven  = ruimte.getY() - 1;
            int kamerOnder  = ruimte.getY() - 1 + ruimte.getHoogte();

            boolean isHorizontaleStap = vanRij == naarRij;

            if (isHorizontaleStap) {
                // Horizontale stap: gaat hij dwars door het kameroppervlak?
                int     linkerVakje       = Math.min(vanKolom, naarKolom);
                boolean rijBinnenKamer    = vanRij      > kamerBoven  && vanRij      < kamerOnder;
                boolean kolomBinnenKamer  = linkerVakje >= kamerLinks && linkerVakje < kamerRechts;

                if (rijBinnenKamer && kolomBinnenKamer) {
                    return false;
                }
            } else {
                // Verticale stap: gaat hij dwars door het kameroppervlak?
                int     bovensteVakje     = Math.min(vanRij, naarRij);
                int ondersteVakje    = Math.max(vanRij, naarRij);
                boolean kolomBinnenKamer2 = vanKolom      > kamerLinks && vanKolom      < kamerRechts;
                boolean rijBinnenKamer2   = bovensteVakje >= kamerBoven && bovensteVakje < kamerOnder;
                boolean eindtBinnenKamer  = ondersteVakje > kamerBoven && ondersteVakje <= kamerOnder;


                if (kolomBinnenKamer2 && (rijBinnenKamer2 || eindtBinnenKamer)) {
                    return false;
                }
            }
        }

        return true;
    }



    // bouwPadTerug: volgt de ouder-ketting terug van doel naar start
    private static List<Point> bouwPadTerug(Point[][] ouderVakje, Point doelVakje) {
        LinkedList<Point> pad = new LinkedList<>();
        Point huidig = doelVakje;

        // Elke stap terug in de ketting, vooraan toevoegen geeft start→doel volgorde
        while (huidig != null) {
            int pixelX = huidig.x * PIXELS_PER_VAKJE;
            int pixelY = huidig.y * PIXELS_PER_VAKJE;

            pad.addFirst(new Point(pixelX, pixelY));
            huidig = ouderVakje[huidig.x][huidig.y];
        }

        return pad;
    }



    // berekenManhattanAfstand: horizontale + verticale afstand (geen diagonaal)
    private static int berekenManhattanAfstand(int kolom1, int rij1, int kolom2, int rij2) {
        int horizontaleAfstand = Math.abs(kolom1 - kolom2);
        int verticaleAfstand   = Math.abs(rij1   - rij2);
        return horizontaleAfstand + verticaleAfstand;
    }



    // getKamerIngang: personen betreden een kamer altijd via het midden onderaan
    public static Point getKamerIngang(HotelRuimte kamer) {
        int kamerLinksInPixels   = (kamer.getX() + 1) * PIXELS_PER_VAKJE;
        int kamerBreedteInPixels = kamer.getBreedte() * PIXELS_PER_VAKJE;
        int kamerMiddenInPixels  = kamerLinksInPixels + kamerBreedteInPixels / 2;

        int ingangX = kamerMiddenInPixels / PIXELS_PER_VAKJE * PIXELS_PER_VAKJE;
        int ingangY = (kamer.getY() - 1 + kamer.getHoogte()) * PIXELS_PER_VAKJE;

        return new Point(ingangX, ingangY);
    }



    // pixelNaarGrid: pixel → grid-index, vastgezet tussen 0 en maxVakjes
    private static int pixelNaarGrid(int pixels, int maxVakjes) {
        int index = pixels / PIXELS_PER_VAKJE;

        if (index < 0)          index = 0;
        if (index > maxVakjes)  index = maxVakjes;

        return index;
    }
}