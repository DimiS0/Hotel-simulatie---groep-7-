package hotelsimulator.ruimtes;

import java.awt.*;

public abstract class HotelRuimte {
	protected String areaType;
	protected String sterrenAantal;
	protected int y;
	protected int x;
	protected int breedte;
	protected int hoogte;
	protected int maxPersonen;
    private int aantalAanwezig = 0;
	public HotelRuimte(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen) {
		this.areaType = areaType;
		this.sterrenAantal = sterrenAantal;
		this.y = 10 - y - hoogte + 1;
		this.x = x;
		this.breedte = breedte;
		this.hoogte = hoogte;
		this.maxPersonen = maxPersonen;
	}
    public boolean isVol() {
        return maxPersonen > 0 && aantalAanwezig >= maxPersonen;
    }
    public synchronized void betreed() {
        aantalAanwezig++;
    }
    public synchronized void verlaat() {
        if (aantalAanwezig > 0) aantalAanwezig--;
    }
    public int getAantalAanwezig() {
        return aantalAanwezig;
    }
	public String getAreaType() {
		return areaType;
	}

	public String getSterrenAantal() {
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
    public abstract void print(Graphics g, int cellSize);

}