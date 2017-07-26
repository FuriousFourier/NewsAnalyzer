package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.info.InfoContainer;
import Analyzer.repository.TagRepository;
import com.sun.media.sound.InvalidDataException;
import Analyzer.secondProject.csv.reader.*;
import Analyzer.secondProject.csv.writer.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static Analyzer.info.InfoContainer.COUNTRIES_FILE_PATH;
import static Analyzer.info.InfoContainer.countryTagFile;

//../../ -> ../SecondProject
//./ -> ../SecondProject/Projekt-IO01/FeedsAnalyzer-master
public class MainTagger {

	private static TagRepository tagRepository;

	public static final String DEFAULT_CONTENT = "NO CONTENT";

	private static final String[] tagFiles = {countryTagFile, InfoContainer.ORGANIZATION_TAG_FILE_PATH, InfoContainer.ORGANIZATION_SHORT_TAG_FILE_PATH};
	private static final String[] destinationSuffixes = {"taggedForCountry", "taggedForOrg", "taggedForOrg/SHORT"};

	private static final String destinationCurrencyTagSuffix = "taggedForCurrency";
	private static BasicTagger[] currencyTaggers = new BasicTagger[2];
	private static BasicTagger[] normalTaggers = new BasicTagger[3];

	/*public static void main(String[] args) throws IOException {
		final long denominator = 1000000000;
		if (tagFiles.length != destinationSuffixes.length) {
			System.err.println("Error in paths");
			System.exit(1);
		}

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
		Path directory = Paths.get(DESTINATION_TAGS_FOLDER_PATHS);
		try {
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
		} catch (NoSuchFileException e) {
			System.out.println("Directory for tagged feeds didn't exist");
		}
		long startTime = System.nanoTime();

		tagNewFeedsCurrency(0);
		//tagNewFeeds(1);
		long firstLoopTime = System.nanoTime();
		//tagGeomedia(2);
		long secondLoopTime = System.nanoTime();
		System.out.println("Finished");
		System.out.println("First time: " + ((firstLoopTime - startTime) / denominator));
		System.out.println("Second time: " + ((secondLoopTime - firstLoopTime) / denominator));
	}*/

	public static void initializeMainTagger() throws IOException {
		currencyTaggers[0] = new PolishCurrencyTagger("Polish");
		currencyTaggers[1] = new EnglishCurrencyTagger("English");
		normalTaggers[0] = new CountryTagger();
		normalTaggers[1] = new OrganizationTagger();
		normalTaggers[2] = normalTaggers[1];

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
	}

	public static void deleteFolder(String folderPath) throws IOException {
		Path directory = Paths.get(folderPath);
		try {
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
		} catch (NoSuchFileException e) {
			System.out.println("Directory for tagged feeds didn't exist");
		}
	}

	public static void tagNewFeeds(int workId) throws IOException {
		long startTime = System.nanoTime();
		for (int i=0; i<tagFiles.length; ++i) {
			System.out.println("Work " + workId + "." + i);
			normalTaggers[i].work(tagFiles[i], InfoContainer.NEW_FEEDS_PATH, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationSuffixes[i]);
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagGeomedia(int workId) throws IOException {
		long startTime = System.nanoTime();
		for (int i=0; i<tagFiles.length; ++i) {
            System.out.println("Work " + workId + "." + i);
			normalTaggers[i].work(tagFiles[i], InfoContainer.oldFeedsFolderPaths, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationSuffixes[i]);
        }
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagNewFeedsCurrency(int workId) throws InvalidDataException {
		if (InfoContainer.currencyTagFiles.length != currencyTaggers.length) {
			System.err.println("Error in start of currency, lengths: " + InfoContainer.currencyTagFiles.length + ", " + currencyTaggers.length);
			throw new InvalidDataException();
		}
		long startTime = System.nanoTime();
		for (int i=0; i<currencyTaggers.length; ++i) {
			try {
				System.out.println("Work " + workId + "." + i);
				currencyTaggers[i].work(InfoContainer.currencyTagFiles[i], InfoContainer.NEW_FEEDS_PATH, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationCurrencyTagSuffix);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void tagGeomediaCurrency(int workId) {
		if (InfoContainer.currencyTagFiles.length != currencyTaggers.length) {
			System.err.println("Error in start of currency, lengths: " + InfoContainer.currencyTagFiles.length + ", " + currencyTaggers.length);
			return;
		}
		long startTime = System.nanoTime();
		for (int i=0; i<currencyTaggers.length; ++i) {
			try {
				System.out.println("Work " + workId + "." + i);
				currencyTaggers[i].work(InfoContainer.currencyTagFiles[i], InfoContainer.oldFeedsFolderPaths, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationCurrencyTagSuffix);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long finishTime = System.nanoTime();
		System.out.println("Finished, time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	private static void createTagsAndCountriesFile() throws IOException {
		String resultFilePath = InfoContainer.TAGS_AND_COUNTRIES_FILE_PATH;
		Map<String,String> tagsAndCountries = ReaderCsvFiles.readTwoFilesAndReturnTagsWithCountries(countryTagFile, COUNTRIES_FILE_PATH);
		WriterCsvFiles.writeTagsAndCountries(resultFilePath, tagsAndCountries);
	}

	public static int[] getDataPositions(String sourceFilePath, String tagsFilePath) throws IOException {
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

	public static TagRepository getTagRepository() {
		return tagRepository;
	}

	public static void setTagRepository(TagRepository tagRepository) {
		MainTagger.tagRepository = tagRepository;
	}

}