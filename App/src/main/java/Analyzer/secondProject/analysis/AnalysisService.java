package Analyzer.secondProject.analysis;

import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import Analyzer.secondProject.database.DAO.*;
import Analyzer.secondProject.database.POJO.PressRelease;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class AnalysisService {

    private static final String FILEPATH_BASE = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/FeedsAnalysis/";

    public static void main(String[] args) {
        creatingFileWithQuantityOfTagsForEveryNewspaper();
        creatingFileWithQuantityOfTagsOnDate();
        creatingFilesWithEbolaTags();
        sumQuantityOfExistingOfTwoCountries();
        generateQuantityOfTagsForNewspapersGroupedByCountry();
        sumQuantityOfExistingOfTwoCountriesForEveryNewspaper();
        creatingFilesWithQuantityOfOrgTagsForEveryNewspaper();
    }

    private static void creatingFileWithQuantityOfTagsForEveryNewspaper(){
        String folderName = "TagsForNewspapers/names/";
        String query = "SELECT n.name, c.name,  count(t.name) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON  prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON c.ID = t.countryID\n" +
                "  GROUP BY n.name, t.name\n" +
                "  ORDER BY count(t.name) DESC";
        createFiles(folderName, query);
//        createFile(folderName, "TOP", query);
    }
    private static void creatingFilesWithQuantityOfOrgTagsForEveryNewspaper(){
        String folderName = "ORGTagsForNewspapers/names/";
        String query = "SELECT n.name, t.name,  count(t.ID) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON  prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  GROUP BY n.name, t.ID\n" +
                "  HAVING t.ID between  2 and 17 \n" +
                "  ORDER BY count(t.ID) DESC ";
        createFiles(folderName, query);
//        createFile(folderName, "TOP", query);
    }

    private static void creatingFileWithQuantityOfTagsOnDate(){
        String folderName = "TagsForDates/NEW/";
        String query = "SELECT f.name, n.name, pr.date, c.name,  count(t.name) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON  prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON c.ID = t.countryID\n" +
                "  GROUP BY n.name, t.name, year(pr.date), month(pr.date), day(pr.date)\n";
        /*String query = "SELECT name from Language";*/
        createFiles(folderName, query);
    }
    private static void creatingFilesWithEbolaTags(){
        String folderName = "EbolaTags/";
        String queryNewspapers = "SELECT n.name, c.name , count(t.ID) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON n.countryID = c.ID\n" +
                "  WHERE t.name = \'EBOLA\'\n" +
                "GROUP BY pr.feedID, t.ID";
        String queryCountries = "SELECT c.name , count(t.ID) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON n.countryID = c.ID\n" +
                "  WHERE t.name = \'EBOLA\'\n" +
                "GROUP BY c.name, t.ID";
        String queryDate = "SELECT pr.date , count(t.ID) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON n.countryID = c.ID\n" +
                "  WHERE t.name = \'EBOLA\'\n" +
                "GROUP BY year(pr.date), month(pr.date), day(pr.date), t.ID";
//        createFile(folderName, "ebolaNewspaper", queryNewspapers);
//        createFile(folderName, "ebolaCountries", queryCountries);
        createFile(folderName, "ebolaDate", queryDate);

    }
    private static void createFiles(String folderName, String query){
        GenericDAO dao = new GenericDAOHibernate();
        List queryResult = dao.executeQuery(query);
        System.out.println("Wielkosc = " + queryResult.size());
        for (Object aQueryResult : queryResult) {
            Object[] queryObject = (Object[]) aQueryResult;
            String[] args = new String[queryObject.length];
            try {
                for (int i = 0; i < queryObject.length; i++){
                    args[i] = queryObject[i].toString();
                }
            } catch (NullPointerException e){
                continue;
            }
            String feedName = (String) queryObject[0];
            String filePath = FILEPATH_BASE + folderName + feedName + ".csv";
            try {
                WriterCsvFiles.write(filePath, args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dao.closeSession();
    }
    private static void createFile(String folderName, String fileName,  String query){
        String filePath = FILEPATH_BASE + folderName + fileName + ".csv";
        GenericDAO dao = new GenericDAOHibernate();
        List queryResult = null;
        try {
           queryResult = dao.executeQuery(query);
        }catch (Exception e){
            e.printStackTrace();
            dao.closeSession();
        }
        if (queryResult != null){
            for (Object aQueryResult : queryResult) {
                Object[] queryObject = (Object[]) aQueryResult;
                writeToFile(filePath, queryObject);

            }
        }
        dao.closeSession();
    }
    private static void writeToFile(String filePath, Object[] toWrite){
        String[] args = new String[toWrite.length];
        for (int i = 0; i < toWrite.length; i++){
            args[i] = toWrite[i].toString();
        }
        try {
            WriterCsvFiles.write(filePath, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void sumQuantityOfExistingOfTwoCountries(){
        String folderFilePath = "ExistingOfTwoCountries/";
        GenericDAO dao = new GenericDAOHibernate();
        String getCountriesQuery = "SELECT c.name from Country as c";
        List<String> firstListOfCountries = dao.executeQuery(getCountriesQuery);
        List<String> secondListOfCountries = new ArrayList<String>(firstListOfCountries);
        for (String country: firstListOfCountries) {
            if (country.equals("United Kingdom") || country.equals("France") || country.equals("United States of America") ||
                    country.equals("Spain") || country.equals("South Korea") || country.equals("Canada") || country.equals("Mexico")
                    || country.equals("Germany") || country.equals("China") || country.equals("Ukraine") || country.equals("Poland") ||
                    country.equals("Iran")) {
                List<PressRelease> firstCountryPressReleases = dao.executeQuery(createQueryToPressReleasesForCountry(country));
//            List<PressRelease> allPressReleasesWithoutCountry = dao.executeQuery(createQueryToPressReleasesWithoutCountry(country));
                for (String secondCountry : secondListOfCountries) {
                    List<PressRelease> secondCountryPressReleases = dao.executeQuery(createQueryToPressReleasesForCountry(secondCountry));
                    int numberOfCommonExistence = findNumberOfCommonExistence(firstCountryPressReleases, secondCountryPressReleases);
                    System.err.println(country + " " + secondCountry + " " + numberOfCommonExistence );
                    try {
                        WriterCsvFiles.write(FILEPATH_BASE + folderFilePath + country + ".csv", country, secondCountry, String.valueOf(numberOfCommonExistence));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        dao.closeSession();
    }

    //VERY BRUTAL WAY - NEED TO BE DONE BETTER
    private static void sumQuantityOfExistingOfTwoCountriesForEveryNewspaper(){
        String folderFilePath = "ExistingOfTwoCountriesInNewspaper/";
        GenericDAO dao = new GenericDAOHibernate();
        String getCountriesQuery = "SELECT c.name from Country as c";
        String getNewspapers = "SELECT n.name, l.name from Newspaper as n inner join Language as l on n.languageID = l.ID";
        List<String> firstListOfCountries = dao.executeQuery(getCountriesQuery);
        List<String> secondListOfCountries = new ArrayList<String>(firstListOfCountries);
        List newspapers = dao.executeQuery(getNewspapers);
        for (Object newspaper: newspapers){
            Object[] queryObject = (Object[]) newspaper;
            String[] args = new String[queryObject.length];
            for (int i = 0; i < queryObject.length; i++){
                args[i] = queryObject[i].toString();
            }
            for (String country: firstListOfCountries) {
                if (country.equals("United Kingdom") || country.equals("France") || country.equals("United States of America") ||
                        country.equals("Spain") || country.equals("South Korea") || country.equals("Canada") || country.equals("Mexico")
                        || country.equals("Germany") || country.equals("China") || country.equals("Ukraine") || country.equals("Poland") ||
                        country.equals("Iran")) {
                    List<PressRelease> firstCountryPressReleases = dao.executeQuery(createQueryToPressReleasesForCountryInNewspaper(country, args[0]));
                    for (String secondCountry : secondListOfCountries) {
                        if (!country.equals(secondCountry)){
                            List<PressRelease> secondCountryPressReleases = dao.executeQuery(createQueryToPressReleasesForCountryInNewspaper(secondCountry, args[0]));
                            int numberOfCommonExistence = findNumberOfCommonExistence(firstCountryPressReleases, secondCountryPressReleases);
                            if (numberOfCommonExistence == 0){
                                continue;
                            }
                            System.err.println(country + " " + secondCountry + " " + numberOfCommonExistence );
                            try {
                                WriterCsvFiles.write(FILEPATH_BASE + folderFilePath + country + "_" + args[0] + ".csv",
                                        args[0], args[1], country, secondCountry, String.valueOf(numberOfCommonExistence));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }
            }
        }

        dao.closeSession();
    }
    private static String createQueryToPressReleasesForCountry(String country){
        return "select p from PressRelease as p \n" +
                "inner join PressReleasesTag as prt on p.ID = prt.pressReleaseID \n" +
                "inner join TAG as t on prt.tagID = t.ID \n" +
                "inner join Country as c on c.ID = t.countryID \n" +
                "where c.name = \'" + country +"\'";
    }
    private static String createQueryToPressReleasesForCountryInNewspaper(String country, String newspaper){
        return "select p from PressRelease as p \n" +
                "inner join PressReleasesTag as prt on p.ID = prt.pressReleaseID \n" +
                "inner join TAG as t on prt.tagID = t.ID \n" +
                "inner join Country as c on c.ID = t.countryID \n" +
                "inner join Feed as f on p.feedID = f.ID \n" +
                "inner join Newspaper as n on n.ID = f.newspaperID \n" +
                "where c.name = \'" + country +"\' and n.name = \'" + newspaper + "\'";
    }
    private static int findNumberOfCommonExistence(List<PressRelease> firstCountryPressReleases, List<PressRelease> secondCountryPressReleases){
        int result = 0;
        for (PressRelease p1: firstCountryPressReleases){
            for (PressRelease p2: secondCountryPressReleases){
                if (p1.getTitle().equals(p2.getTitle())){
                    result++;
                    break;
                }
            }
        }
        return result;
    }
    private static void generateQuantityOfTagsForNewspapersGroupedByCountry(){
        String folderName = "QuantityOfTagsForNewspapersGroupedByCountry/";
        String query = "SELECT c2.name, n.name, c.name,  count(t.name) FROM PressReleasesTag as prt\n" +
                "  INNER JOIN PressRelease as pr ON prt.pressReleaseID = pr.ID\n" +
                "  INNER JOIN TAG as t ON  prt.tagID = t.ID\n" +
                "  INNER JOIN Feed as f ON pr.feedID = f.ID\n" +
                "  INNER JOIN Newspaper as n ON f.newspaperID = n.ID\n" +
                "  INNER JOIN Country as c ON c.ID = t.countryID\n" +
                "  INNER JOIN Country as c2 on c2.ID = n.countryID\n" +
                "  GROUP BY n.name, t.name\n" +
                "  ORDER BY count(t.name) DESC";
        createFiles(folderName, query);

    }

}
