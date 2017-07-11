package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.LanguageRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;

import java.util.List;

/**
 * Created by pawel on 07.07.17.
 */

@Controller
public class TryController {


    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private NewspaperRepository newspaperRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @RequestMapping("/")
    public String index() {
        foo();
        return "index";
    }

    public void foo(){
        if (newspaperRepository == null) {
            System.out.println("Lel");
            System.exit(1);
        }

        List<Language> languages = languageRepository.findByName("Polish");
        if (languages.size() == 0) {
            System.out.println("Brak języków");
            System.exit(0);
        }
        System.out.println("Języków znalezionych jest: " + languages.size());
        for (Language language : languages) {
            System.out.println("Język " + language.getId());
            System.out.println(language.getName());
            List<Newspaper> newspapers = language.getNewspapers();
            if (newspapers.size() == 0) {
                System.out.println("Brak gazet dla tego języka");
            } else {
                for (Newspaper newspaper : newspapers) {
                    System.out.println("Gazeta: " + newspaper.getName());
                    List<Feed> feeds = feedRepository.findByNewspaper(newspaper);
                    System.out.println("Feedy:");
                    for (Feed myFeed : feeds) {
                        System.out.println(myFeed.getName() + ": " + myFeed.getSection());
                    }
                }
            }
        }
    }
}
