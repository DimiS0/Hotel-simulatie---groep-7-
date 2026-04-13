package hotelsimulator.pathfinding;

import hotelsimulator.ruimtes.*;
import java.awt.Point;
import java.util.*;

public class Pathfinder {

    public static final int GRID_GROOTTE = 10;
    public static final int CEL_GROOTTE  = 50;

    public static List<Point> vindPad(int startPixelX, int startPixelY,
                                      int eindPixelX,  int eindPixelY,
                                      List<HotelRuimte> ruimtes) {
        int vanGx  = klem(startPixelX / CEL_GROOTTE, 0, GRID_GROOTTE);
        int vanGy  = klem(startPixelY / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarGx = klem(eindPixelX  / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarGy = klem(eindPixelY  / CEL_GROOTTE, 0, GRID_GROOTTE);

        boolean[][] loopbaar = berekenLoopbaar(ruimtes);

        if (!loopbaar[vanGx][vanGy] || !loopbaar[naarGx][naarGy]) {
            System.out.println("Start of einde zit in een kamer!");
            return new ArrayList<>();
        }

        int n = GRID_GROOTTE + 1;
        int[][] gScore     = new int[n][n];
        int[][] fScore     = new int[n][n];
        Point[][] ouder    = new Point[n][n];
        boolean[][] bezocht = new boolean[n][n];

        for (int[] rij : gScore) Arrays.fill(rij, Integer.MAX_VALUE);
        for (int[] rij : fScore) Arrays.fill(rij, Integer.MAX_VALUE);

        gScore[vanGx][vanGy] = 0;
        fScore[vanGx][vanGy] = heuristiek(vanGx, vanGy, naarGx, naarGy);

        PriorityQueue<Point> openLijst = new PriorityQueue<>(
                Comparator.comparingInt(p -> fScore[p.x][p.y])
        );
        openLijst.add(new Point(vanGx, vanGy));

        int[][] richtingen = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!openLijst.isEmpty()) {
            Point huidig = openLijst.poll();

            if (huidig.x == naarGx && huidig.y == naarGy) {
                return reconstrueerPad(ouder, huidig);
            }

            if (bezocht[huidig.x][huidig.y]) continue;
            bezocht[huidig.x][huidig.y] = true;

            for (int[] dir : richtingen) {
                int nx = huidig.x + dir[0];
                int ny = huidig.y + dir[1];

                if (nx < 0 || nx > GRID_GROOTTE || ny < 0 || ny > GRID_GROOTTE) continue;
                if (!loopbaar[nx][ny]) continue;
                if (bezocht[nx][ny]) continue;

                // *** NIEUWE CHECK: blokkeer stappen die door een kamer gaan ***
                if (!isStapToegestaan(huidig.x, huidig.y, nx, ny, ruimtes)) continue;

                int nieuweG = gScore[huidig.x][huidig.y] + 1;
                if (nieuweG < gScore[nx][ny]) {
                    ouder[nx][ny] = huidig;
                    gScore[nx][ny] = nieuweG;
                    fScore[nx][ny] = nieuweG + heuristiek(nx, ny, naarGx, naarGy);
                    openLijst.add(new Point(nx, ny));
                }
            }
        }

        System.out.println("Geen pad gevonden!");
        return new ArrayList<>();
    }

    /**
     * Controleert of een stap van (gx,gy) naar (nx,ny) door een kamer gaat.
     * Dit is nodig voor dunne kamers (breedte=1 of hoogte=1) waar geen interne
     * knooppunten zijn maar de verbinding ertussen WEL door de kamer gaat.
     */
    private static boolean isStapToegestaan(int gx, int gy, int nx, int ny,
                                            List<HotelRuimte> ruimtes) {
        for (HotelRuimte r : ruimtes) {
            if (!(r instanceof HotelKamer)    &&
                    !(r instanceof Bioscoop)       &&
                    !(r instanceof FitnessRuimtes) &&
                    !(r instanceof Restaurant)) continue;

            int links = r.getX() + 1;
            int rechts = r.getX() + 1 + r.getBreedte();
            int boven  = r.getY() - 1;
            int onder  = r.getY() - 1 + r.getHoogte();

            if (gy == ny) {
                // Horizontale stap — geblokkeerd als gy door het interieur gaat
                // én de stap in de x-richting van de kamer valt
                int minGx = Math.min(gx, nx);
                if (gy > boven && gy < onder && minGx >= links && minGx < rechts) {
                    return false;
                }
            } else {
                // Verticale stap — geblokkeerd als gx door het interieur gaat
                // én de stap in de y-richting van de kamer valt
                int minGy = Math.min(gy, ny);
                if (gx > links && gx < rechts && minGy >= boven && minGy < onder) {
                    return false;
                }
            }
        }
        return true;
    }

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

            // Blokkeer strikt interne knooppunten (niet de randen)
            for (int x = links + 1; x < rechts; x++) {
                for (int y = boven + 1; y < onder; y++) {
                    if (x >= 0 && x < n && y >= 0 && y < n) {
                        loopbaar[x][y] = false;
                    }
                }
            }
        }
        return loopbaar;
    }

    public static Point getKamerMidden(HotelRuimte kamer) {
        int pixelLinks = (kamer.getX() + 1) * CEL_GROOTTE;
        int pixelBoven = (kamer.getY() - 1) * CEL_GROOTTE;
        int pixelBreed = kamer.getBreedte() * CEL_GROOTTE;
        int pixelHoog  = kamer.getHoogte() * CEL_GROOTTE;
        // Midden in X (snapped naar dichtstbijzijnde gridlijn van 50)
        int middenX = (pixelLinks + pixelBreed / 2) / CEL_GROOTTE * CEL_GROOTTE;
        // Midden in Y
        int middenY = pixelBoven + pixelHoog / 2;
        return new Point(middenX, middenY);
    }
    public static Point getKamerIngang(HotelRuimte kamer) {
        int pixelLinks = (kamer.getX() + 1) * CEL_GROOTTE;
        int pixelBreed = kamer.getBreedte() * CEL_GROOTTE;
        int onderY     = (kamer.getY() - 1 + kamer.getHoogte()) * CEL_GROOTTE;
        // Ingang X: snapped naar dichtstbijzijnde gridlijn
        int ingangX = (pixelLinks + pixelBreed / 2) / CEL_GROOTTE * CEL_GROOTTE;
        return new Point(ingangX, onderY);
    }

    private static int heuristiek(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static List<Point> reconstrueerPad(Point[][] ouder, Point doel) {
        LinkedList<Point> pad = new LinkedList<>();
        Point huidig = doel;
        while (huidig != null) {
            pad.addFirst(new Point(huidig.x * CEL_GROOTTE, huidig.y * CEL_GROOTTE));
            huidig = ouder[huidig.x][huidig.y];
        }
        return pad;
    }

    private static int klem(int waarde, int min, int max) {
        return Math.max(min, Math.min(max, waarde));
    }
}