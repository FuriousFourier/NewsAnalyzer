package pl.edu.agh.Analyzer;

import database.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.controller.DatabaseTryController;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.repository.LanguageRepository;
import pl.edu.agh.Analyzer.util.DbUtil;
import tagger.Tagger;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
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

        while (true) {
            System.out.println("Napisz \"p\" to to zrobię (możesz też napisać \"d\")");
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
            rss.Main.main(tmp);
            System.out.println("Im tagging");
            Tagger.tagNewFeeds(1);
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
            System.err.println("Exception in Tagger");
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
