package pl.edu.agh.Analyzer.ui;

import java.io.BufferedReader;
import java.io.IOException;


/**
 * Created by karolina on 11.07.17.
 */
public class AnalysisHandler {
    private BufferedReader br;

    public AnalysisHandler(BufferedReader br){
        this.br = br;
    }
    public void startHandling() throws IOException {
        String field = "", value = "", fieldName = "";

        MainUI.myPrint("Enter the field you'd like to focus on: " +
                "\t m -> month and year" +
                "\t n -> newspaper title" +
                "\t c -> country" +
                "\t l -> language");
        field = br.readLine();

        if (field.startsWith("m")){
            fieldName = "month and year, mm:yyyy";
        }
        else if (field.startsWith("n")){
            fieldName = "newspaper title";
        }
        else if (field.startsWith("c")){
            fieldName = "country name";
        }
        else if (field.startsWith("l")){
            fieldName = "language";
        }
        MainUI.myPrint("Enter " + fieldName +  " (type ?? to show all possible values):");
        value = br.readLine();
        while (value.startsWith("??")){
            if (field.startsWith("m")){
                listYears();
            }
            else if (field.startsWith("n")){
                listNewspapers();
            }
            else if (field.startsWith("c")){
                listCountries();
            }
            else if (field.startsWith("l")){
                listLanguages();
            }
            value = br.readLine();
        }
        System.out.println("You've chosen field " + fieldName + " and value "+ value);
        System.out.println("All db entries will be fetched soon...");
        //tu bedzie zapytanie
        //tu powstanie graf
    }

    private void graphCreator(){

    }

    private void listNewspapers(){

    }
    private void listCountries(){

    }
    private void listLanguages(){

    }
    private void listYears(){

    }
}
