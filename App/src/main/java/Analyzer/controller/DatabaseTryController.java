package Analyzer.controller;

import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import static Analyzer.secondProject.info.FeedInfoContainer.*;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import Analyzer.NewsAnalyzerMain;
import Analyzer.model.*;
import Analyzer.repository.*;
import Analyzer.support.PressReleaseId;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static Analyzer.secondProject.info.FeedInfoContainer.languages;

/**
 * Created by pawel on 12.07.17.
 */

@Controller
public class DatabaseTryController {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private NewspaperRepository newspaperRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private PressReleaseRepository pressReleaseRepository;

    private Map<String, Language> languageMap = new HashMap<>();
    private Map<String, Country> countryMap = new HashMap<>();
    private Map<String, Newspaper> newspaperMap = new HashMap<>();
    private Map<String, Feed> feedMap = new HashMap<>();
    private boolean flushed = true;

    private static Map<PressReleaseId, PressRelease> pressReleaseMap = new HashMap<>();
    private static Map<String, Tag> tagMap = new HashMap<>();

    private static final String GEOMEDIA_FEEDS_FILE_PATH = "../SecondProject/geomedia/Geomedia_extract_AGENDA/Geomedia_extract_AGENDA/";
    private static final String ORG_PATH = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/TaggedFeeds/taggedForOrg/";
    private static final String NEW_FEEDS_PATH_TAGGED = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/TaggedFeeds/taggedForCountry";
    private static final String NEW_FEEDS_PATH = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Feeds";
    private static final String COUNTRY_TAG_FILE_NAME = "Dico_Country_Free.csv";
    private static final String EBOLA_TAG_FILE_NAME = "Dico_Ebola_Free.csv";
    private static final String GEOMEDIA_EBOLA_TAGGED_FILE_NAME = "rss_unique_TAG_country_Ebola.csv";
    private static final String GEOMEDIA_RSS_FILE_NAME = "rss.csv";
    private static final String GEOMEDIA_UNIQUE_FILE_NAME = "rss_unique.csv";
    private static final String ORG_TAGGED_FILE_NAME = "rss_org_tagged.csv";

