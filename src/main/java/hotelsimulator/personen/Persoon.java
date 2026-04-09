package hotelsimulator.personen;

import hotelsimulator.core.Hotel;
import hotelsimulator.ruimtes.HotelRuimte;
import hotelsimulator.ruimtes.Lift;
import hotelsimulator.ruimtes.Schacht;

import java.awt.*;
import java.util.Random;

public abstract class Persoon {
    private boolean heeftVerzoekIngediend = false;
    protected HotelRuimte huidigeRuimte;
    protected int pixelX;
    protected int pixelY;
    protected int doelX;
    protected int doelY;
    protected Hotel hotel;
    protected Schacht schacht;
    protected Lift lift;
    protected static final int SNELHEID = 2;
    protected static final Random random = new Random();

    public Persoon(int startX, int startY, Lift lift, Schacht schacht, Hotel hotel) {
        this.hotel = hotel;
        this.schacht  = schacht;
        this.lift  = lift;
        this.pixelX = startX;
        this.pixelY = startY;
        this.doelX = startX;
        this.doelY = startY;
    }

    // Beweegt 1 stap richting het doel
    public void beweeg() {
        if (pixelX < doelX) pixelX += SNELHEID;
        else if (pixelX > doelX) pixelX -= SNELHEID;

        if (pixelY < doelY) pixelY += SNELHEID;
        else if (pixelY > doelY) pixelY -= SNELHEID;

        int gridX = pixelX /50;
        int gridY = pixelY /50;

        if (gridX == schacht.getX()){
            for (int verdieping : lift.getVerdiepingenY()){
                if (gridY == verdieping){
                    liftVerzoek(gridY);
                }
            }
        } else {
            //als ze niet op het vakje zijn
            heeftVerzoekIngediend = false;
        }


    }

    // Kiest een nieuw random doel op de zwarte lijnen (grid-lijnen)
    public void kiesRandomDoel(int cellSize, int gridGrootte) {
        // Snap naar de dichtstbijzijnde gridlijn
        int randomRij = random.nextInt(gridGrootte);
        int randomKolom = random.nextInt(gridGrootte);
        doelX = randomKolom * cellSize;
        doelY = randomRij * cellSize;
    }

    public boolean isOpDoel() {
        return pixelX == doelX && pixelY == doelY;
    }

    public void liftVerzoek(int stopVerdieping){

        //verzoeken boolean zodat ze niet elke seconde 100den verzoeken maken
        if (!heeftVerzoekIngediend) {
            heeftVerzoekIngediend = true;

            //niet opgeroepen worden als de lift dezelfde y heeft
            if (lift.getY() != stopVerdieping) {
                hotel.getLiftOproepen().addLast(stopVerdieping);
                System.out.println("verzoek " + hotel.getLiftOproepen().size());

                //bechikbaar? lift naar locatie sturen van gast/schoonmaker
                if (lift.getBeschikbaar()) {
                    lift.roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
        }
    }

    public abstract void print(Graphics g);

    public HotelRuimte getHuidigeRuimte() { return huidigeRuimte; }
    public void setHuidigeRuimte(HotelRuimte r) { this.huidigeRuimte = r; }
}