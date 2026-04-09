package hotelsimulator.ruimtes;

import hotelsimulator.core.Hotel;

import java.awt.*;

public class Lift extends HotelRuimte {
   private int [] verdiepingenY = {10,7,4};
   private int stopPositie = 10;
   private int doelStopVerdieping = 10;
    private boolean beschikbaar = true;
    private boolean omhoog = true;
    private Hotel hotel;

    public Lift(String areaType, String sterrenAantal, int y, int x, int breedte, int hoogte, int maxPersonen, Hotel hotel) {
        super(areaType, sterrenAantal, y, x, breedte, hoogte, maxPersonen);
        this.hotel = hotel;
    }
    @Override
    public void print(Graphics g, int cellSize) {
        //vul kleur, waar hij moet vullen, x en y-1 omdat de grid bij 0 begint en de JSON bij 1 , breedte en hoogte ook want anders is elk kamer 1 pixel
        Color zachtrose = new Color(255,128,139);
        g.setColor(zachtrose);
        g.fillRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //outline, waar hij een outline moet tekenen
        g.setColor(Color.BLACK);
        g.drawRect((x+1)*cellSize, (y-1)*cellSize, breedte*cellSize, hoogte*cellSize);

        //text kleur, waar hij de string moet neerzetten
        g.setColor(Color.WHITE);
        g.drawString("Lift", (x+1)*cellSize + 5, (y-1)*cellSize + 15);
    }

        public void liftBwegen() {

            if (beschikbaar){
                return;
            }

            if (y > doelStopVerdieping){
                y--;
            } else if (y < doelStopVerdieping) {
                y++;
            }
            if (y == doelStopVerdieping) {
                stopPositie = doelStopVerdieping;
                hotel.getLiftOproepen().removeFirst();
                beschikbaar = true;

                //lijst niet leeg? pak volgende
                if (!hotel.getLiftOproepen().isEmpty()){
                    roepLiftNaar(hotel.getLiftOproepen().getFirst());
                }
            }
            System.out.println("y na beweging: " + y);
        }

        public void roepLiftNaar(int verdieping){
            doelStopVerdieping = verdieping;
            beschikbaar = false;
        }
        public int [] getVerdiepingenY(){
            return verdiepingenY;
        }
        public boolean getBeschikbaar(){
            return beschikbaar;
        }
    }