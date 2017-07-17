package pl.edu.agh.Analyzer.controller;

import org.h2.util.New;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

        final long denominator = 1000000000;
        List<PressRelease> pressReleasesTmp = new LinkedList<>();

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

        long startDBTime = System.nanoTime();
        System.out.println("Robie zapytanie do bazy");
        Language language = languageRepository.findByName("English");
        long endDBTime = System.nanoTime();
        System.out.println("Skonczylem pobierac z bazy, czas: " + ((endDBTime - startDBTime) / denominator));
        if (language == null) {
            System.out.println("Brak języków");
            return viewName;
        }
        Set<Newspaper> newspapers = language.getNewspapers();
        if (newspapers.size() == 0) {
            printWriter.println("Brak gazet dla tego języka");
        } else {
            for (Newspaper newspaper : newspapers) {
                Set<Feed> feeds = newspaper.getFeeds();
                for (Feed myFeed : feeds) {
                    Set<PressRelease> pressReleases = myFeed.getPressReleases();
                    for (PressRelease pressRelease : pressReleases) {
                        if (pressRelease.getTitle().contains("Best pictures ever")) {
                            pressReleasesTmp.add(pressRelease);
                        }
                    }
                }
            }
        }
        for (PressRelease pressRelease : pressReleasesTmp) {
            printWriter.println(pressRelease.getId() + "; " + pressRelease.getDate() + "; " + pressRelease.getTitle() + "; "
                    + pressRelease.getContent());
            printWriter.println(pressRelease.getTitle().length() + "; " + pressRelease.getDate().getTime() + "; "
                    + pressRelease.getContent().length() + "; " + pressRelease.getTags().size());
            printWriter.println();
        }
        language = null;
        newspapers = null;
        pressReleasesTmp = null;
        System.gc();
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

		Feed feed = feedRepository.findByName("pl_POL_fakt_int");
		if (feed == null) {
			System.out.println("Error lel");
			printWriter.close();
			return "myError";
		}

		Set<PressRelease> pressReleases = feed.getPressReleases();
		for (PressRelease pressRelease : pressReleases) {
			if (pressRelease.getTags().size() != 0)
				printWriter.println(pressRelease.getTitle() + ", " + pressRelease.getTags().size());
		}
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

    @GetMapping("/getPl")
    public String getPl() {
        final String viewName = "foo";
        final String errorViewName = "myError";

        Language language = languageRepository.findByName("Polish");
        if (language == null) {
            System.out.println("Brak języka");
            return errorViewName;
        }

        List<Newspaper> newspapers = newspaperRepository.findByLanguage(language);
        if (newspapers.size() == 0) {
            System.out.println("Coś się popsuło");
            return errorViewName;
        }
        for (Newspaper newspaper : newspapers) {
            System.out.println("Gazeta: " + newspaper.getName());
            for (Feed feed : newspaper.getFeeds()) {
                System.out.println("\t" + feed.getName());
            }
        }
        return viewName;
    }

    @GetMapping("/getFeeds")
    public String getFeedsRequest() {
        final String viewName = "foo";

        Iterable<Feed> feeds = feedRepository.findAll();
        System.out.println("Feedy: ");
        for (Feed feed : feeds) {
            System.out.println(feed.getName());
        }
        return viewName;
    }

    @GetMapping("/getPl/pressreleases")
    public String getPlPressReeases(){
        final String viewName = "foo";
        final String errorViewName = "myError";

        final File file = new File("polishPressReleases.txt");

        file.delete();
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            System.out.println("Coś się popsuło");
            return errorViewName;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Kodowanie nie działa");
            return errorViewName;
        }

        Language language = languageRepository.findByName("Polish");
        if (language == null) {
            System.err.println("Brak języka");
            return errorViewName;
        }
        Set<Newspaper> newspapers = language.getNewspapers();
        for (Newspaper newspaper : newspapers) {
            Set<Feed> feeds = newspaper.getFeeds();
            printWriter.println("Gazeta: " + newspaper.getName() + ", feedów: " + feeds.size());
            for (Feed feed : feeds) {
                Set<PressRelease> pressReleases = feed.getPressReleases();
                System.out.println("PressReleasów: " + pressReleases.size());
                for (PressRelease pressRelease : pressReleases) {
                    printWriter.println(newspaper.getName() + ": " + feed.getName() + ": " + pressRelease.getDate() + ": " + pressRelease.getTitle());
                }
            }
        }
        return viewName;
    }
}