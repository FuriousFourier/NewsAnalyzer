package tagger;

import au.com.bytecode.opencsv.CSVReader;
import csv.reader.ReaderCsvFiles;
import csv.writer.WriterCsvFiles;
import org.hibernate.boot.jaxb.SourceType;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//../../ -> ../SecondProject
//./ -> ../SecondProject/Projekt-IO01/FeedsAnalyzer-master
public class Tagger {
    private static final Character feedFileSeparator = ' ';
    private static final Character tagFileSeparator = '\t';
    private static final String secondFeedsFolderPaths = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB";
    private static final String firstFeedsFolderPaths = "../SecondProject/geomedia/Geomedia_extract_AGENDA/Geomedia_extract_AGENDA";
    private static final String countryTagFile = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv";
    private static final String newFeedsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Feeds";
    private static final String destinationTagsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/TaggedFeeds";
    public static void main(String[] args) throws IOException {
        System.out.println(System.getProperty("user.dir"));
        Map<String, String> tagsMap = ReaderCsvFiles.readAtTwoPosition("../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv",
                1, 2, '\t');
        /*tagFileByCountry("../../geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv",
                "./Feeds",
                "./TaggedFeeds");
        System.out.println("Robota 1");
        tagFileByOrganization("./orgs.csv",
                "../../geomedia/Geomedia_extract_AGENDA/Geomedia_extract_AGENDA",
                "./TaggedFeeds/OrgTag");
        System.out.println("Robota 2");
        tagFileByOrganization("./orgs_short.csv",
                firstFeedsFolderPaths,
                "./TaggedFeeds/OrgTag/SHORT");
        System.out.println("Robota 3");
        tagFileByCountry(countryTagFile, "./Feeds",
                "./TaggedFeeds/NewFeeds/CountryTag" );*/
        System.out.println("Robota 1");
        tagFileByCountry("../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv", newFeedsFolderPaths, destinationTagsFolderPaths + "/taggedForCountry");
        System.out.println("Robota 2");
        tagFileByOrganization("../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs.csv",firstFeedsFolderPaths, destinationTagsFolderPaths + "/taggedForOrg");
        System.out.println("Robota 3");
        tagFileByOrganization("../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs_short.csv", firstFeedsFolderPaths, destinationTagsFolderPaths + "/taggedForOrg/SHORT");
        System.out.println("Finisz");

    }

    private static void tagFileByCountry(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
        File file = new File(sourceFolderPath);
        File[] files = file.listFiles();
        assert files != null;
        for (File f: files){
            if (f.isFile()) {
                String sourceFilePath = f.getAbsolutePath();
                String destinationFilePath = destinationFolderPath + "/" + f.getName();
                System.out.println(sourceFilePath + " is tagging");
                tagFile(tagsFilePath, sourceFilePath, destinationFilePath, feedFileSeparator, tagFileSeparator);
            }
        }



    }
    private static void tagFileByOrganization(String tagsFilePath, String sourceFolderPath, String destinationfolderPath) throws IOException {
        File file = new File(sourceFolderPath);
        String[] directories = file.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
        assert directories != null;
        for (String dir: directories){
            String sourceFilePath = sourceFolderPath + "/" + dir + "/rss_unique.csv";
            String destinationFilePath = destinationfolderPath + "/" + dir + "/rss_org_tagged.csv";
            System.out.println(sourceFilePath + " is tagging");
            tagFile(tagsFilePath, sourceFilePath, destinationFilePath, feedFileSeparator, tagFileSeparator);
        }

    }
    private static void tagFile(String tagsFilePath, String sourceFilePath, String destinationFilePath,
                                Character feedFileSeparator, Character tagFileSeparator) throws IOException {
/*        System.out.println("Source: " + sourceFilePath);
        System.out.println("Destinaion: " + destinationFilePath);
        System.out.println("tags file: " + tagsFilePath);*/

        int[] positions;
        char[] readBytes = new char[4];

        FileReader tmpFileReader = new FileReader(sourceFilePath);
        if (tmpFileReader.read(readBytes, 0, 4) != 4) {
            System.out.println("Coś się popsuło");
            System.exit(12);
        }
        if (new String(readBytes).equals("\"ID\"")) {
            positions = new int[]{1, 2, 3, 4};
        } else {
            positions = new int[]{0, 1, 2, 3};
        }

        List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, positions[0], feedFileSeparator);
        List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, positions[1], feedFileSeparator);
        List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, positions[2], feedFileSeparator);
        List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, positions[3], feedFileSeparator);
        List<String> tags = ReaderCsvFiles.readAtPosition(tagsFilePath, 0, tagFileSeparator);
        List<String> tagsKeys = ReaderCsvFiles.readAtPosition(tagsFilePath,  1, tagFileSeparator);

        for (int i = 0; i < titles.size(); i++) {
            List<String> usedTags = new LinkedList<String>();
            for (int j = 0; j < tags.size(); j++){
                if (!usedTags.contains(tags.get(j))){
                    String tagKey = tagsKeys.get(j);
                    try {
                        if (i < descriptions.size()) {
                            if ((titles.get(i).contains(tagKey)) || (descriptions.get(i).contains(tagKey))){
                                WriterCsvFiles.write(destinationFilePath, feeds.get(i), times.get(i), titles.get(i), descriptions.get(i), tags.get(j));
                                usedTags.add(tags.get(j));
                            }
                        }else {
                            if ((titles.get(i).contains(tagKey))){
                                WriterCsvFiles.write(destinationFilePath, feeds.get(i), times.get(i), titles.get(i), descriptions.get(i), tags.get(j));
                                usedTags.add(tags.get(j));
                            }
                        }
                    }catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private static void createTagsAndCountriesFile() throws IOException {
        String resultFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagsAndCountries.csv";
        String tagsFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv";
        String countriesFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/countries.csv";
        Map<String,String> tagsAndCountries = ReaderCsvFiles.readTwoFilesAndReturnTagsWithCountries(tagsFilePath,countriesFilePath);
        WriterCsvFiles.writeTagsAndCountries(resultFilePath, tagsAndCountries);
    }
}
