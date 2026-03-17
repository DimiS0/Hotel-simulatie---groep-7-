package Simulator;
import java.util.ArrayList;

public class HotelRuimte {
    protected String areaType;
    protected String sterrenAantal;
    protected int y;
    protected int x;
    protected int breedte;
    protected int maxPersonen;


    public HotelRuimte(String areaType,String sterrenAantal,int y, int x, int breedte,int maxPersonen ){

   this.areaType= areaType;
   this.sterrenAantal = sterrenAantal;
   this.y = y;
   this.x = x;
   this.breedte = breedte;
   this.maxPersonen = maxPersonen;

    }
    public String getAreaType(){return areaType;}
    public String getSterrenAantal(){return sterrenAantal;}
    public int getY(){return y;}
    public int getX(){return x;}
    public int getBreedte(){return breedte;}
    public int getMaxPersonen(){return maxPersonen;}
}
