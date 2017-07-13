package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.LanguageRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;
import pl.edu.agh.Analyzer.repository.PressReleaseRepository;

import java.io.*;
import java.util.Date;
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

    @Autowired
    private PressReleaseRepository pressReleaseRepository;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/foo")
    public String foo() {

        final String viewName = "foo";
        Date patternDate = new Date(116, 7, 8, 0, 0, 0);

        final File file = new File("foo.txt");

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
            return viewName;
        }

        Language language = languageRepository.findByName("English");
        if (language == null) {
            System.out.println("Brak języków");
            System.exit(0);
        }
        printWriter.println("Język " + language.getId());
        printWriter.println(language.getName());
        List<Newspaper> newspapers = language.getNewspapers();
        if (newspapers.size() == 0) {
            printWriter.println("Brak gazet dla tego języka");
        } else {
            for (Newspaper newspaper : newspapers) {
                printWriter.println("Gazeta: " + newspaper.getName());
                List<Feed> feeds = newspaper.getFeeds();
                for (Feed myFeed : feeds) {
                    List<PressRelease> pressReleases = myFeed.getPressReleases();
                    printWriter.println("Notki:");
                    for (PressRelease pressRelease : pressReleases) {
                        Date dateOfCurrent = pressRelease.getDate();
                        if (dateOfCurrent.after(patternDate)) {
                            printWriter.println(dateOfCurrent + ": " + pressRelease.getContent());
                        }
                    }
                }
            }
        }
        language = null;
        printWriter.close();
        System.out.println("Data is written ");
        return viewName;
    }

    @RequestMapping("/bar")
    public String bar() {
        final String viewName = "foo";
        final Date patternDate = new Date(117, 1, 1, 0, 0, 0);
        System.out.println("Pattern Date: " + patternDate);

        final File file = new File("bar.txt");

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
            return viewName;
        }
        System.out.println("No to robim");

        Iterable<PressRelease> pressReleases = pressReleaseRepository.findAll();

        for (PressRelease pressRelease : pressReleases) {
            Date currentReleaseDate = pressRelease.getDate();

            if (currentReleaseDate.after(patternDate)) {
                printWriter.println(currentReleaseDate + ": " + pressRelease.getContent());
            } else {
                //System.out.println(currentReleaseDate + " ");
            }
        }
        System.out.println();
        pressReleases = null;
        printWriter.close();
        System.out.println("Data is written");
        return viewName;

    }

    @GetMapping("/lang")
    public String getLangs() {
        final String viewName = "foo";
        Iterable<Language> languages = languageRepository.findAll();
        System.out.println("Języki:");
        for (Language language : languages) {
            System.out.println(language.getName());
        }
        return viewName;
    }
}