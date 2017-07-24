package Analyzer.ui;

import Analyzer.secondProject.tagger.ComplexTag;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import Analyzer.model.*;
import Analyzer.controller.AnalysisController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.springframework.http.HttpHeaders.USER_AGENT;
import static Analyzer.ui.GraphHandler.graphCreator;


/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;

    public AnalysisHandler(BufferedReader br) {
        this.br = br;
    }

    public void startHandling() throws IOException {
        String field = "", value = "", fieldName = "";

        System.out.println("Enter the field you'd like to focus on: \n" +
                "\t d -> month and year\n" +
                "\t n -> newspaper title\n" +
                "\t nd -> newspaper and day\n" +
        "\t nm -> newspaper and month\n" +
        "\t r -> create new reports based on these existing\n");
        field = br.readLine();

        if (field.startsWith("d")) {
            fieldName = "month and year (mm-yyyy)";
        } else if (field.startsWith("nd") || field.startsWith("nm")){
            //nothing happens
        } else if (field.startsWith("n")) {
            fieldName = "newspaper title";
        } else if (field.startsWith("r")){
            URL url = new URL("http://localhost:8080/broadAnalysis");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            return;
        }
        System.out.println("Type:\n" +
                "\t?? -> to show all possible values \n" +
                "\t# -> to analyse all values \n" +
                "\t$ -> to enter a value\n");
        value = br.readLine();

        if (value.startsWith("??")) {
            URL url = null;
            if (field.startsWith("d")) {
                url = new URL("http://localhost:8080/dates");
            } else if (field.startsWith("n")) {
                url = new URL("http://localhost:8080/news");
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
            } else if (field.startsWith("nm")) {
                AnalysisController.setIsIteratingOverDates(true);
                url = new URL("http://localhost:8080/analyseNewspaper");
            } else if (field.startsWith("nd")) {
                AnalysisController.setIsIteratingOverDays(true);
                AnalysisController.setIsIteratingOverDates(true);
                url = new URL("http://localhost:8080/analyseNewspaper");
            } else if (field.startsWith("n")) {
                AnalysisController.setIsIteratingOverDates(false);
                url = new URL("http://localhost:8080/analyseNewspaper");
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
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
            }else {
                System.out.println("field: " + field + " - returning...");
                return;
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
        }

    }
}

