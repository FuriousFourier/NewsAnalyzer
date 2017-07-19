package pl.edu.agh.Analyzer.ui;

import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import pl.edu.agh.Analyzer.model.*;
import pl.edu.agh.Analyzer.controller.AnalysisController;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.springframework.http.HttpHeaders.USER_AGENT;
import static pl.edu.agh.Analyzer.ui.GraphHandler.graphCreator;


/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;
//    private AnalysisController controller;

    public AnalysisHandler(BufferedReader br) {
        this.br = br;
        //      controller = new AnalysisController();
    }

    public void startHandling() throws IOException {
        //graphCreator("Date", "02-2017", null); // zmienic, aby bazowal na wynikach z zapytania dla dat
        //return;
        String field = "", value = "", fieldName = "";

        System.out.println("Enter the field you'd like to focus on: \n" +
                "\t d -> month and year\n" +
                "\t n -> newspaper title\n");
                //"\t c -> country\n" + //needs amending (due to graphHandler)
                //"\t l -> language"); //j.w.
        field = br.readLine();

        if (field.startsWith("d")) {
            fieldName = "month and year (mm-yyyy)";
        } else if (field.startsWith("n")) {
            fieldName = "newspaper title";
        } else if (field.startsWith("c")) {
            fieldName = "country name";
        } else if (field.startsWith("l")) {
            fieldName = "language";
        }
        System.out.println("Type:\n" +
                "\t?? -> to show all possible values \n" +
                "\t % -> to show all possible values, divided into dates (only for newspapers)\n" +
                "\t# -> to analyse all values \n" +
                "\t$ -> to enter a value\n");
        value = br.readLine();
        if (value.startsWith("??")) {
            URL url = null;
            if (field.startsWith("d")) {
                url = new URL("http://localhost:8080/dates");
            } else if (field.startsWith("n")) {
                url = new URL("http://localhost:8080/news");
            } else if (field.startsWith("c")) {
                url = new URL("http://localhost:8080/countr");
            } else if (field.startsWith("l")) {
                url = new URL("http://localhost:8080/langs");
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();

            value = br.readLine();
        } else if (value.startsWith("#")) {
            System.out.println("Redirection to controller...");
            URL url = null;
            if (field.startsWith("d")) {
                url = new URL("http://localhost:8080/analyseDate");
            } else if (field.startsWith("n")) {
                AnalysisController.setIsIteratingOverDates(false);
                url = new URL("http://localhost:8080/analyseNewspaper");
            } else if (field.startsWith("c")) {
                url = new URL("http://localhost:8080/analyseCountry");
            } else if (field.startsWith("l")) {
                url = new URL("http://localhost:8080/analyseLanguage");
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("*************");
            System.out.println("Response Code: " + responseCode);
            System.out.println("*************");

        } else if (value.startsWith("%")) {
            System.out.println("Redirection to controller...");
            URL url = null;
            AnalysisController.setIsIteratingOverDates(true);
            if (field.startsWith("n")) {
                url = new URL("http://localhost:8080/analyseNewspaper");
            } else {
                System.out.println("Sorry, option supported only for newspapers");
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("*************");
            System.out.println("Response Code: " + responseCode);
            System.out.println("*************");
        }
        if (value.startsWith("$")) {
            System.out.println("Redirection to controller...");
            AnalysisController.setIsAskingForValue(true);
            URL url = null;
            if (field.startsWith("d")) {
                System.out.println("-----for dates-----");
                url = new URL("http://localhost:8080/notesDate");
            } else if (field.startsWith("n")) {
                System.out.println("-----for newspapers-----");
                url = new URL("http://localhost:8080/notesNews");
            } else if (field.startsWith("c")) {
                System.out.println("-----for countries-----");
                url = new URL("http://localhost:8080/notesCountr");
            } else if (field.startsWith("l")) {
                System.out.println("-----for languages-----");
                url = new URL("http://localhost:8080/notesLangs");
            } else {
                System.out.println("field: " + field + " - returning...");
                return;
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("*************");
            System.out.println("Response Code: " + responseCode);
            System.out.println("*************");

        }

    }
}

