package hotelsimulator.pathfinding;

import hotelsimulator.ruimtes.*;
import java.awt.Point;
import java.util.*;

public class Pathfinder {

    public static final int GRID_GROOTTE = 10;
    public static final int CEL_GROOTTE  = 50;

    public static List<Point> vindPad(int startX, int startY, int eindX, int eindY,
                                      List<HotelRuimte> ruimtes) {
        int vanX  = klem(startX / CEL_GROOTTE, 0, GRID_GROOTTE);
        int vanY  = klem(startY / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarX = klem(eindX  / CEL_GROOTTE, 0, GRID_GROOTTE);
        int naarY = klem(eindY  / CEL_GROOTTE, 0, GRID_GROOTTE);

        boolean[][] loopbaar = berekenLoopbaar(ruimtes);

        if (!loopbaar[vanX][vanY] || !loopbaar[naarX][naarY]) {
            System.out.println("Start of einde zit in een kamer!");
            return new ArrayList<>();
        }

        int n = GRID_GROOTTE + 1;
        int[][] gScore      = new int[n][n];
        int[][] fScore      = new int[n][n];
        Point[][] ouder     = new Point[n][n];
        boolean[][] bezocht = new boolean[n][n];

        for (int[] rij : gScore) Arrays.fill(rij, Integer.MAX_VALUE);
        for (int[] rij : fScore) Arrays.fill(rij, Integer.MAX_VALUE);

        gScore[vanX][vanY] = 0;
        fScore[vanX][vanY] = heuristiek(vanX, vanY, naarX, naarY);

        PriorityQueue<Point> open = new PriorityQueue<>(
                Comparator.comparingInt(p -> fScore[p.x][p.y]));
        open.add(new Point(vanX, vanY));

        int[][] richtingen = {{0,1},{0,-1},{1,0},{-1,0}};

        while (!open.isEmpty()) {
            Point huidig = open.poll();
            if (huidig.x == naarX && huidig.y == naarY) return reconstrueerPad(ouder, huidig);
            if (bezocht[huidig.x][huidig.y]) continue;
            bezocht[huidig.x][huidig.y] = true;

            for (int[] d : richtingen) {
                int nx = huidig.x + d[0], ny = huidig.y + d[1];
                if (nx < 0 || nx > GRID_GROOTTE || ny < 0 || ny > GRID_GROOTTE) continue;
                if (!loopbaar[nx][ny] || bezocht[nx][ny]) continue;
                if (!isStapToegestaan(huidig.x, huidig.y, nx, ny, ruimtes)) continue;

                int nieuweG = gScore[huidig.x][huidig.y] + 1;
                if (nieuweG < gScore[nx][ny]) {
                    ouder[nx][ny]  = huidig;
                    gScore[nx][ny] = nieuweG;
                    fScore[nx][ny] = nieuweG + heuristiek(nx, ny, naarX, naarY);
                    open.add(new Point(nx, ny));
                }
            }
        }

        System.out.println("Geen pad gevonden!");
        return new ArrayList<>();
    }

    private static boolean isStapToegestaan(int gx, int gy, int nx, int ny,
                                            List<HotelRuimte> ruimtes) {
        for (HotelRuimte r : ruimtes) {
            if (r instanceof Trap) {
                int trapX = r.getX() + 1;
                if (nx == trapX && gx != trapX && ny != 2 && ny != 5 && ny != 8) return false;
                continue;
            }
            if (!(r instanceof HotelKamer) && !(r instanceof Bioscoop)
                    && !(r instanceof FitnessRuimtes) && !(r instanceof Restaurant)) continue;

            int links = r.getX() + 1, rechts = links + r.getBreedte();
            int boven = r.getY() - 1, onder  = boven + r.getHoogte();

            if (gy == ny) {
                if (gy > boven && gy < onder && Math.min(gx, nx) >= links && Math.min(gx, nx) < rechts)
                    return false;
            } else {
                if (gx > links && gx < rechts && Math.min(gy, ny) >= boven && Math.min(gy, ny) < onder)
                    return false;
            }
        }
        return true;
    }

    public static boolean[][] berekenLoopbaar(List<HotelRuimte> ruimtes) {
        int n = GRID_GROOTTE + 1;
        boolean[][] loopbaar = new boolean[n][n];
        for (boolean[] rij : loopbaar) Arrays.fill(rij, true);

        for (HotelRuimte r : ruimtes) {
            if (r instanceof HotelKamer || r instanceof Bioscoop
                    || r instanceof FitnessRuimtes || r instanceof Restaurant) {
                int links = r.getX() + 1, rechts = links + r.getBreedte();
                int boven = r.getY() - 1, onder  = boven + r.getHoogte();
                for (int x = links + 1; x < rechts; x++)
                    for (int y = boven + 1; y < onder; y++)
                        if (x >= 0 && x < n && y >= 0 && y < n) loopbaar[x][y] = false;
            }
            if (r instanceof Trap) {
                int trapX = r.getX() + 1;
                for (int y = 0; y <= GRID_GROOTTE; y++) loopbaar[trapX][y] = false;
                loopbaar[trapX][2] = loopbaar[trapX][5] = loopbaar[trapX][8] = true;
            }
        }
        return loopbaar;
    }

    public static Point getKamerIngang(HotelRuimte kamer) {
        int pixelLinks = (kamer.getX() + 1) * CEL_GROOTTE;
        int ingangX    = (pixelLinks + kamer.getBreedte() * CEL_GROOTTE / 2) / CEL_GROOTTE * CEL_GROOTTE;
        int onderY     = (kamer.getY() - 1 + kamer.getHoogte()) * CEL_GROOTTE;
        return new Point(ingangX, onderY);
    }

    private static int heuristiek(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static List<Point> reconstrueerPad(Point[][] ouder, Point doel) {
        LinkedList<Point> pad = new LinkedList<>();
        for (Point p = doel; p != null; p = ouder[p.x][p.y])
            pad.addFirst(new Point(p.x * CEL_GROOTTE, p.y * CEL_GROOTTE));
        return pad;
    }

    private static int klem(int waarde, int min, int max) {
        return Math.max(min, Math.min(max, waarde));
    }
}