package pl.edu.agh.Analyzer.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by karolina on 11.07.17.
 */
public class MainUI {
    public static void main (String args[]){
        //inicjalizacja wszystkiego - na razue tylko  bufora wejsciowego
        //petla nieskonczone
        //funkckja do wypisywania komend
        //funckja do informowania "aby wypisac komendy, wpisz ?"
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean isRunning = true;
        try {
            while (isRunning) {
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line.equals("q")) {
                    isRunning = false;
                }
                else if (line.startsWith("d"))
                    myPrint("New notes will be downloaded");
                else if (line.startsWith("t"))
                    myPrint("Notes will get missing tags");
                else if (line.startsWith("u"))
                    myPrint("Database will be updated with new data");
                else if (line.startsWith("a"))
                    myPrint("Analysis will start  soon...");
                else if (line.startsWith("?"))
                    listCommands();
                if (!line.startsWith("?") && isRunning)
                    iterativeCommand();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void listCommands(){
        myPrint("List of commands:");
        myPrint("\t d -> download new press notes");
        myPrint("\t t -> tag new notes");
        myPrint("\t u -> update database with new data (notes and tags");
        myPrint("\t a -> analyse social network");
        myPrint("\t ? -> show this message");
    }
    private static void iterativeCommand(){
        myPrint("Type ? and press ENTER to show all commands");
    }

    private static void myPrint(String s){
        System.out.println(s);
    }
}
