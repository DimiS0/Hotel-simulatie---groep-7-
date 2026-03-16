package Simulator;

public class Hotel {
    private HotelRuimte vakje;
    private HotelRuimte hotelRuimte;
    private Gast gast;
    private Schoonmaker schoonmaker;

    public Hotel(){
        this.gast = new Gast();
        this.schoonmaker = new Schoonmaker();
        this.hotelRuimte = new HotelRuimte();
        this.vakje = null;
    }

    public void hotelStart(){

    }
}
