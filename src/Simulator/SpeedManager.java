package Simulator;
import java.util.Scanner;
public class SpeedManager {
    private NaarHTEOmzetten n;
    private HTE currentSpeed;

    public SpeedManager(){
        this.n = new NaarHTEOmzetten();
    }
    public void kiesSnelheid(String label){

    }
    public void toonSnelheid(){

    }
    public NaarHTEOmzetten omzettenNaarHte(String Label){
        NaarHTEOmzetten snelheid = n.fromLabel(Label);
        return NaarHTEOmzetten;
    }
}
