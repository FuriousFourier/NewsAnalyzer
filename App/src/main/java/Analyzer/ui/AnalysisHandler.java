package Analyzer.ui;

import com.itextpdf.text.DocumentException;
import Analyzer.controller.AnalysisController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;
    private AnalysisController controller;

    public AnalysisHandler(BufferedReader br) {
        this.br = br;
    }

    public void startHandling() throws IOException, ParseException, DocumentException {
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
            URL url = new URL("http://localhost:8080/broadAnalysis");controller.analyse();
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

