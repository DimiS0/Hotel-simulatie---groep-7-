package Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class HoofdSimulator {
    private StarterGui swingGui;
    private Evenement event;
    private HTE hte;

    public HoofdSimulator() {
        swingGui = new StarterGui();
    }

public void start() {
    Scanner scanner = new Scanner(System.in);
    Hotel hotel = new Hotel();

    swingGui.guiStart();
    System.out.println("Voer een naam van een .layout bestand in");
    String bestandnaam = scanner.nextLine();

    File file = new File(bestandnaam);

    try {
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()){
            String regel = fileScanner.nextLine();
            System.out.println(regel);
        }
        fileScanner.close();

    } catch (FileNotFoundException e) {
        System.out.println("Bestand niet gevonden: " + e.getMessage());

         }
    hotel.hotelLayout();
    }
}
