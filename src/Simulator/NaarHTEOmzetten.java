package Simulator;
public class NaarHTEOmzetten {

    //zet label om naar HTE enum waarde
    public HTE fromLabel(String label) {
        for (HTE s : HTE.values()) {
            if (s.getLabel().equalsIgnoreCase(label)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Ongeldige snelheid: " + label);
    }
}
