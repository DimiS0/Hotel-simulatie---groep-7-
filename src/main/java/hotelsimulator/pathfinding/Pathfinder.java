package hotelsimulator.pathfinding;

import hotelsimulator.ruimtes.*;
import java.awt.Point;
import java.util.*;

public class Pathfinder {

    // Het hotel is 10x10 vakjes, elk vakje = 50 pixels op het scherm
    public static final int GRID_GROOTTE = 10;
    public static final int CEL_GROOTTE  = 50;


    // Vind een looproute van startpunt naar eindpunt (in pixels)
    // Geeft een lijst van tussenpunten terug die de gast één voor één afloopt
    public static List<Point> vindPad(int startPixelX, int startPixelY,
                                      int eindPixelX,  int eindPixelY,
                                      List<HotelRuimte> ruimtes) {

        // Pixels → rastervakjes (pixel 200 / 50 = vakje 4)
        int vanGx  = klem(startPixelX / CEL_GROOTTE, 0, GRID_GROOTTE);
        int vanGy  = klem(startPixelY / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarGx = klem(eindPixelX  / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarGy = klem(eindPixelY  / CEL_GROOTTE, 0, GRID_GROOTTE);

        // Kaart van het hotel: welke vakjes mag de gast betreden?
        boolean[][] loopbaar = berekenLoopbaar(ruimtes);

        // Als start of einde in een kamer zit, is er geen route mogelijk
        if (!loopbaar[vanGx][vanGy] || !loopbaar[naarGx][naarGy]) {
            System.out.println("Start of einde zit in een kamer!");
            return new ArrayList<>();
        }

        int n = GRID_GROOTTE + 1;

        // gScore = hoeveel stappen vanaf de start om dit vakje te bereiken
        // fScore = gScore + schatting naar het doel (bepaalt welk vakje we als eerste bekijken)
        // ouder  = welk vakje leidde hierheen (nodig om achteraf het pad te reconstrueren)
        int[][] gScore      = new int[n][n];
        int[][] fScore      = new int[n][n];
        Point[][] ouder     = new Point[n][n];
        boolean[][] bezocht = new boolean[n][n];

        for (int[] rij : gScore) Arrays.fill(rij, Integer.MAX_VALUE);
        for (int[] rij : fScore) Arrays.fill(rij, Integer.MAX_VALUE);

        gScore[vanGx][vanGy] = 0;
        fScore[vanGx][vanGy] = heuristiek(vanGx, vanGy, naarGx, naarGy);

        // Wachtrij die altijd het meest veelbelovende vakje (laagste fScore) als eerste geeft
        PriorityQueue<Point> openLijst = new PriorityQueue<>(
                Comparator.comparingInt(p -> fScore[p.x][p.y])
        );
        openLijst.add(new Point(vanGx, vanGy));

        // Geen diagonaal — alleen omlaag, omhoog, rechts, links
        int[][] richtingen = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!openLijst.isEmpty()) {
            Point huidig = openLijst.poll();

            // Doel bereikt → zoek het pad terug via de ouder-verwijzingen
            if (huidig.x == naarGx && huidig.y == naarGy) {
                return reconstrueerPad(ouder, huidig);
            }

            if (bezocht[huidig.x][huidig.y]) continue;
            bezocht[huidig.x][huidig.y] = true;

            // Bekijk alle vier buren
            for (int[] dir : richtingen) {
                int nx = huidig.x + dir[0];
                int ny = huidig.y + dir[1];

                if (nx < 0 || nx > GRID_GROOTTE || ny < 0 || ny > GRID_GROOTTE) continue;
                if (!loopbaar[nx][ny]) continue;
                if (bezocht[nx][ny]) continue;

                // Extra check voor dunne kamers (1 vakje breed): stap mag niet dwars door een kamer gaan
                if (!isStapToegestaan(huidig.x, huidig.y, nx, ny, ruimtes)) continue;

                int nieuweG = gScore[huidig.x][huidig.y] + 1;
                if (nieuweG < gScore[nx][ny]) {
                    ouder[nx][ny]  = huidig;
                    gScore[nx][ny] = nieuweG;
                    fScore[nx][ny] = nieuweG + heuristiek(nx, ny, naarGx, naarGy);
                    openLijst.add(new Point(nx, ny));
                }
            }
        }

        System.out.println("Geen pad gevonden!");
        return new ArrayList<>();
    }


    // Controleert of een stap van A naar B niet dwars door een dunne kamer gaat
    // Dunne kamers (1 vakje breed) hebben geen interieur dat geblokkeerd wordt,
    // maar je mag er toch niet doorheen stappen
    private static boolean isStapToegestaan(int gx, int gy, int nx, int ny,
                                            List<HotelRuimte> ruimtes) {
        for (HotelRuimte r : ruimtes) {
            // Trap: blokkeer x=7 BEHALVE bij ingangen (y=1, 4, 7)
            if (r instanceof Trap) {
                int trapGx = r.getX() + 1; // dynamisch berekend = 8, niet hardcoded 7
                if (nx == trapGx && gx != trapGx) {
                    boolean isIngang = (ny == 2 || ny == 5 || ny == 8);
                    if (!isIngang) return false;
                }
                continue;
            }

            if (!(r instanceof HotelKamer)    &&
                    !(r instanceof Bioscoop)       &&
                    !(r instanceof FitnessRuimtes) &&
                    !(r instanceof Restaurant)) continue;

            int links  = r.getX() + 1;
            int rechts = r.getX() + 1 + r.getBreedte();
            int boven  = r.getY() - 1;
            int onder  = r.getY() - 1 + r.getHoogte();

            if (gy == ny) {
                // Horizontale stap: geblokkeerd als de stap door het kamer-oppervlak gaat
                int minGx = Math.min(gx, nx);
                if (gy > boven && gy < onder && minGx >= links && minGx < rechts)
                    return false;
            } else {
                // Verticale stap: zelfde idee maar dan voor kolommen
                int minGy = Math.min(gy, ny);
                if (gx > links && gx < rechts && minGy >= boven && minGy < onder)
                    return false;
            }
        }
        return true;
    }


    // Maakt een kaart van het hotel: true = mag lopen, false = kamer-interieur (geblokkeerd)
    // Alleen het INTERIEUR wordt geblokkeerd — de randen blijven vrij zodat gasten er langs kunnen
    public static boolean[][] berekenLoopbaar(List<HotelRuimte> ruimtes) {
        int n = GRID_GROOTTE + 1;
        boolean[][] loopbaar = new boolean[n][n];
        for (boolean[] rij : loopbaar) Arrays.fill(rij, true);

        for (HotelRuimte r : ruimtes) {
            if (!(r instanceof HotelKamer)    &&
                    !(r instanceof Bioscoop)       &&
                    !(r instanceof FitnessRuimtes) &&
                    !(r instanceof Restaurant)) continue;

            int links  = r.getX() + 1;
            int rechts = r.getX() + 1 + r.getBreedte();
            int boven  = r.getY() - 1;
            int onder  = r.getY() - 1 + r.getHoogte();

            // links+1 en boven+1 slaan de rand over — alleen het interieur wordt geblokkeerd
            for (int x = links + 1; x < rechts; x++)
                for (int y = boven + 1; y < onder; y++)
                    if (x >= 0 && x < n && y >= 0 && y < n)
                        loopbaar[x][y] = false;

        }
        for (HotelRuimte r : ruimtes) {
            if (r instanceof Trap) {
                int trapGx = r.getX() + 1; // = 8
                for (int y = 0; y <= GRID_GROOTTE; y++) {
                    loopbaar[trapGx][y] = false;
                }
                // Alleen de 3 ingangen zijn beloopbaar
                loopbaar[trapGx][2] = true;
                loopbaar[trapGx][5] = true;
                loopbaar[trapGx][8] = true;
            }
        }
        return loopbaar;
    }




    // Geeft de ingang van een kamer terug: midden van de onderkant
    // Gasten komen altijd van onderen de kamer in
    public static Point getKamerIngang(HotelRuimte kamer) {
        int pixelLinks = (kamer.getX() + 1) * CEL_GROOTTE;
        int pixelBreed = kamer.getBreedte() * CEL_GROOTTE;
        int onderY     = (kamer.getY() - 1 + kamer.getHoogte()) * CEL_GROOTTE;
        int ingangX    = (pixelLinks + pixelBreed / 2) / CEL_GROOTTE * CEL_GROOTTE;
        return new Point(ingangX, onderY);
    }


    // Schat hoeveel stappen er nog nodig zijn naar het doel (Manhattan Distance)
    // Telt horizontale + verticale afstand — geen diagonaal
    private static int heuristiek(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }


    // Zoek het pad terug van doel naar start via de opgeslagen ouder-verwijzingen
    // addFirst() zorgt dat het resultaat van start naar doel loopt
    private static List<Point> reconstrueerPad(Point[][] ouder, Point doel) {
        LinkedList<Point> pad = new LinkedList<>();
        Point huidig = doel;
        while (huidig != null) {
            pad.addFirst(new Point(huidig.x * CEL_GROOTTE, huidig.y * CEL_GROOTTE));
            huidig = ouder[huidig.x][huidig.y];
        }
        return pad;
    }


    // Houdt een getal binnen een min en max grens
    // Voorbeeld: klem(12, 0, 10) → 10 | klem(-3, 0, 10) → 0 | klem(5, 0, 10) → 5
    private static int klem(int waarde, int min, int max) {
        return Math.max(min, Math.min(max, waarde));
    }
}