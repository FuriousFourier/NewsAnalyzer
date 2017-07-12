package pl.edu.agh.Analyzer;

import database.util.HibernateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;
import tagger.Tagger;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by pawel on 07.07.17.
 */


@SpringBootApplication
public class Main {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        SpringApplication.run(Main.class, args);
        while (true) {
            System.out.println("Napisz \"p\" to to zrobię");
            String line = scanner.nextLine();
            if (line.equals("p")) {
                getNewFeeds();
            } else if (line.equals("q")) {
                System.out.println("Cześć");
                System.exit(0);
            } else {
                System.out.println("Błędna opcja");
            }
        }

    }

    private static void getNewFeeds(){
        String [] tmp = new String[0];
        try {
            rss.Main.main(tmp);
            Tagger.main(tmp);
            HibernateUtil.main(tmp);
            System.out.println("Finisz");
        } catch (IOException e) {
            System.err.println("Exception in Tagger");
            e.printStackTrace();
        }
    }

}
