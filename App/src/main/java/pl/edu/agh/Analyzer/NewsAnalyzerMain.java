package pl.edu.agh.Analyzer;

import database.util.HibernateUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.agh.Analyzer.controller.DatabaseTryController;
import tagger.Tagger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.Scanner;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * Created by pawel on 07.07.17.
 */


@SpringBootApplication
public class NewsAnalyzerMain {

    public static Long securityNumber;


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random(System.currentTimeMillis());
        securityNumber = random.nextLong();
        SpringApplication.run(NewsAnalyzerMain.class, args);

        while (true) {
            System.out.println("Napisz \"p\" to to zrobię (możesz też napisać \"d\")");
            String line = scanner.nextLine();
            if (line.equals("p")) {
                getNewFeeds();
            } else if (line.equals("q")) {
                System.out.println("Cześć");
                System.exit(0);
            } else if (line.equals("d")) {
                URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + securityNumber);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = connection.getResponseCode();
                System.out.println("*************");
                System.out.println("Response Code: " + responseCode);
                System.out.println("*************");
            } else {
                System.out.println("Błędna opcja");
            }
            System.gc();
        }

    }

    private static void getNewFeeds(){
        String [] tmp = new String[0];
        try {
            System.out.println("Pobieram feedy");
            rss.Main.main(tmp);
            System.out.println("Taguję");
            Tagger.main(tmp);
            System.out.println("Lecim z bazą");

            URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + securityNumber);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("*************");
            System.out.println("Response Code: " + responseCode);
            System.out.println("*************");
            System.out.println("Koniec pracy");
        } catch (IOException e) {
            System.err.println("Exception in Tagger");
            e.printStackTrace();
        }
    }

}
