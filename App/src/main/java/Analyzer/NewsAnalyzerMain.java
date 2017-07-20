package Analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import Analyzer.secondProject.tagger.MainTagger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * Created by pawel on 07.07.17.
 */


@SpringBootApplication
public class NewsAnalyzerMain {

    public static Long securityNumber;
    private static ConfigurableApplicationContext configurableApplicationContext;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random(System.currentTimeMillis());
        securityNumber = random.nextLong();
        configurableApplicationContext = SpringApplication.run(NewsAnalyzerMain.class, args);
        MainTagger.initializeMainTagger();

        while (true) {
            System.out.println("Napisz \"p\" to to zrobię (możesz też napisać \"d\" albo \"t\")");
            String line = scanner.nextLine();
            if (line.equals("p")) {
                getNewFeeds();
            } else if (line.equals("q")) {
                System.out.println("Bye");
                System.exit(0);
            } else if (line.equals("d")) {
                URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + securityNumber);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = connection.getResponseCode();
                System.out.println("*************");
                System.out.println("Response Code: " + responseCode);
                System.out.println("*************");
            } else if (line.equals("t")) {
            	MainTagger.tagNewFeedsCurrency(0);
			} else {
                System.out.println("Błędna opcja");
            }
            System.gc();
        }

    }

    private static void getNewFeeds(){
        String [] tmp = new String[0];
        try {
            System.out.println("Im gonna download feeds");
            Analyzer.secondProject.rss.Main.main(tmp);
            System.out.println("Im tagging");
            MainTagger.tagNewFeeds(1);
            System.out.println("Lets go with db");

            URL url = new URL("http://localhost:8080/addThingsToDB?secNum=" + securityNumber);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("*************");
            System.out.println("Response Code: " + responseCode);
            System.out.println("*************");
            System.out.println("Work finished");
        } catch (IOException e) {
            System.err.println("Exception in MainTagger");
            e.printStackTrace();
        }
    }

    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

    public static void setConfigurableApplicationContext(ConfigurableApplicationContext configurableApplicationContext) {
        NewsAnalyzerMain.configurableApplicationContext = configurableApplicationContext;
    }
}
