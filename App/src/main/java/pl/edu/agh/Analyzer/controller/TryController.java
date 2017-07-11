package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.LanguageRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;

import java.io.*;
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

        final File file = new File("lol.txt");

        file.delete();
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            System.out.println("Coś się popsuło");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Kodowanie nie działa");
        }
        if (printWriter == null) {
            System.out.println("Kapa");
            return;
        }

        List<Language> languages = languageRepository.findByName("English");
        if (languages.size() == 0) {
            System.out.println("Brak języków");
            System.exit(0);
        }
        printWriter.println("Języków znalezionych jest: " + languages.size());
        for (Language language : languages) {
            printWriter.println("Język " + language.getId());
            printWriter.println(language.getName());
            List<Newspaper> newspapers = language.getNewspapers();
            if (newspapers.size() == 0) {
                printWriter.println("Brak gazet dla tego języka");
            } else {
                for (Newspaper newspaper : newspapers) {
                    printWriter.println("Gazeta: " + newspaper.getName());
                    List<Feed> feeds = newspaper.getFeeds();
                    printWriter.println("Feedy:");
                    for (Feed myFeed : feeds) {
                        printWriter.println(myFeed.getName() + ": " + myFeed.getSection());
                        List<PressRelease> pressReleases = myFeed.getPressReleases();
                        printWriter.println("Notki:");
                        for (PressRelease pressRelease : pressReleases) {
                            printWriter.println(pressRelease.getDate() + ": " + pressRelease.getContent());
                        }
                    }
                }
            }
        }
        languages = null;
        printWriter.close();
    }
}
