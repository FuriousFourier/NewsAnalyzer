package tagger;

import com.sun.media.sound.InvalidDataException;
import com.sun.syndication.feed.atom.Feed;
import csv.reader.ReaderCsvFiles;
import csv.writer.WriterCsvFiles;
import info.FeedInfoContainer;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

//../../ -> ../SecondProject
//./ -> ../SecondProject/Projekt-IO01/FeedsAnalyzer-master
public class Tagger {

	private static final String secondFeedsFolderPaths = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB";
	private static final String oldFeedsFolderPaths = "../SecondProject/geomedia/Geomedia_extract_AGENDA/Geomedia_extract_AGENDA";
	private static final String countryTagFile = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Dico_Country_Free.csv";
	private static final String newFeedsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Feeds";
	private static final String destinationTagsFolderPaths = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/TaggedFeeds";
	private static final String organizationTagsFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs.csv";
	private static final String organizationShortTagsFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/orgs_short.csv";
	private static final String currenciesPlFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/currencies_Polish.csv";
	private static final String currenciesEngFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/currencies_English.csv";
	public static final String DEFAULT_CONTENT = "NO CONTENT";

	private static final String[] tagFiles = {countryTagFile, currenciesEngFilePath};//organizationTagsFilePath, organizationShortTagsFilePath};
	private static final String[] destinationSuffixes = {"taggedForCountry", "taggedForCurrency"};//"taggedForOrg", "taggedForOrg/SHORT"};

	public static void main(String[] args) throws IOException {
		if (tagFiles.length != destinationSuffixes.length) {
			System.err.println("Error in paths");
			System.exit(1);
		}

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
		Path directory = Paths.get(destinationTagsFolderPaths);
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});

		tagNewFeeds(1);
		//tagGeomedia(2);
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
		final String CURRENCY_WORD = "currencies";
		int langNameLength;
		boolean isTaggingForCurrency = tagsFilePath.contains(CURRENCY_WORD);
		String lang = null;
		if (isTaggingForCurrency) {
			langNameLength = tagsFilePath.length() - 4 - tagsFilePath.lastIndexOf(CURRENCY_WORD) - CURRENCY_WORD.length() - 1;
			if (langNameLength > 0)
				lang = tagsFilePath.substring(tagsFilePath.lastIndexOf(CURRENCY_WORD) + CURRENCY_WORD.length() + 1, langNameLength + tagsFilePath.lastIndexOf(CURRENCY_WORD) + CURRENCY_WORD.length() + 1);
		}
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null files for " + sourceFolderPath);
			return;
		}
		List<String> tags = null;
		List<String> tagsKeys = null;
		for (File f: files){
			if (f.isFile()) {
				String sourceFilePath = f.getAbsolutePath();
				String destinationFilePath = destinationFolderPath + "/" + f.getName();
				String feedLang = null;
				int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

				List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
				if (isTaggingForCurrency) {
					int feedIndex = Arrays.asList(FeedInfoContainer.feedsNames).indexOf(feeds.get(0));
					if (feedIndex == -1) {
						feedIndex = Arrays.asList(FeedInfoContainer.nonGeomediaFeedsNames).indexOf(feeds.get(0));
						if (feedIndex == -1) {
							System.err.println(feeds.get(0) + " not found on list");
							return;
						}
						feedLang = FeedInfoContainer.nonGeomediaNewspapersLanguage[feedIndex];
					} else {
						feedLang = FeedInfoContainer.newspapersLanguage[feedIndex];
					}
					if (!feedLang.equals(lang))
						return;
				}

				List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
				List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
				List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);
				if (tags == null) {
					tags = ReaderCsvFiles.readAtPosition(tagsFilePath, dataPositions[4]);
					tagsKeys = ReaderCsvFiles.readAtPosition(tagsFilePath, dataPositions[5]);
				}

				System.out.println(sourceFilePath + " is being tagged");
				TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, tags, tagsKeys);
				tagFile(tagDataContainer, destinationFilePath);
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
		List<String> tags = null;
		List<String> tagsKeys = null;

		for (String dir: directories){
			String sourceFilePath = sourceFolderPath + "/" + dir + "/rss_unique.csv";
			String destinationFilePath = destinationfolderPath + "/" + dir + ".csv";
			System.out.println(sourceFilePath + " is being tagged");

			int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

			List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
			List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
			List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
			List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

			if (tags == null) {
				tags = ReaderCsvFiles.readAtPosition(tagsFilePath, dataPositions[4]);
				tagsKeys = ReaderCsvFiles.readAtPosition(tagsFilePath, dataPositions[5]);
			}

			TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, tags, tagsKeys);
			tagFile(tagDataContainer, destinationFilePath);
		}

	}

	private static void tagFile(TagDataContainer tagDataContainer, String destinationFilePath) throws IOException {

		Set<String> usedTags = new HashSet<>();

		if (tagDataContainer.getTitles().size() != tagDataContainer.getDescriptions().size()) {
			System.err.println("LOOOOOOOL + " + tagDataContainer.getTitles().size() + "; " + tagDataContainer.getDescriptions().size());
		}
		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			usedTags.clear();
			for (int j = 0; j < tagDataContainer.getTags().size(); j++){
				if (!usedTags.contains(tagDataContainer.getTags().get(j))){
					String tagKey = tagDataContainer.getTagsKeys().get(j).toLowerCase();
					try {
						if ((tagDataContainer.getTitles().get(i).contains(tagKey)) || (tagDataContainer.getDescriptions().get(i).contains(tagKey))){
							WriterCsvFiles.write(destinationFilePath, tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), tagDataContainer.getTags().get(j));
							usedTags.add(tagDataContainer.getTags().get(j));
						}
					}catch (IndexOutOfBoundsException e){
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i + ", j: " + j);
					}

				}
			}
		}
	}

	private static void createTagsAndCountriesFile() throws IOException {
		String resultFilePath = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/tagDataContainer.getTags()AndCountries.csv";
		String tagsFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/Dico_Country_Free.csv";
		String countriesFilePath = "../SecondProject/geomedia/cist-sample_geomedia-db/Sample_GeomediaDB/countries.csv";
		Map<String,String> tagsAndCountries = ReaderCsvFiles.readTwoFilesAndReturnTagsWithCountries(tagsFilePath,countriesFilePath);
		WriterCsvFiles.writeTagsAndCountries(resultFilePath, tagsAndCountries);
	}

	private static int[] getDataPositions(String sourceFilePath, String tagsFilePath) throws IOException {
		int[] toReturn = new int[6];
		char[] readBytes = new char[4];
		String readBytesString;

		FileReader tmpFileReader = new FileReader(sourceFilePath);
		if (tmpFileReader.read(readBytes, 0, 4) != 4) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			throw new InvalidDataException();
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("\"ID\"")) {
			toReturn[0] = 1;
			toReturn[1] = 2;
			toReturn[2] = 3;
			toReturn[3] = 4;
		} else {
			toReturn[0] = 0;
			toReturn[1] = 1;
			toReturn[2] = 2;
			toReturn[3] = 3;
		}

		readBytes = new char[2];
		tmpFileReader = new FileReader(tagsFilePath);
		if (tmpFileReader.read(readBytes, 0, 2) != 2) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			throw new InvalidDataException();
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("ID")) {
			toReturn[4] = 1;
			toReturn[5] = 2;
		} else {
			toReturn[4] = 0;
			toReturn[5] = 1;
		}
		return toReturn;
	}

	private static Set<CurrencyTag> getCurrencyTags(){
		return new HashSet<>();
	}
}