    public static Date convertStringToDate(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        Date resultDate = null;
        try {
            resultDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultDate;
    }

    @GetMapping("/addThingsToDB")
    public String addSomething(@RequestParam(name = "secNum") Long secNum) throws IOException {
        final long denominator = 1000000000;
        if (!secNum.equals(NewsAnalyzerMain.securityNumber))
            return "forbidden";
        long startTime = System.nanoTime();
        if (languageMap == null) {
            System.out.println("Im creating maps etc.");
            languageMap = new HashMap<>(languages.length);
            countryMap = new HashMap<>();
            newspaperMap = new HashMap<>();
            feedMap = new HashMap<>();
            tagMap = new HashMap<>();
            pressReleaseMap = new HashMap<>();
        }
        if (flushed) {
            languageMap.clear();
            countryMap.clear();
            newspaperMap.clear();
            feedMap.clear();
            tagMap.clear();
            pressReleaseMap.clear();

            //get data from db
            System.out.println("Im getting collections from DB");
            Iterable<Language> languagesFromDb = languageRepository.findAll();
            Iterable<Country> countriesFromDb = countryRepository.findAll();
            Iterable<Newspaper> newspapersFromDb = newspaperRepository.findAll();
            Iterable<Feed> feedsFromDb = feedRepository.findAll();
            Iterable<Tag> tagsFromDb = tagRepository.findAll();
            Iterable<PressRelease> pressReleasesFromDb = pressReleaseRepository.findAll();

            System.out.println("Im gonna put them into my data structures");
            for (Language language : languagesFromDb) {
                languageMap.put(language.getName(), language);
            }
            for (Country country : countriesFromDb) {
                countryMap.put(country.getName(), country);
            }
            for (Newspaper newspaper : newspapersFromDb) {
                newspaperMap.put(newspaper.getName(), newspaper);
            }
            for (Feed feed : feedsFromDb) {
                feedMap.put(feed.getName(), feed);
            }
            for (Tag tag : tagsFromDb) {
                tagMap.put(tag.getName(), tag);
            }
            for (PressRelease pressRelease : pressReleasesFromDb) {
                pressReleaseMap.put(new PressReleaseId(pressRelease.getTitle(), pressRelease.getDate(), pressRelease.getFeed()), pressRelease);
            }

            System.out.println("Lets go with updating database");
            System.out.println("Languages");
            //langs
            for (String languageName : languages) {
                Language language = languageMap.get(languageName);
                if (language == null) {
                    language = new Language();
                    language.setName(languageName);
                    language.setNewspapers(new HashSet<>());
                    languageMap.put(languageName, language);
                }
            }

            System.out.println("Countries");
            //countries
            String tagsAndCountriesFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
            List<String> countries = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 1);
            for (String country1 : countries) {
                Country addingCountry = countryMap.get(country1);
                if (addingCountry == null) {
                    addingCountry = new Country();
                    addingCountry.setName(country1);
                    addingCountry.setNewspapers(new HashSet<>());
                    countryMap.put(country1, addingCountry);
                }
            }

            System.out.println("Newspapers");
            //newspapers
            addNewspapersToDb(newspapersNames, newspapersCountry, newspapersLanguage);
            addNewspapersToDb(nonGeomediaNewspapersNames, nonGeomediaNewspapersCountry, nonGeomediaNewspapersLanguage);

            System.out.println("Tags");
            //tags
            List<String> tags = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 0);
            for (int i = 0; i < tags.size(); i++) {
                Country country = countryMap.get(countries.get(i));
                if (country == null) {
                    continue;
                }
                Tag tag = tagMap.get(tags.get(i));
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tags.get(i));
                    tag.setPressReleases(new HashSet<>());
                }
                tag.setCountry(country);
                country.setTag(tag);
                tagMap.put(tags.get(i), tag);
            }

            System.out.println("Feeds");
            //feeds
            addFeedsToDb("International", feedsNames, newspapersNames);

            //Here we should add pl feeds
            addFeedsToDb("Polish", nonGeomediaFeedsNames, nonGeomediaNewspapersNames);

            System.out.println("PressReleases");
            //pressReleases
            addPressReleasesToDBNewWay(GEOMEDIA_FEEDS_FILE_PATH, false);
            addPressReleasesToDBNewWay(NEW_FEEDS_PATH, true);

			List<String> myTitles = new ArrayList<>(pressReleaseMap.values().size());
			for (PressRelease pressRelease : pressReleaseMap.values()) {
				myTitles.add(pressRelease.getTitle());
			}
			Collections.sort(myTitles);
			File file = new File("titles.txt");
			file.delete();
			PrintWriter printWriter = new PrintWriter(file, "UTF-8");
			for (String s : myTitles) {
				printWriter.println(s);
			}
			printWriter.close();
			System.out.println(pressReleaseMap.size() + " " + pressReleaseMap.values().size() + " " + myTitles.size());

			System.out.println("Linking table");
            //linking table
            addPressReleasesTagsDataNewWay(NEW_FEEDS_PATH_TAGGED, 2, 1);

            System.out.println("PressReleases ebola tags");
            //pressReleaseMap ebola tags
            String[] ebolaFilePaths = new String[feedsNames.length];
            for (int i = 0; i < feedsNames.length; i++) {
                ebolaFilePaths[i] = GEOMEDIA_FEEDS_FILE_PATH + feedsNames[i] + "/rss_unique_TAG_country_Ebola.csv";
            }

            for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
                String filePath = ebolaFilePaths[i1];
                List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3);
                List<String> myEbolaTags = ReaderCsvFiles.readAtPosition(filePath, 6);
                List<String> dates = ReaderCsvFiles.readAtPosition(filePath, 2);
                Feed feed = feedMap.get(feedsNames[i1]);
                if (feed == null) {
                    continue;
                }

                Tag ebolaTag = tagMap.get("EBOLA");
                if (ebolaTag != null) {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myEbolaTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = pressReleaseMap.get(new PressReleaseId(titles.get(i), convertStringToDate(dates.get(i)), feed));
                        if (pressRelease == null || !pressRelease.getFeed().equals(feed)) {
                            continue;
                        }
                        pressRelease.getTags().add(ebolaTag);
                        ebolaTag.getPressReleases().add(pressRelease);
                    }
                }
            }

            System.gc();
            System.out.println("OrganizationFeed tags");
            //organization feed tags
			String[] organizationFilePaths = new String[2 * feedsNames.length];
			for (int i = 0; i < feedsNames.length; i++) {
                organizationFilePaths[2*i] = ORG_PATH + "SHORT/" + feedsNames[i] + ".csv";
				organizationFilePaths[2 * i + 1] = ORG_PATH + feedsNames[i] + ".csv";
			}
            for (int i1 = 0; i1 < organizationFilePaths.length; i1++) {
                String filePath = organizationFilePaths[i1];

                List<String> titles;
                List <String> myOrgTags;
				List<String> dates;
				try {
					titles = ReaderCsvFiles.readAtPosition(filePath, 3);
					myOrgTags = ReaderCsvFiles.readAtPosition(filePath, 4);
					dates = ReaderCsvFiles.readAtPosition(filePath, 1);
				} catch (FileNotFoundException e) {
					System.err.println("File " + filePath + " not found");
					break;
				}
				Feed feed = feedMap.get(feedsNames[i1]);
                if (feed == null) {
                    continue;
                }
                try {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myOrgTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        Date date = convertStringToDate(dates.get(i));
                        PressRelease pressRelease = pressReleaseMap.get(new PressReleaseId(titles.get(i), date, feed));
                        Tag tag = tagMap.get(myOrgTags.get(i));
                        if (pressRelease == null || tag == null || !pressRelease.getFeed().equals(feed)) {
                            continue;
                        }
                        pressRelease.getTags().add(tag);
                        tag.getPressReleases().add(pressRelease);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

		System.out.println("I will be saving this to DB now");
        flushed = false;
        System.out.println("Countries size: " + countryMap.values().size());
        System.out.println("Feeds size: " + feedMap.values().size());
        System.out.println("Languages size: " + languageMap.values().size());
        System.out.println("Newspapers size: " + newspaperMap.values().size());
        System.out.println("Tags size: " + tagMap.values().size());
        System.out.println("PressReleases size: " + pressReleaseMap.values().size());
        long startDBTime = System.nanoTime();

        Collection<Country> countryCollection = countryMap.values();
        Collection<Feed> feedCollection = feedMap.values();
        Collection<Language> languageCollection = languageMap.values();
        Collection<Newspaper> newspaperCollection = newspaperMap.values();
        Collection<Tag> tagCollection = tagMap.values();
        Collection<PressRelease> pressReleaseCollection = pressReleaseMap.values();

        long collectionTime = System.nanoTime();
        System.out.println("I created collections, time: " + ((collectionTime - startDBTime) / denominator));

        languageRepository.save(languageCollection);
        long languageTime = System.nanoTime();
        System.out.println("I saved languages, time:" + ((languageTime - collectionTime) / denominator));

        countryRepository.save(countryCollection);
        long countryTime = System.nanoTime();
        System.out.println("I saved countries, time: " + ((countryTime - languageTime) / denominator));

        newspaperRepository.save(newspaperCollection);
        long newspaperTime = System.nanoTime();
        System.out.println("I saved newspapers, time: " + ((newspaperTime - countryTime) / denominator));

        tagRepository.save(tagCollection);
        long tagTime = System.nanoTime();
        System.out.println("I saved tags, time: " + ((tagTime - newspaperTime) / denominator));

        feedRepository.save(feedCollection);
        long feedTime = System.nanoTime();
        System.out.println("I saved feeds, time: " + ((feedTime - tagTime) / denominator));

        pressReleaseRepository.save(pressReleaseCollection);
        long pressReleaseTime = System.nanoTime();
        System.out.println("I saved pressReleases, time: " + ((pressReleaseTime - feedTime) / denominator));

        flushed = true;
        System.out.println("I have saved");
        this.feedMap.clear();
        this.countryMap.clear();
        this.languageMap.clear();
        this.newspaperMap.clear();
        tagMap.clear();
        pressReleaseMap.clear();
        long currentTime = System.nanoTime();
        System.out.println("Database updated, time: " + ((currentTime - startDBTime) / 1000000000) + " sec");
        System.out.println("Execution time: " + ((currentTime - startTime) / 1000000000) + " sec");
        System.gc();
        return "foo";
    }

    private void extractFeedsFilesAndSaveNewWay(File file, boolean newFeeds) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            System.out.println("Null for " + file.getAbsolutePath());
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().contains(".csv") && !f.getName().equals(GEOMEDIA_RSS_FILE_NAME) && !f.getName().equals(GEOMEDIA_EBOLA_TAGGED_FILE_NAME)
                        && !f.getName().equals(EBOLA_TAG_FILE_NAME)
                        && !f.getName().equals(COUNTRY_TAG_FILE_NAME)) {

                    String filePath = f.getAbsolutePath();
                    List<String> feedsNames = null;
                    List<String> dates = null;
                    List<String> titles = null;
                    List<String> contents = null;
                    if (newFeeds) {
                        feedsNames = ReaderCsvFiles.readAtPosition(filePath, 0);
                        dates = ReaderCsvFiles.readAtPosition(filePath, 1);
                        titles = ReaderCsvFiles.readAtPosition(filePath, 2);
                        contents = ReaderCsvFiles.readAtPosition(filePath, 3);
                    } else {
                        feedsNames = ReaderCsvFiles.readAtPosition(filePath, 1);
                        dates = ReaderCsvFiles.readAtPosition(filePath, 2);
                        titles = ReaderCsvFiles.readAtPosition(filePath, 3);
                        contents = ReaderCsvFiles.readAtPosition(filePath, 4);
                    }

                    for (int i = 0; i < feedsNames.size(); i++) {
                        Feed feed = feedMap.get(feedsNames.get(i));
                        if (feed == null) {
                            System.out.println("There is no feed called \" " + feedsNames.get(i) + "\", filename: " + filePath);
                            continue;
                        }
                        try {
                            Date date = convertStringToDate(dates.get(i));
                            PressReleaseId pressReleaseId = new PressReleaseId(titles.get(i), date, feed);
                            PressRelease pressRelease = pressReleaseMap.get(pressReleaseId);
                            if (pressRelease == null){
                                pressRelease = new PressRelease();
                                pressRelease.setFeed(feed);
                                pressRelease.setTitle(titles.get(i));
                                pressRelease.setDate(date);
                                pressRelease.setContent(contents.get(i));
                                feed.getPressReleases().add(pressRelease);
                                pressRelease.setTags(new HashSet<>());
                                pressReleaseMap.put(pressReleaseId, pressRelease);
                            }
                        } catch (DataException e) {
                            e.printStackTrace();
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("Index out of bound: " + e.getMessage());
                        }

                    }
                }
            } else if (f.isDirectory()) {
                extractFeedsFilesAndSaveNewWay(f, newFeeds);
            }
        }
    }

    private void addPressReleasesToDBNewWay(String path, boolean newFeeds) throws IOException {
        File file = new File(path);
        extractFeedsFilesAndSaveNewWay(file, newFeeds);
    }

    private void addPressReleasesTagsDataNewWay(String path, int titlesPosition, int datePosition) throws IOException {
        File file = new File(path);
        extractTaggedFeedsFilesAndSaveNewWay(file, titlesPosition, datePosition);

    }

    private void extractTaggedFeedsFilesAndSaveNewWay(File file, int titlesPosition, int datePosition) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            System.out.println("There is null here, file: " + file.getAbsolutePath());
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                if ((f.getName().contains(".csv") && !f.getName().equals(GEOMEDIA_RSS_FILE_NAME)
                        && !f.getName().equals(EBOLA_TAG_FILE_NAME)
                        && !f.getName().equals(COUNTRY_TAG_FILE_NAME)
                        && !f.getName().equals(GEOMEDIA_UNIQUE_FILE_NAME)) || (f.getName().equals(ORG_TAGGED_FILE_NAME))) {
                    String filePath = f.getAbsolutePath();
                    List<String> titles = ReaderCsvFiles.readAtPosition(filePath, titlesPosition);
                    List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 4);
                    List<String> dates = ReaderCsvFiles.readAtPosition(filePath, datePosition);

                    //ATTENTION - PARSING FILE SHOULD BE NAMED LIKE FEED NAME !!!
                    String feedName = f.getName().split("\\.")[0];
                    Feed feed = feedMap.get(feedName);
                    if (feed == null) {
                        System.out.println("There is no feed called \"" + feedName + "\"");
                        continue;
                    }
                    for (int i = 0; i < titles.size(); i++) {
                        if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        Date date = convertStringToDate(dates.get(i));
                        PressReleaseId pressReleaseId = new PressReleaseId(titles.get(i), date, feed);
                        PressRelease pressRelease = pressReleaseMap.get(pressReleaseId);
                        Tag tag = tagMap.get(tags.get(i));
                        if (pressRelease == null || tag == null || !pressRelease.getFeed().equals(feed)) {
							continue;
                        }
                        pressRelease.getTags().add(tag);
                        tag.getPressReleases().add(pressRelease);
                    }
                }

            } else if (f.isDirectory()) {
                extractTaggedFeedsFilesAndSaveNewWay(f, titlesPosition, datePosition);
            }
        }
    }

    private void addFeedsToDb(String section, String[] myFeedNames, String[] myNewspapersNames) {
        for (int i = 0; i < myFeedNames.length; i++) {
            Newspaper newspaper = newspaperMap.get(myNewspapersNames[i]);
            if (newspaper == null) {
                continue;
            }
            Feed addingFeed = feedMap.get(myFeedNames[i]);
            if (addingFeed == null) {
                addingFeed = new Feed();
                addingFeed.setName(myFeedNames[i]);
                addingFeed.setPressReleases(new HashSet<>());
            }
            addingFeed.setNewspaper(newspaper);
            addingFeed.setSection(section);
            newspaper.getFeeds().add(addingFeed);
            feedMap.put(myFeedNames[i], addingFeed);
        }
    }

    private void addNewspapersToDb(String[] myNewspapersNames, String[] myNewspapersCountry, String[] myNewspapersLanguage) {
        for (int i = 0; i < myNewspapersNames.length; i++) {
            Country country = countryMap.get(myNewspapersCountry[i]);
            Language language = languageMap.get(myNewspapersLanguage[i]);
            if (language == null || country == null) {
                continue;
            }
            Newspaper newspaper = newspaperMap.get(myNewspapersNames[i]);
            if (newspaper == null) {
                newspaper = new Newspaper();
                newspaper.setName(myNewspapersNames[i]);
                newspaper.setFeeds(new HashSet<>());
            }
            newspaper.setCountry(country);
            newspaper.setLanguage(language);
            language.getNewspapers().add(newspaper);
            country.getNewspapers().add(newspaper);
            newspaperMap.put(myNewspapersNames[i], newspaper);
        }
    }
}
