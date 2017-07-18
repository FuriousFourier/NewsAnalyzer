package tagger;

import csv.reader.ReaderCsvFiles;
import csv.writer.WriterCsvFiles;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

//../../ -> ../SecondProject
//./ -> ../SecondProject/Projekt-IO01/FeedsAnalyzer-master
public class Tagger {

	//private static final String secondFeedsFolderPaths = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB";
	private static final String oldFeedsFolderPaths = "../SecondProject/geomedia/Geomedia_extract_AGENDA/Geomedia_extract_AGENDA";
	private static final String countryTagFile = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Dico_Country_Free.csv";
	private static final String newFeedsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Feeds";
	private static final String destinationTagsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/TaggedFeeds";
	private static final String organizationTagsFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs.csv";
	private static final String organizationShortTagsFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs_short.csv";
	public static final String DEFAULT_CONTENT = "NO CONTENT";

	private static final String[] tagFiles = {countryTagFile, organizationTagsFilePath, organizationShortTagsFilePath};
	private static final String[] destinationSuffixes = {"taggedForCountry", "taggedForOrg", "taggedForOrg/SHORT"};

	public static void main(String[] args) throws IOException {
		//Map<String, String> tagsMap = ReaderCsvFiles.readAtTwoPosition("../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv", 1, 2, '\t');

		if (tagFiles.length != destinationSuffixes.length) {
			System.err.println("Error in paths");
			System.exit(1);
		}

		tagNewFeeds(1);
		tagGeomedia(2);
		System.out.println("Finished");

	}

	public static void tagNewFeeds(int workId) throws IOException {
		for (int i=0; i<tagFiles.length; ++i) {
			System.out.println("Work " + workId + "." + i);
			tagFileNormalWay(tagFiles[i], newFeedsFolderPaths, destinationTagsFolderPaths + "/" + destinationSuffixes[i]);
		}
	}

	public static void tagGeomedia(int workId) throws IOException {
		for (int i=0; i<tagFiles.length; ++i) {
            System.out.println("Work " + workId + "." + i);
            tagFileWithUniqueInName(tagFiles[i], oldFeedsFolderPaths, destinationTagsFolderPaths + "/" + destinationSuffixes[i]);
        }
	}

	private static void tagFileNormalWay(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null files for " + sourceFolderPath);
		}
		for (File f: files){
			if (f.isFile()) {
				String sourceFilePath = f.getAbsolutePath();
				String destinationFilePath = destinationFolderPath + "/" + f.getName();
				System.out.println(sourceFilePath + " is being tagged");
				tagFile(tagsFilePath, sourceFilePath, destinationFilePath);
			}
		}



	}
	private static void tagFileWithUniqueInName(String tagsFilePath, String sourceFolderPath, String destinationfolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		String[] directories = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (directories == null) {
			System.out.println("No directories in " + file.getAbsolutePath());
			return;
		}
		for (String dir: directories){
			String sourceFilePath = sourceFolderPath + "/" + dir + "/rss_unique.csv";
			//String destinationFilePath = destinationfolderPath + "/" + dir + "/rss_org_tagged.csv";
			String destinationFilePath = destinationfolderPath + "/" + dir + ".csv";
			System.out.println(sourceFilePath + " is being tagged");
			tagFile(tagsFilePath, sourceFilePath, destinationFilePath);
		}

	}
	private static void tagFile(String tagsFilePath, String sourceFilePath, String destinationFilePath) throws IOException {
		int[] newsPositions;
		int [] tagsPositions;
		char[] readBytes = new char[4];
		String readBytesString;

		FileReader tmpFileReader = new FileReader(sourceFilePath);
		if (tmpFileReader.read(readBytes, 0, 4) != 4) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			return;
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("\"ID\"")) {
			newsPositions = new int[]{1, 2, 3, 4};
		} else {
			newsPositions = new int[]{0, 1, 2, 3};
		}

		readBytes = new char[2];
		tmpFileReader = new FileReader(tagsFilePath);
		if (tmpFileReader.read(readBytes, 0, 2) != 2) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			return;
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("ID")) {
			tagsPositions = new int[]{1, 2};
		} else {
			tagsPositions = new int[]{0, 1};
		}

		List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, newsPositions[0]);
		List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, newsPositions[1]);
		List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, newsPositions[2]);
		List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, newsPositions[3]);
		List<String> tags = ReaderCsvFiles.readAtPosition(tagsFilePath, tagsPositions[0]);
		List<String> tagsKeys = ReaderCsvFiles.readAtPosition(tagsFilePath,  tagsPositions[1]);
		Set<String> usedTags = new HashSet<>();

		for (int i = 0; i < titles.size(); i++) {
			usedTags.clear();
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
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i + ", j: " + j);
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