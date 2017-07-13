package pl.edu.agh.Analyzer;

import database.util.HibernateUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.agh.Analyzer.ui.AnalysisHandler;
import rss.Main;
import tagger.Tagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by karolina on 11.07.17.
 */
@SpringBootApplication
public class MainUI {
    public static void main (String args[]){

        SpringApplication.run(MainUI.class, args);
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final AnalysisHandler handler = new AnalysisHandler(br);
        boolean isRunning = true;
        try {
            while (isRunning) {
                iterativeMsg();
                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line.startsWith("q")) {
                    isRunning = false;
                }
                else if (line.startsWith("d")) {
                    myPrint("New notes will be downloaded");
                    Main.main(null);
                    myPrint("Notes has been successfully downloaded");
                }
                else if (line.startsWith("t")) {
                    myPrint("Notes will get missing tags");
                    Tagger.main(null);
                    myPrint("Tagging finished successfully");
                }
                else if (line.startsWith("u")) {
                    myPrint("Database will be updated with new data");
                    HibernateUtil.main(null);
                    myPrint("Database updated successfully");
                }
                else if (line.startsWith("a")) {
                    myPrint("Analysis will start  soon...");
                    handler.startHandling();
                    myPrint("Analysis finished successfully");
                }
                else if (line.startsWith("?"))
                    listCommands();
            }
            myPrint("Goodbye!");
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void listCommands(){
        myPrint("List of commands:\n" +
            "\t d -> download new press notes\n" +
            "\t t -> tag new notes\n" +
            "\t u -> update database with new data (notes and tags)\n" +
            "\t a -> analyse social network\n" +
            "\t q -> quit application\n" +
            "\t ? -> show this message\n");
    }
    private static void iterativeMsg(){
        myPrint("Type ? and press ENTER to show all commands");
    }

    private static final void myPrint(String s){
        System.out.println(s);
    }
}
