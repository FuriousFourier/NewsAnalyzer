package Analyzer.ui;

import com.itextpdf.text.DocumentException;
import Analyzer.controller.AnalysisController;

import java.io.*;
import java.text.ParseException;
/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;
    private AnalysisController controller;

    public AnalysisHandler(BufferedReader br, AnalysisController controller) {
        this.br = br;
        this.controller = controller;
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
            controller.analyse();
            return;
        }
        System.out.println("Type:\n" +
                "\t?? -> to show all possible values \n" +
                "\t# -> to analyse all values \n" +
                "\t$ -> to enter a value\n");
        value = br.readLine();

        if (value.startsWith("??")) {
            if (field.startsWith("d")) {
              controller.getPressReleasesSortedByDate();
            } else if (field.startsWith("n")) {
                controller.getAllNewspapers();
            }
            value = br.readLine();
        } else if (value.startsWith("#")) {
            System.out.println("Redirection to controller...");
            if (field.startsWith("d")) {
                controller.analyseByDate();
            } else if (field.startsWith("nm")) {
                controller.setIsIteratingOverDates(true);
                controller.analyseByNewspaper();
            } else if (field.startsWith("nd")) {
                controller.setIsIteratingOverDays(true);
                controller.setIsIteratingOverDates(true);
                controller.analyseByNewspaper();
            } else if (field.startsWith("n")) {
                controller.setIsIteratingOverDates(false);
                controller.analyseByNewspaper();
            }
        }
        if (value.startsWith("$")) {
            System.out.println("Redirection to controller...");
            controller.setIsAskingForValue(true);
            if (field.startsWith("d")) {
                System.out.println("-----for dates-----");
                controller.getPressReleasesByDate();
            } else if (field.startsWith("n")) {
                System.out.println("-----for newspapers-----");
                controller.getPressReleasesByNews();
            }else {
                System.out.println("field: " + field + " - returning...");
                return;
            }
        }

    }
}

