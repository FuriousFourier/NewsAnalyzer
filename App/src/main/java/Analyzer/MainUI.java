package Analyzer;

import Analyzer.controller.AnalysisController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import Analyzer.ui.AnalysisHandler;
import Analyzer.secondProject.rss.Main;
import org.springframework.context.ConfigurableApplicationContext;
//import Analyzer.secondProject.tagger.Tagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static org.springframework.http.HttpHeaders.USER_AGENT;


/**
 * Created by karolina on 11.07.17.
 */
@SpringBootApplication
public class MainUI {
    public static Long securityNumber;
    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main (String args[]){
        Random random = new Random(System.currentTimeMillis());
        securityNumber = random.nextLong();
        configurableApplicationContext = SpringApplication.run(MainUI.class, args);
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final AnalysisController analysisController = new AnalysisController(br);
        final AnalysisHandler handler = new AnalysisHandler(br, analysisController);
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
                    //myPrint("Notes will get missing tags");
                    //Tagger.main(null);
                    myPrint("Tagging finished successfully");
                }
                else if (line.startsWith("u")) {//TODO: zmienic na bezposrednie wolanie DbUtil!
                    myPrint("Database will be updated with new data");
                    URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + securityNumber);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = connection.getResponseCode();
                    System.out.println("*************");
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("*************");
                    myPrint("Database updated successfully");
                }
                else if (line.startsWith("p")) { //TODO: zmienic na bezposrednie wolanie DbUtil!
                    myPrint("Tags will be fetched soon...");
                    URL url = new URL("http://localhost:8080/getTags");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = connection.getResponseCode();
                    System.out.println("*************");
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("*************");
                    myPrint("All  tags have been printed out");
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
        System.exit(0);
    }

    private static void listCommands(){
        myPrint("List of commands:\n" +
                "\t d -> download new press notes\n" +
                "\t t -> tag new notes\n" +
                "\t u -> update database with new data (notes and tags)\n" +
                "\t p -> print out all tags\n" +
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

    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    public static void setConfigurableApplicationContext(ConfigurableApplicationContext configurableApplicationContext) {
        MainUI.configurableApplicationContext = configurableApplicationContext;
    }
}
