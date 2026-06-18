package hotelsimulator.ruimtes;

import java.awt.*;

public abstract class HotelRuimte {
	protected String areaType;
	protected int sterrenAantal;
	protected int y;
	protected int x;
	protected int breedte;
	protected int hoogte;
	protected int maxPersonen;
    private int aantalGasten = 0;
    private int aantalSchoonmakers = 0;
    private boolean cleaningEmergency = false;
    private int orgineleY;
	public HotelRuimte(String areaType, int sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		this.areaType = areaType;
		this.sterrenAantal = sterrenAantal;
        this.orgineleY = y;
		this.y = y;
		this.x = x;
		this.breedte = breedte;
		this.hoogte = hoogte;
		this.maxPersonen = maxPersonen;
	}
    public boolean isVol() {
        return maxPersonen > 0 && (aantalGasten + aantalSchoonmakers) >= maxPersonen;
    }

    public synchronized void betreedAlsGast() {
        aantalGasten++;
    }

    public synchronized void verlaat() {
        if (aantalGasten > 0) {
            aantalGasten--;
        }}

    public synchronized void betreedAlsSchoonmaker() {
        aantalSchoonmakers++;
    }
    public synchronized void verlaatAlsSchoonmaker() {
        if (aantalSchoonmakers > 0) aantalSchoonmakers--;
    }

    //nodig voor dynamisch maken van layout
    public int getOrgineleY() {
        return orgineleY;
    }

    @SuppressWarnings("unused")
    public boolean isBeloopbaar(int gridX, int gridY) {
        return true;
    }
    public int[] getIngangen() {
        return new int[0];
    }
    public boolean isCleaningEmergency() {
        return cleaningEmergency;
    }
    public void setCleaningEmergency(boolean waarde) {
        cleaningEmergency = waarde;
    }

    public String getAreaType() {
        return areaType;
    }

    public int getSterrenAantal() {
        return sterrenAantal;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getBreedte() {
        return breedte;
    }

    public int getHoogte() {
        return hoogte;
    }

    public int getMaxPersonen() {
        return maxPersonen;
    }

    public long getVerblijfMs() {
        return 5000;
    }

    public abstract void print(Graphics g, int cellSize);

    public void herberekenY(int maxHoogte) {
        // +1 voor de lege roosterrij bovenaan
        this.y = maxHoogte - orgineleY - hoogte + 2;
    }
}