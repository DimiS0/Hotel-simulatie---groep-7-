package Simulator;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class HoofdSimulator {
    private StarterGui swingGui;
    private Evenement event;
    private HTE hte;

    public HoofdSimulator() {
        swingGui = new StarterGui(this);
    }

public void start() {
    Scanner scanner = new Scanner(System.in);
    Hotel hotel = new Hotel();

    swingGui.guiStart();
}

    public void LayoutKiezer(){
        JFileChooser fileChooser = new JFileChooser();
        System.out.println("Voer een naam van een .layout bestand in");

        int result = fileChooser.showOpenDialog(null);

        if(result != JFileChooser.APPROVE_OPTION){
            System.out.println("geen bestand gekozen");
            return;
        }
        File file = fileChooser.getSelectedFile();

        try {
            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String regel = fileScanner.nextLine();

                //regel vervangen met layout?
                System.out.println(swingGui.layout);
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Bestand niet gevonden: " + e.getMessage());
            JOptionPane.showMessageDialog(
                    null,
                    "Het gekozen layoutbestand kan niet worden geladen.",
                    "Fout",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
    }
}
