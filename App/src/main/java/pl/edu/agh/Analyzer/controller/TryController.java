package pl.edu.agh.Analyzer.controller;

import org.h2.util.New;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.Analyzer.model.*;
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
		String viewName = "foo";
		final long denominator = 1000000000;

		Iterable<Feed> feeds = feedRepository.findAll();
		for (Feed feed : feeds) {
			if (feed.getName().equals("fr_FRA_lmonde_int")) {
				System.out.println("Here it is: " + feed.getNewspaper().getLanguage().getName());
			}
		}
		return viewName;
    }

    @RequestMapping("/bar")
    public String bar() {
        final String viewName = "foo";
        final File file = new File("bar.txt");
		final Date patternDate = new Date(117, 5, 1);

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
        List<PressRelease> pressReleases = (List<PressRelease>)pressReleaseRepository.findAll();
		for (PressRelease pressRelease : pressReleases) {
			if (pressRelease.getDate().before(patternDate)) {
				printWriter.println(pressRelease.getDate() + "; " + pressRelease.getTitle());
				for (Tag tag : pressRelease.getTags()) {
					printWriter.println("\t" + tag.getName() + "\t");
				}
			}
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

        Set<Newspaper> newspapers = newspaperRepository.findByLanguage(language);
        if (newspapers.isEmpty()) {
            System.out.println("Coś się popsuło");
            return errorViewName;
        }
        for (Newspaper newspaper : newspapers) {
            System.out.println("Gazeta: " + newspaper.getName());
            for (Feed feed : newspaper.getFeeds()) {
                System.out.println("\t" + feed.getName());
				for (PressRelease pressRelease : feed.getPressReleases()) {
					System.out.println("\t\t" + pressRelease.getDate() + "; " + pressRelease.getTitle());
				}
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
                printWriter.println("PressReleasów: " + pressReleases.size());
                for (PressRelease pressRelease : pressReleases) {
                    printWriter.println(newspaper.getName() + ": " + feed.getName() + ": " + pressRelease.getDate() + ": " + pressRelease.getTitle());
					Set<Tag> tags = pressRelease.getTags();
					printWriter.print("	Tagi:	");
					for (Tag tag : tags) {
						printWriter.print(tag.getName() + "	");
					}
					printWriter.print("\n");
				}
            }
        }
        return viewName;
    }

	@RequestMapping("/lol")
	public String lol() {
		final String viewName = "foo";
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
			return viewName;
		}
		System.out.println("No to robim");

		Feed feed = feedRepository.findByName("pl_POL_fakt_int");
		if (feed == null) {
			System.out.println("Brak");
			return "myError";
		}
		Set<PressRelease> pressReleases = feed.getPressReleases();
		Date patternDate = DatabaseTryController.convertStringToDate("2017-07-11 13:38:27");

		for (PressRelease pressRelease : pressReleases) {
			if (pressRelease.getDate().compareTo(patternDate) == 0) {
				printWriter.println(patternDate + "; " + pressRelease.getTitle());
			}
		}
		printWriter.close();
		System.out.println("Data is written");
		return viewName;

	}
}