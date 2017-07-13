package pl.edu.agh.Analyzer.controller;

import csv.reader.ReaderCsvFiles;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    private static List<PressRelease> pressReleases;
    private static List<Tag> tagsList;

    @GetMapping("/addThingsOldWay")
    public String addThingsToDB() throws IOException {
        System.out.println("Im going old way");
        addLanguagesToDB();
        addCountriesToDB();
        addNewspapersToDB();
        addTagsToDb();
        addFeedsDataToDB();
        addPressReleasesToDB(GEOMEDIA_FEEDS_FILE_PATH, false);
        addPressReleasesToDB(NEW_FEEDS_PATH, true);
        addPressReleasesTagsData(NEW_FEEDS_PATH_TAGGED, 2);
        addPressReleasesEbolaTags();
        addOrganizationFeedsTagged();
        return "foo";
    }

    public void addLanguagesToDB(){
        for (String language: languages) {
            Language lg = languageRepository.findByName(language);
            if (lg == null) {
                lg = new Language();
            }
            lg.setName(language);
            lg.setNewspapers(new ArrayList<Newspaper>());
            languageRepository.save(lg);
        }
    }

    public void addNewspapersToDB(){

        for (int i = 0; i < newspapersNames.length; i++){
            Country country;
            Language language;
            Newspaper newspaper = newspaperRepository.findByName(newspapersNames[i]);
            if (newspaper == null) {
                country = countryRepository.findByName(newspapersCountry[i]);
                language = languageRepository.findByName(newspapersLanguage[i]);
                if (country == null || language == null){
                    continue;
                }
                newspaper = new Newspaper();
                newspaper.setName(newspapersNames[i]);
                newspaper.setCountry(country);
                newspaper.setLanguage(language);
                newspaper.setFeeds(new ArrayList<>());
                language.getNewspapers().add(newspaper);
                country.getNewspapers().add(newspaper);
                newspaperRepository.save(newspaper);
                languageRepository.save(language);
                countryRepository.save(country);
            }
        }
    }

    public void addCountriesToDB() throws IOException {
        String filePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
        List<String> countries = ReaderCsvFiles.readAtPosition(filePath, 1, '\t');
        for (int i = 0; i < countries.size(); i++){
            Country addingCountry = new Country();
            addingCountry.setName(countries.get(i));
            System.out.println(countries.get(i));
            addingCountry.setNewspapers(new ArrayList<>());
            countryRepository.save(addingCountry);
        }
    }

    public void addTagsToDb() throws IOException {
        String filePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
        List<String> countries = ReaderCsvFiles.readAtPosition(filePath, 1, '\t');
        List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 0, '\t');
        for (int i = 0; i < tags.size(); i++){
            Country country = countryRepository.findByName(countries.get(i));
            Tag tag = new Tag();
            tag.setName(tags.get(i));
            if (country == null){
                continue;
            }
            tag.setCountry(country);
            tag.setPressReleases(new ArrayList<>());
            tagRepository.save(tag);
            country.setTag(tag);
            countryRepository.save(country);
        }
    }

    public void addFeedsDataToDB(){
        String intSection = "International";
        for (int i = 0; i < feedsNames.length; i++){
            Newspaper newspaper = newspaperRepository.findByName(newspapersNames[i]);
            if (newspaper == null){
                continue;
            }
            Feed addingFeed = new Feed();
            addingFeed.setName(feedsNames[i]);
            addingFeed.setNewspaper(newspaper);
            addingFeed.setSection(intSection);
            addingFeed.setPressReleases(new ArrayList());
            newspaper.getFeeds().add(addingFeed);
            newspaperRepository.save(newspaper);
            feedRepository.save(addingFeed);
        }
    }

    public void addPressReleasesToDB(String path, boolean newFeeds) throws IOException {
        File file = new File(path);
        extractFeedsFilesAndSave(file, newFeeds);
    }

    private void extractFeedsFilesAndSave(File file, boolean newFeeds) throws IOException {
        File[] files = file.listFiles();
//        assert files != null;
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

                    System.err.println(filePath);
                    for (int  i = 0; i < feedsNames.size(); i++){
                        List<Feed> feeds = feedRepository.findByName(feedsNames.get(i));
                        if (feeds.size() == 0){
                            continue;
                        }
                        Feed feed = feeds.get(0);
                        try {
                            PressRelease pressRelease = new PressRelease();
                            pressRelease.setFeed(feed);
                            pressRelease.setTitle(titles.get(i));
                            pressRelease.setDate(convertStringToDate(dates.get(i)));
                            pressRelease.setContent(contents.get(i));
                            feed.getPressReleases().add(pressRelease);
                            pressRelease.setTags(new ArrayList<>());
                            feedRepository.save(feed);
                            pressReleaseRepository.save(pressRelease);
                        }catch (DataException e){
                            e.printStackTrace();
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }

                    }
                }
            }
            else if (f.isDirectory()){
                extractFeedsFilesAndSave(f, newFeeds);
            }
        }
    }

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

    private static Tag findTag(String tagName){
        for (Object aTagsList : tagsList) {
            Tag t = (Tag) aTagsList;
            if (t.getName().equals(tagName)) {
                return t;
            }
        }
        return null;
    }

    private static PressRelease findPressRelease(String title){
        for (Object pressRelease : pressReleases) {
            PressRelease t = (PressRelease) pressRelease;
            if (t.getTitle().equals(title)) {
                return t;
            }
        }
        return null;
    }

    private void addPressReleasesTagsData(String path, int titlesPosition) throws IOException {
        File file = new File(path);
        extractTaggedFeedsFilesAndSave(file, titlesPosition);

    }

    private void extractTaggedFeedsFilesAndSave(File file, int titlesPosition) throws IOException {
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
                    List<Feed> feeds = feedRepository.findByName(feedName);
                    if (feeds.size() == 0) {
                        continue;
                    }
                    Feed feed = feeds.get(0);
                    List<PressRelease> pressReleases = pressReleaseRepository.findByFeed(feed);
                    if (pressReleases.size() == 0) {
                        continue;
                    }
                    Iterable<Tag> tagIterable = tagRepository.findAll();
                    tagsList = new ArrayList();
                    for (Tag myTag : tagIterable) {
                        tagsList.add(myTag);
                    }
                    for (int i = 0; i < titles.size(); i++) {
                        if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = findPressRelease(titles.get(i));
                        Tag tag = findTag(tags.get(i));
                        if (pressRelease == null || tag == null){
                            continue;
                        }
                        pressRelease.getTags().add(tag);
                        tag.getPressReleases().add(pressRelease);
                        tagRepository.save(tag);
                        pressReleaseRepository.save(pressRelease);
                    }
                }

            }
            else if (f.isDirectory()) {
                extractTaggedFeedsFilesAndSave(f, titlesPosition);
            }
        }
    }

    //WORKING BUT RESULT COULD BE DONE WITH addPressReleasesTagsData method if name of file will be name of feed and tag index will be increasing by one
    public void addPressReleasesEbolaTags() throws IOException {
        String[] ebolaFilePaths = new String[feedsNames.length];
        for (int i=0; i < feedsNames.length; i++){
            ebolaFilePaths[i] = GEOMEDIA_FEEDS_FILE_PATH + feedsNames[i] + "/rss_unique_TAG_country_Ebola.csv";
            System.out.println(ebolaFilePaths[i]);
        }
        for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
            String filePath = ebolaFilePaths[i1];
            List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
            List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 6, '\t');
            System.err.println("#################################");
            System.err.println(filePath);
            System.err.println("#################################");
            String queryForPressRelease = "select p from PressRelease as p inner join p.feedID as f where f.name = \'" + feedsNames[i1] + "\'";
            String queryForEbolaTag = "from TAG as t where t.name = \'EBOLA\'";
            List<Feed> feeds = feedRepository.findByName(feedsNames[i1]);
            if (feeds.size() == 0) {
                continue;
            }
            Feed feed = feeds.get(0);
            pressReleases = feed.getPressReleases();
            if (pressReleases.size() == 0) {
                System.out.println("Baaaaad");
                continue;
            }
            Tag ebolaTag = tagRepository.findByName("EBOLA");
            if (ebolaTag != null) {
                for (int i = 0; i < titles.size(); i++) {
                    if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                        continue;
                    }
                    PressRelease pressRelease = findPressRelease(titles.get(i));
                    if (pressRelease == null) {
                        continue;
                    }
                    pressRelease.getTags().add(ebolaTag);
                    ebolaTag.getPressReleases().add(pressRelease);
                    pressReleaseRepository.save(pressRelease);
                    tagRepository.save(ebolaTag);
                }
            }
        }
    }

    //WORKING BUT RESULT COULD BE DONE WITH addPressReleasesTagsData method if name of file will be name of feed
    public void addOrganizationFeedsTagged() throws IOException {
        String[] ebolaFilePaths = new String[feedsNames.length];
        for (int i=0; i < feedsNames.length; i++){
            ebolaFilePaths[i] = ORG_PATH + "SHORT/" + feedsNames[i] + "/rss_org_tagged.csv";
            System.out.println(ebolaFilePaths[i]);
        }
        for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
            String filePath = ebolaFilePaths[i1];
            List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
            List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 4, '\t');
            System.err.println("#################################");
            System.err.println(filePath);
            System.err.println("#################################");
            String queryForPressRelease = "select p from PressRelease as p inner join p.feedID as f where f.name = \'" + feedsNames[i1] + "\'";
            String queryForTag = "from TAG";
            List<Feed> feeds = feedRepository.findByName(feedsNames[i1]);
            if (feeds.size() == 0) {
                continue;
            }
            Feed feed = feeds.get(0);
            pressReleases = pressReleaseRepository.findByFeed(feed);
            Iterable<Tag> tagIterable = tagRepository.findAll();
            tagsList = new ArrayList<>();
            for (Tag tag : tagIterable) {
                tagsList.add(tag);
            }
            try {
                for (int i = 0; i < titles.size(); i++) {
                    if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                        continue;
                    }
                    PressRelease pressRelease = findPressRelease(titles.get(i));
                    Tag tag = findTag(tags.get(i));
                    if (pressRelease == null || tag == null){
                        continue;
                    }
                    pressRelease.getTags().add(tag);
                    tag.getPressReleases().add(pressRelease);
                    tagRepository.save(tag);
                    pressReleaseRepository.save(pressRelease);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/addThingsNewWay")
    public String  addSomething() throws IOException {
        long startTime = System.nanoTime();
        if (flushed) {
            languagesToDb = new HashMap<>(languages.length);
            countriesToDb = new HashMap<>();
            newspapersToDb = new HashMap<>();
            feedsToDb = new HashMap<>();
            pressReleasesToDb = new ArrayList<>();

            System.out.println("Langs");
            //langs
            for (String languageName : languages) {
                Language language = new Language();
                language.setName(languageName);
                language.setNewspapers(new ArrayList());
                languagesToDb.put(languageName, language);
            }

            System.out.println("Countries");
            //countries
            String tagsAndCountriesFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
            List<String> countries = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 1, '\t');
            for (int i = 0; i < countries.size(); i++) {
                Country addingCountry = new Country();
                addingCountry.setName(countries.get(i));
                addingCountry.setNewspapers(new ArrayList<>());
                countriesToDb.put(countries.get(i), addingCountry);
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
                newspaper.setFeeds(new ArrayList<>());
                language.getNewspapers().add(newspaper);
                country.getNewspapers().add(newspaper);
                newspapersToDb.put(newspapersNames[i], newspaper);
            }

            System.out.println("Tags");
            //tags
            tagsList = new ArrayList<>();
            List<String> tags = ReaderCsvFiles.readAtPosition(tagsAndCountriesFilePath, 0, '\t');
            for (int i = 0; i < tags.size(); i++) {
                Country country = countriesToDb.get(countries.get(i));
                if (country == null) {
                    continue;
                }
                Tag tag = new Tag();
                tag.setName(tags.get(i));
                tag.setCountry(country);
                tag.setPressReleases(new ArrayList<>());
                country.setTag(tag);
                tagsList.add(tag);
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
                addingFeed.setPressReleases(new ArrayList());
                newspaper.getFeeds().add(addingFeed);
                feedsToDb.put(feedsNames[i], addingFeed);
            }

            System.out.println("PressReleases");
            //pressReleases
            pressReleases = new LinkedList<>();
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
                pressReleases = feed.getPressReleases();
                if (pressReleases.size() == 0) {
                    System.out.println("Baaaaad");
                    continue;
                }

                Tag ebolaTag = findTag("EBOLA");
                if (ebolaTag != null) {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myEbolaTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = findPressRelease(titles.get(i));
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
                pressReleases = feed.getPressReleases();
                try {
                    for (int i = 0; i < titles.size(); i++) {
                        if (myOrgTags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = findPressRelease(titles.get(i));
                        Tag tag = findTag(myOrgTags.get(i));
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
        long startDBTime = System.nanoTime();
        Collection<Country> countryCollection = countriesToDb.values();
        Collection<Feed> feedCollection = feedsToDb.values();
        Collection<Language> languageCollection = languagesToDb.values();
        Collection<Newspaper> newspaperCollection = newspapersToDb.values();
        countryRepository.save(countryCollection);
        feedRepository.save(feedCollection);
        languageRepository.save(languageCollection);
        newspaperRepository.save(newspaperCollection);
        pressReleaseRepository.save(pressReleasesToDb);
        tagRepository.save(tagsList);
        flushed = true;
        System.out.println("I have saved");
        this.feedsToDb = null;
        this.countriesToDb = null;
        this.languagesToDb = null;
        this.newspapersToDb = null;
        this.pressReleasesToDb = null;
        tagsList = null;
        pressReleases = null;
        long currentTime = System.nanoTime();
        System.out.println("Czas zapisu do bazy: " + ((currentTime - startDBTime)/1000000000) + " s");
        System.out.println("Czas wykonania: " + ((currentTime - startTime)/1000000000) + " s");
        return "foo";
    }

    private void extractFeedsFilesAndSaveNewWay(File file, boolean newFeeds) throws IOException {
        File[] files = file.listFiles();
//        assert files != null;
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
                            pressRelease.setTags(new ArrayList<>());
                            pressReleases.add(pressRelease);
                            pressReleasesToDb.add(pressRelease);
                        }catch (DataException e){
                            e.printStackTrace();
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
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
                    List<PressRelease> pressReleases = feed.getPressReleases();
                    if (pressReleases.size() == 0) {
                        continue;
                    }
                    if (tagsList == null || tagsList.size() == 0) {
                        throw new IOException("AAAAAAAAA");
                    }
                    for (int i = 0; i < titles.size(); i++) {
                        if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = findPressRelease(titles.get(i));
                        Tag tag = findTag(tags.get(i));
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
