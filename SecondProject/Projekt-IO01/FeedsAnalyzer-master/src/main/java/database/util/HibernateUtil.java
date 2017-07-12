package database.util;

import csv.reader.ReaderCsvFiles;
import database.DAO.GenericDAO;
import database.DAO.GenericDAOHibernate;
import database.POJO.*;
import org.hibernate.exception.DataException;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HibernateUtil {
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
    private static List pressReleases;
    private static List tagsList;


    public static void main(String[] args) throws IOException {

        //foo();
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
    }

    public static void foo() {
        GenericDAO feedDao = new GenericDAOHibernate();
        List feeds = feedDao.executeQuery("SELECT f.name from Feed as f\n");
        for (Object o : feeds) {
            System.out.println(o);
        }
        feedDao.closeSession();
    }

    public static void addLanguagesToDB(){
        String[] languages = {
                "English",
                "Spanish",
                "French",
                "Polish"
        };
        GenericDAO<Language,Integer> dao = new GenericDAOHibernate<Language, Integer>((Language.class));
        for (String language: languages) {
            Language lg = new Language();
            lg.setName(language);
            dao.create(lg);
        }
        dao.closeSession();
    }

    public static void addNewspapersToDB(){
        String[] newspapersCountry = {
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
        String[] newspapersLanguage = {
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
        GenericDAO<Newspaper,Integer> dao = new GenericDAOHibernate<Newspaper, Integer>(Newspaper.class);
        for (int i = 0; i < newspapersNames.length; i++){
            String countryIdQuery = "select ID from Country where name = \'" + newspapersCountry[i] +"\'";
            String languageIdQuery = "select ID from Language where name = \'" + newspapersLanguage[i]+"\'";
            List countryIds = dao.executeQuery(countryIdQuery);
            List languagesIds = dao.executeQuery(languageIdQuery);
            if (countryIds.size() != 1 || languagesIds.size() != 1){
                continue;
            }
            Newspaper newspaper = new Newspaper();
            newspaper.setName(newspapersNames[i]);
            newspaper.setCountryID(new Country((Integer) countryIds.get(0)));
            newspaper.setLanguageID(new Language((Integer) languagesIds.get(0)));
            dao.create(newspaper);
        }
        dao.closeSession();
    }

    public static void addCountriesToDB() throws IOException {
        String filePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
        List<String> countries = ReaderCsvFiles.readAtPosition(filePath, 1, '\t');
        GenericDAO<Country,Integer> dao = new GenericDAOHibernate<Country, Integer>(Country.class);
        for (int i = 0; i < countries.size(); i++){
            Country addingCountry = new Country();
            addingCountry.setName(countries.get(i));
            System.out.println(countries.get(i));
            dao.create(addingCountry);
            if (i % 20 == 0){
                dao.flushAndClear();
            }
        }
        dao.closeSession();
    }

    public static void addTagsToDb() throws IOException {
        String filePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
        List<String> countries = ReaderCsvFiles.readAtPosition(filePath, 1, '\t');
        List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 0, '\t');
        GenericDAO<TAG,Integer> dao = new GenericDAOHibernate<TAG,Integer>(TAG.class);
        for (int i = 0; i < tags.size(); i++){
            String countryIdQuery = "select ID from Country where name = \'" + countries.get(i) +"\'";
            TAG tag = new TAG();
            tag.setName(tags.get(i));
            List countriesIDs = dao.executeQuery(countryIdQuery);
            if (countriesIDs.size() != 1){
                continue;
            }
            tag.setCountryID(new Country((Integer) countriesIDs.get(0), countries.get(i)));
            dao.create(tag);
            if (i % 20 == 0){
                dao.flushAndClear();
            }
        }
        dao.closeSession();
    }
    public static void addFeedsDataToDB(){
        String intSection = "International";
        GenericDAO<Feed,Integer> dao = new GenericDAOHibernate<Feed, Integer>(Feed.class);
        for (int i = 0; i < feedsNames.length; i++){
            String queryForNewspapers = "from Newspaper where name = \'" + newspapersNames[i] +"\'";
            List newspapers = dao.executeQuery(queryForNewspapers);
            if (newspapers.size() != 1){
                continue;
            }
            Feed addingFeed = new Feed();
            addingFeed.setName(feedsNames[i]);
            addingFeed.setNewspaperID((Newspaper) newspapers.get(0));
            addingFeed.setSection(intSection);
            dao.create(addingFeed);
            if (i % 20 == 0){
                dao.flushAndClear();
            }
        }
        dao.closeSession();
    }

    public static void addPressReleasesToDB(String path, boolean newFeeds) throws IOException {
        File file = new File(path);
        extractFeedsFilesAndSave(file, newFeeds);
    }
    private static void extractFeedsFilesAndSave(File file, boolean newFeeds) throws IOException {
        File[] files = file.listFiles();
        assert files != null;
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
                    GenericDAO<PressRelease,Integer> dao = new GenericDAOHibernate<PressRelease, Integer>(PressRelease.class);
                    System.err.println(filePath);
                    for (int  i = 0; i < feedsNames.size(); i++){
                        String queryForFeed = "from Feed where name = \'" + feedsNames.get(i) + "\'";
                        List feeds = dao.executeQuery(queryForFeed);
                        if (feeds.size() != 1){
                            continue;
                        }
                        try {
                            PressRelease pressRelease = new PressRelease();
                            pressRelease.setFeedID((Feed) feeds.get(0));
                            pressRelease.setTitle(titles.get(i));
                            pressRelease.setDate(convertStringToDate(dates.get(i)));
                            pressRelease.setContent(contents.get(i));
                            dao.create(pressRelease);
                            if (i%20 == 0){
                                dao.flushAndClear();
                            }
                        }catch (DataException e){
                            e.printStackTrace();
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }

                    }
                    dao.closeSession();
                }
            }
            else if (f.isDirectory()){
                extractFeedsFilesAndSave(f, newFeeds);
            }
        }
    }
    private static void addPressReleasesTagsData(String path, int titlesPosition) throws IOException {
        File file = new File(path);
        extractTaggedFeedsFilesAndSave(file, titlesPosition);

    }
    private static void extractTaggedFeedsFilesAndSave(File file, int titlesPosition) throws IOException {
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
                    GenericDAO<PressReleasesTag, Integer> dao = new GenericDAOHibernate<PressReleasesTag, Integer>(PressReleasesTag.class);
                    System.err.println("#################################");
                    System.err.println(filePath);
                    System.err.println("#################################");
                    //ATTENTION - PARSING FILE SHOULD BE NAMED LIKE FEED NAME !!!
                    String feedName = f.getName().split("\\.")[0];
                    System.out.println(feedName);
                    String queryForPressRelease = "select p from PressRelease as p inner join p.feedID as f where f.name = \'" + feedName + "\'";
                    String queryForTag = "from TAG";
                    pressReleases = dao.executeQuery(queryForPressRelease);
                    tagsList = dao.executeQuery(queryForTag);
                    for (int i = 0; i < titles.size(); i++) {
                        if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                            continue;
                        }
                        PressRelease pressRelease = findPressRelease(titles.get(i));
                        TAG tag = findTag(tags.get(i));
                        if (pressRelease == null || tag == null){
                            continue;
                        }
                        PressReleasesTag pressReleasesTag = new PressReleasesTag();
                        pressReleasesTag.setPressReleaseID(pressRelease);
                        pressReleasesTag.setTagID(tag);
                        dao.create(pressReleasesTag);
                        if (i % 20 == 0) {
                            dao.flushAndClear();
                        }
                    }
                    dao.closeSession();
                }

            }
            else if (f.isDirectory()) {
                    extractTaggedFeedsFilesAndSave(f, titlesPosition);
            }
        }
    }

    //WORKING BUT RESULT COULD BE DONE WITH addPressReleasesTagsData method if name of file will be name of feed and tag index will be increasing by one
    public static void addPressReleasesEbolaTags() throws IOException {
        String[] ebolaFilePaths = new String[feedsNames.length];
        for (int i=0; i < feedsNames.length; i++){
            ebolaFilePaths[i] = GEOMEDIA_FEEDS_FILE_PATH + feedsNames[i] + "/rss_unique_TAG_country_Ebola.csv";
            System.out.println(ebolaFilePaths[i]);
        }
        for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
            String filePath = ebolaFilePaths[i1];
            List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
            List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 6, '\t');
            GenericDAO<PressReleasesTag, Integer> dao = new GenericDAOHibernate<PressReleasesTag, Integer>(PressReleasesTag.class);
            System.err.println("#################################");
            System.err.println(filePath);
            System.err.println("#################################");
            String queryForPressRelease = "select p from PressRelease as p inner join p.feedID as f where f.name = \'" + feedsNames[i1] + "\'";
            String queryForEbolaTag = "from TAG as t where t.name = \'EBOLA\'";
            pressReleases = dao.executeQuery(queryForPressRelease);
            List queryResult = dao.executeQuery(queryForEbolaTag);
            if (queryResult.size() > 0) {
                TAG ebolaTag = (TAG) dao.executeQuery(queryForEbolaTag).get(0);
                for (int i = 0; i < titles.size(); i++) {
                    if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                        continue;
                    }
                    PressRelease pressRelease = findPressRelease(titles.get(i));
                    if (pressRelease == null) {
                        continue;
                    }
                    PressReleasesTag pressReleasesTag = new PressReleasesTag();
                    pressReleasesTag.setPressReleaseID(pressRelease);
                    pressReleasesTag.setTagID(ebolaTag);
                    dao.create(pressReleasesTag);
                    if (i % 20 == 0) {
                        dao.flushAndClear();
                    }
                }
            }
            dao.closeSession();
        }
    }

    //WORKING BUT RESULT COULD BE DONE WITH addPressReleasesTagsData method if name of file will be name of feed
    public static void addOrganizationFeedsTagged() throws IOException {
        String[] ebolaFilePaths = new String[feedsNames.length];
        for (int i=0; i < feedsNames.length; i++){
            ebolaFilePaths[i] = ORG_PATH + "SHORT/" + feedsNames[i] + "/rss_org_tagged.csv";
            System.out.println(ebolaFilePaths[i]);
        }
        for (int i1 = 0; i1 < ebolaFilePaths.length; i1++) {
            String filePath = ebolaFilePaths[i1];
            List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 3, '\t');
            List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 4, '\t');
            GenericDAO<PressReleasesTag, Integer> dao = new GenericDAOHibernate<PressReleasesTag, Integer>(PressReleasesTag.class);
            System.err.println("#################################");
            System.err.println(filePath);
            System.err.println("#################################");
            String queryForPressRelease = "select p from PressRelease as p inner join p.feedID as f where f.name = \'" + feedsNames[i1] + "\'";
            String queryForTag = "from TAG";
            pressReleases = dao.executeQuery(queryForPressRelease);
            tagsList = dao.executeQuery(queryForTag);
            try {
                for (int i = 0; i < titles.size(); i++) {
                    if (tags.get(i).equals("") || titles.get(i).contains("\'")) {
                        continue;
                    }
                    PressRelease pressRelease = findPressRelease(titles.get(i));
                    TAG tag = findTag(tags.get(i));
                    if (pressRelease == null || tag == null){
                        continue;
                    }
                    PressReleasesTag pressReleasesTag = new PressReleasesTag();
                    pressReleasesTag.setPressReleaseID(pressRelease);
                    pressReleasesTag.setTagID(tag);
                    dao.create(pressReleasesTag);
                    if (i % 20 == 0) {
                        dao.flushAndClear();
                    }
                }
            }catch (NullPointerException e){
                dao.closeSession();
            }
            dao.closeSession();
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
    private static TAG findTag(String tagName){
        for (Object aTagsList : tagsList) {
            TAG t = (TAG) aTagsList;
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
}
