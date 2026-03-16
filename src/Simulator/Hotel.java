package Simulator;

public class Hotel {
    private Kamer vakje;
    private Kamer kamer;
    private Gast gast;
    private Schoonmaker schoonmaker;

    public Hotel(){
        this.gast = new Gast();
        this.schoonmaker = new Schoonmaker();
        this.kamer = new Kamer();
        this.vakje = null;
    }

    public void hotelStart(){

    }
}
