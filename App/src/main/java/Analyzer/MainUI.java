package Analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import Analyzer.ui.AnalysisHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static Analyzer.info.InfoContainer.analysisChartsPath;
import static Analyzer.info.InfoContainer.analysisCsvPath;
import static Analyzer.info.InfoContainer.analysisPdfPath;
import static org.springframework.http.HttpHeaders.USER_AGENT;


/**
 * Created by karolina on 11.07.17.
 */
@SpringBootApplication
public class MainUI {
    public static Long securityNumber;
    public MainUI(){

    }

    public static void main (String args[]) {
        Random random = new Random(System.currentTimeMillis());
        securityNumber = random.nextLong();
        MainUI mainUI = new MainUI();
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
                else if (line.startsWith("p")) {
                    synchronized (mainUI) {
                        myPrint("Tags will be fetched soon...");
                        URL url = new URL("http://localhost:8080/getTags");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestProperty("User-Agent", USER_AGENT);
                        int responseCode = connection.getResponseCode();
                        System.out.println("*************");
                        System.out.println("Response Code: " + responseCode);
                        System.out.println("*************");
                        myPrint("All  tags have been printed out");
                    }
                }
                else if (line.startsWith("a")) {
                    synchronized (mainUI) {

                        (new File(analysisCsvPath)).mkdirs();
                        (new File(analysisChartsPath)).mkdirs();
                        (new File(analysisPdfPath)).mkdirs();

                        myPrint("Analysis will start  soon...");
                        handler.startHandling();
                        myPrint("Analysis finished successfully");
                    }
                }
                else if (line.startsWith("?"))
                    listCommands();
            }
            myPrint("Goodbye!");
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void listCommands(){
        myPrint("List of commands:\n" +
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

}
