package Simulator;

public class SpeedManager {
    private int Factor;
    private String Label;
    private HTEclass n = new HTEclass();
    public SpeedManager (int Factor, String Label) {
        this.Factor = Factor;
        this.Label = Label;
    }
    public HTE omzettenNaarHte(){
        HTE HTEwaarde = n.fromLabel(Label);
        return HTEwaarde;
    }
    public void toonSnelheid(){
        System.out.println(Factor);
        System.out.println(Label);
    }
}
