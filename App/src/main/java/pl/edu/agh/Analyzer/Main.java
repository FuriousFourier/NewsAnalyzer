package pl.edu.agh.Analyzer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;

import java.util.List;

/**
 * Created by pawel on 07.07.17.
 */

@SpringBootApplication
public class Main {


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
