package pl.edu.agh.Analyzer.controller;

import csv.reader.ReaderCsvFiles;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.Analyzer.NewsAnalyzerMain;
import pl.edu.agh.Analyzer.model.*;
import pl.edu.agh.Analyzer.repository.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private Map<String, Language> languagesToDb;
    private Map<String, Country> countriesToDb;
    private Map<String, Newspaper> newspapersToDb;
    private Map<String, Feed> feedsToDb;
    private List<PressRelease> pressReleasesToDb;
    private boolean flushed = true;

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
    private static String[] newspapersNames = {
            "South China Morning Post",
            "Le Monde",
            "The Times of India",
            "El Universal",
            "The New York Times",
            "The Australian",
            "Herald Sun",
            "The Star",
            "China Daily",
            "Daily Telegraph",
            "The Guardian",
            "Hindustan Times",
            "Japan Times",
            "Times of Malta",
            "The Star(malaise)",
            "This Day",
            "New Zealand Herald",
            "The News International",
            "Today",
            "Washington Post",
            "Chronicle",
            "Le Nacion",
            "La Razon",
            "La patria",
            "El mercurio",
            "La tercera",
            "El periodico de Catalunya",
            "El Pais",
            "La Jordana (Mex)",
            "El Universal(MEX)",
            "El Universal",
            "Dernière Heure",
            "Le soir",
            "Le Journal de Montreal",
            "El Watan",
            "LExpression",
            "Le Parisien"
    };

    private static String[] feedsNames = {
            "fr_FRA_lmonde_int",
            "en_CHN_mopost_int",
            "en_IND_tindia_int",
            "es_MEX_univer_int",
            "en_USA_nytime_int",
            "en_AUS_austra_int",
            "en_AUS_hersun_int",
            "en_CAN_starca_int",
            "en_CHN_chinad_int",
            "en_GBR_dailyt_int",
            "en_GBR_guardi_int",
            "en_IND_hindti_int",
            "en_JPN_jatime_int",
            "en_MLT_tmalta_int",
            "en_MYS_starmy_int",
            "en_NGA_thiday_int",
            "en_NZL_nzhera_int",
            "en_PAK_newint_int",
            "en_SGP_twoday_int",
            "en_USA_wapost_int",
            "en_ZWE_chroni_int",
            "es_ARG_nacion_int",
            "es_BOL_larazo_int",
            "es_BOL_patria_int",
            "es_CHL_mercur_int",
            "es_CHL_tercer_int",
            "es_ESP_catalu_int",
            "es_ESP_elpais_int",
            "es_MEX_jormex_int",
            "es_MEX_univer_int",
            "es_VEN_univer_int",
            "fr_BEL_derheu_int",
            "fr_BEL_lesoir_int",
            "fr_CAN_jmontr_int",
            "fr_DZA_elwata_int",
            "fr_DZA_xpress_int",
            "fr_FRA_lepari_int",
    };

    private static final String[] newspapersCountry = {
            "China",
            "France",
            "India",
            "Mexico",
            "United States of America",
            "Australia",
            "Australia",
            "Canada",
            "China",
            "United Kingdom",
            "United Kingdom",
            "India",
            "Japan",
            "Malta",
            "Malaysia",
            "Nigeria",
            "New Zealand",
            "Pakistan",
            "Singapore",
            "United States of America",
            "Zimbabwe",
            "Argentina",
            "Bolivia",
            "Bolivia",
            "Chile",
            "Chile",
            "Spain",
            "Spain",
            "Mexico",
            "Mexico",
            "Venezuela",
            "Belgium",
            "Belgium",
            "Canada",
            "Algeria",
            "Algeria",
            "France"
    };
    private static final String[] newspapersLanguage = {
            "English",
            "French",
            "English",
            "Spanish",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "English",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "Spanish",
            "French",
            "French",
            "French",
            "French",
            "French",
            "French"
    };

    private static final String[] languages = {
            "English",
            "Spanish",
            "French",
            "Polish"
    };

    //private static List<PressRelease> pressReleases;
    private static Map<String, PressRelease> pressReleases;
    private static Map<String, Tag> tagMap;

    private static Date convertStringToDate(String date){
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
    public String  addSomething(@RequestParam(name = "secNum") Long secNum) throws IOException {
        if (!secNum.equals(NewsAnalyzerMain.securityNumber))
            return "forbidden";
        long startTime = System.nanoTime();
        if (flushed) {
            languagesToDb = new HashMap<>(languages.length);
            countriesToDb = new HashMap<>();
            newspapersToDb = new HashMap<>();
            feedsToDb = new HashMap<>();
            pressReleasesToDb = new LinkedList<>();

            System.out.println("Langs");
            //langs
            for (String languageName : languages) {
                Language language = new Language();
                language.setName(languageName);
                language.setNewspapers(new LinkedList());
                languagesToDb.put(languageName, language);
            }

            System.out.println("Countries");
            //countries
            String tagsAndCountriesFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
            List<String> countries = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 1, '\t');
            for (String country1 : countries) {
                Country addingCountry = new Country();
                addingCountry.setName(country1);
                addingCountry.setNewspapers(new LinkedList<>());
                countriesToDb.put(country1, addingCountry);
            }

            System.out.println("Newspapers");
            //newspapers
            for (int i = 0; i < newspapersNames.length; i++) {
                Country country = countriesToDb.get(newspapersCountry[i]);
                Language language = languagesToDb.get(newspapersLanguage[i]);
                if (language == null || country == null) {
                    continue;
                }
                Newspaper newspaper = new Newspaper();
                newspaper.setName(newspapersNames[i]);
                newspaper.setCountry(country);
                newspaper.setLanguage(language);
                newspaper.setFeeds(new LinkedList<>());
                language.getNewspapers().add(newspaper);
                country.getNewspapers().add(newspaper);
                newspapersToDb.put(newspapersNames[i], newspaper);
            }

            System.out.println("Tags");
            //tags
            tagMap = new HashMap<>();
            List<String> tags = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 0, '\t');
            for (int i = 0; i < tags.size(); i++) {
                Country country = countriesToDb.get(countries.get(i));
                if (country == null) {
                    continue;
                }
                Tag tag = new Tag();
                tag.setName(tags.get(i));
                tag.setCountry(country);
                tag.setPressReleases(new LinkedList<>());
                country.setTag(tag);
                tagMap.put(tags.get(i), tag);
            }

            System.out.println("Feeds");
            //feeds
            String intSection = "International";
            for (int i = 0; i < feedsNames.length; i++) {
                Newspaper newspaper = newspapersToDb.get(newspapersNames[i]);
                if (newspaper == null) {
                    continue;
                }
                Feed addingFeed = new Feed();
                addingFeed.setName(feedsNames[i]);
                addingFeed.setNewspaper(newspaper);
                addingFeed.setSection(intSection);
                addingFeed.setPressReleases(new LinkedList());
                newspaper.getFeeds().add(addingFeed);
                feedsToDb.put(feedsNames[i], addingFeed);
            }

            System.out.println("PressReleases");
            //pressReleases
            pressReleases = new HashMap<>();
            addPressReleasesToDBNewWay(GEOMEDIA_FEEDS_FILE_PATH, false);
            addPressReleasesToDBNewWay(NEW_FEEDS_PATH, true);

            System.out.println("Linking table");
            //linking table
            addPressReleasesTagsDataNewWay(NEW_FEEDS_PATH_TAGGED, 2);

            System.out.println("PressReleases ebola tags");
            //pressReleases ebola tags
            String[] ebolaFilePaths = new String[feedsNames.length];
            for (int i = 0; i < feedsNames.length; i++) {
                ebolaFilePaths[i] = GEOMEDIA_FEEDS_FILE_PATH + feedsNames[i] + "/rss_unique_TAG_country_Ebola.csv";
                System.out.println(ebolaFilePaths[i]);
            }
            for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
                String filePath = ebolaFilePaths[i1];
                List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
                List<String> myEbolaTags = ReaderCsvFiles.readAtPosition(filePath, 6, '\t');
                System.err.println("#################################");
                System.err.println(filePath);
                System.err.println("#################################");
                Feed feed = feedsToDb.get(feedsNames[i1]);
                if (feed == null) {
                    continue;
                }
                pressReleases.clear();
                for (PressRelease pressRelease : feed.getPressReleases()) {
                    pressReleases.put(pressRelease.getTitle(), pressRelease);
                }
                if (pressReleases.size() == 0) {
                    System.out.println("Baaaaad");
                    continue;
                }

                Tag ebolaTag = tagMap.get("EBOLA");
                if (ebolaTag != null) {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myEbolaTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = pressReleases.get(titles.get(i));
                        if (pressRelease == null) {
                            continue;
                        }
                        pressRelease.getTags().add(ebolaTag);
                        ebolaTag.getPressReleases().add(pressRelease);
                    }
                }
            }

            System.out.println("OrganizationFeed tags");
            //organization feed tags
            for (int i = 0; i < feedsNames.length; i++) {
                ebolaFilePaths[i] = ORG_PATH + "SHORT/" + feedsNames[i] + "/rss_org_tagged.csv";
                System.out.println(ebolaFilePaths[i]);
            }
            for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
                String filePath = ebolaFilePaths[i1];
                List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
                List<String> myOrgTags = ReaderCsvFiles.readAtPosition(filePath, 4, '\t');
                System.err.println("#################################");
                System.err.println(filePath);
                System.err.println("#################################");
                Feed feed = feedsToDb.get(feedsNames[i1]);
                if (feed == null) {
                    continue;
                }
                pressReleases.clear();
                for (PressRelease pressRelease : feed.getPressReleases()) {
                    pressReleases.put(pressRelease.getTitle(), pressRelease);
                }
                try {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myOrgTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = pressReleases.get(titles.get(i));
                        Tag tag = tagMap.get(myOrgTags.get(i));
                        if (pressRelease == null || tag == null) {
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
        System.out.println("Countries size: " + countriesToDb.values().size());
        System.out.println("Feeds size: " + feedsToDb.values().size());
        System.out.println("Languages size: " + languagesToDb.values().size());
        System.out.println("Newspapers size: " + newspapersToDb.values().size());
        System.out.println("Tags size: " + tagMap.values().size());
        System.out.println("PressReleases size: " + pressReleasesToDb.size());
        long startDBTime = System.nanoTime();
        Collection<Country> countryCollection = countriesToDb.values();
        Collection<Feed> feedCollection = feedsToDb.values();
        Collection<Language> languageCollection = languagesToDb.values();
        Collection<Newspaper> newspaperCollection = newspapersToDb.values();
        Collection<Tag> tagCollection = tagMap.values();
        countryRepository.save(countryCollection);
        feedRepository.save(feedCollection);
        languageRepository.save(languageCollection);
        newspaperRepository.save(newspaperCollection);
        pressReleaseRepository.save(pressReleasesToDb);
        tagRepository.save(tagCollection);
        flushed = true;
        System.out.println("I have saved");
        this.feedsToDb = null;
        this.countriesToDb = null;
        this.languagesToDb = null;
        this.newspapersToDb = null;
        this.pressReleasesToDb = null;
        tagMap = null;
        pressReleases = null;
        long currentTime = System.nanoTime();
        System.out.println("Czas zapisu do bazy: " + ((currentTime - startDBTime)/1000000000) + " s");
        System.out.println("Czas wykonania: " + ((currentTime - startTime)/1000000000) + " s");
        return "foo";
    }

    private void extractFeedsFilesAndSaveNewWay(File file, boolean newFeeds) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            System.out.println("Null dla " + file.getAbsolutePath());
            return;
        }
        for (File f: files){
            if (f.isFile()) {
                if (f.getName().contains(".csv") && !f.getName().equals(GEOMEDIA_RSS_FILE_NAME) && !f.getName().equals(GEOMEDIA_EBOLA_TAGGED_FILE_NAME)
                        && !f.getName().equals(EBOLA_TAG_FILE_NAME)
                        && !f.getName().equals(COUNTRY_TAG_FILE_NAME)) {

                    String filePath = f.getAbsolutePath();
                    System.out.println(filePath);
                    List<String> feedsNames = null;
                    List<String> dates = null;
                    List<String> titles = null;
                    List<String> contents = null;
                    if (newFeeds) {
                        feedsNames = ReaderCsvFiles.readAtPosition(filePath,0, ' ');
                        dates = ReaderCsvFiles.readAtPosition(filePath, 1, ' ');
                        titles = ReaderCsvFiles.readAtPosition(filePath, 2, ' ');
                        contents = ReaderCsvFiles.readAtPosition(filePath, 3, ' ');
                    }
                    else {
                        feedsNames = ReaderCsvFiles.readAtPosition(filePath,1, '\t');
                        dates = ReaderCsvFiles.readAtPosition(filePath, 2, '\t');
                        titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
                        contents = ReaderCsvFiles.readAtPosition(filePath, 4, '\t');
                    }

                    for (int  i = 0; i < feedsNames.size(); i++){
                        Feed feed = feedsToDb.get(feedsNames.get(i));
                        if (feed == null){
                            continue;
                        }
                        try {
                            PressRelease pressRelease = new PressRelease();
                            pressRelease.setFeed(feed);
                            pressRelease.setTitle(titles.get(i));
                            pressRelease.setDate(convertStringToDate(dates.get(i)));
                            pressRelease.setContent(contents.get(i));
                            feed.getPressReleases().add(pressRelease);
                            pressRelease.setTags(new LinkedList<>());
                            pressReleases.put(titles.get(i), pressRelease);
                            pressReleasesToDb.add(pressRelease);
                        }catch (DataException e){
                            e.printStackTrace();
                        }catch (IndexOutOfBoundsException e){
                            System.err.println("Index out of bound: " + e.getMessage());
                        }

                    }
                }
            }
            else if (f.isDirectory()){
                extractFeedsFilesAndSaveNewWay(f, newFeeds);
            }
        }
    }

    private void addPressReleasesToDBNewWay(String path, boolean newFeeds) throws IOException {
        File file = new File(path);
        extractFeedsFilesAndSaveNewWay(file, newFeeds);
    }

    private void addPressReleasesTagsDataNewWay(String path, int titlesPosition) throws IOException {
        File file = new File(path);
        extractTaggedFeedsFilesAndSaveNewWay(file, titlesPosition);

    }

    private void extractTaggedFeedsFilesAndSaveNewWay(File file, int titlesPosition) throws IOException {
        File[] files = file.listFiles();
        //assert files != null;
        if (files == null) {
            System.out.println("Tu jest null, dla file: " + file.getAbsolutePath());
        }
        for (File f : files) {
            if (f.isFile()) {
                if ((f.getName().contains(".csv") && !f.getName().equals(GEOMEDIA_RSS_FILE_NAME)
                        && !f.getName().equals(EBOLA_TAG_FILE_NAME)
                        && !f.getName().equals(COUNTRY_TAG_FILE_NAME)
                        && !f.getName().equals(GEOMEDIA_UNIQUE_FILE_NAME)) || (f.getName().equals(ORG_TAGGED_FILE_NAME))) {
                    String filePath = f.getAbsolutePath();
                    System.out.println(filePath);
                    List<String> titles = ReaderCsvFiles.readAtPosition(filePath, titlesPosition, '\t');
                    List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 4, '\t');

                    System.err.println("#################################");
                    System.err.println(filePath);
                    System.err.println("#################################");
                    //ATTENTION - PARSING FILE SHOULD BE NAMED LIKE FEED NAME !!!
                    String feedName = f.getName().split("\\.")[0];
                    System.out.println(feedName);
                    Feed feed = feedsToDb.get(feedName);
                    if (feed == null) {
                        continue;
                    }
                    if (feed.getPressReleases().size() == 0) {
                        continue;
                    }
                    pressReleases.clear();
                    for (PressRelease pressRelease : feed.getPressReleases()) {
                        pressReleases.put(pressRelease.getTitle(), pressRelease);
                    }
                    if (tagMap == null || tagMap.size() == 0) {
                        throw new IOException("AAAAAAAAA");
                    }
                    for (int i = 0; i < titles.size(); i++) {
                        if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = pressReleases.get(titles.get(i));
                        Tag tag = tagMap.get(tags.get(i));
                        if (pressRelease == null || tag == null){
                            continue;
                        }
                        pressRelease.getTags().add(tag);
                        tag.getPressReleases().add(pressRelease);
                    }
                }

            }
            else if (f.isDirectory()) {
                extractTaggedFeedsFilesAndSaveNewWay(f, titlesPosition);
            }
        }
    }
}
