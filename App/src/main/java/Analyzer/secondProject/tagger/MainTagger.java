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
		normalTaggers[2] = new ShortOrganizationTagger();

		// recursively deleting folder with tagged feeds, code copied from
		// https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
		deleteFolder(InfoContainer.DESTINATION_TAGS_FOLDER_PATHS);
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
			System.out.println("Directory " + folderPath + " not found");
		}
	}

	public static void initWorkersAndStart(int workId, Thread[] workers, int i, BasicTagger tagger, String tagFile, String destinationSuffix, String sourcePath, boolean isGeomedia) {
		workers[i] = new Thread(() -> {
			try {
				System.out.println("Work " + workId + "." + i + ", dest: " + destinationSuffix);
				tagger.work(tagFile, sourcePath, InfoContainer.DESTINATION_TAGS_FOLDER_PATHS + "/" + destinationSuffix, isGeomedia);
			} catch (IOException e) {
				System.err.println("Error in one of workers :/");
				e.printStackTrace();
			}
		});
		workers[i].start();
	}

	public static void tagGeomedia(int workId) throws IOException {

		if (InfoContainer.currencyTagFiles.length != currencyTaggers.length) {
			System.err.println("Error in start of currency, lengths: " + InfoContainer.currencyTagFiles.length + ", " + currencyTaggers.length);
			return;
		}

		long startTime = System.nanoTime();
		Thread[] notCurrencyWorkers = new Thread[tagFiles.length];
		Thread[] currencyWorkers = new Thread[currencyTaggers.length-1];
		for (int i=0; i<tagFiles.length; ++i) {
			final BasicTagger tagger = normalTaggers[i];
			final String tagFile = tagFiles[i];
			final String suffix = destinationSuffixes[i];

			initWorkersAndStart(workId, notCurrencyWorkers, i, tagger, tagFile, suffix, InfoContainer.oldFeedsFolderPaths, true);
		}

		for (int i=1; i<currencyTaggers.length; ++i) {
			final BasicTagger tagger = currencyTaggers[i];
			final String tagFile = InfoContainer.currencyTagFiles[i];
			initWorkersAndStart(workId+1, currencyWorkers, i-1, tagger, tagFile, destinationCurrencyTagSuffix, InfoContainer.oldFeedsFolderPaths, true);
		}

		waitForWorkers(notCurrencyWorkers);
		waitForWorkers(currencyWorkers);

		long finishTime = System.nanoTime();
		System.out.println("Finished work " + workId + ", time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	public static void waitForWorkers(Thread[] workers) {
		for (Thread thread : workers) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Error in tagger :/");
				e.printStackTrace();
			}
		}
	}

	public static void tagNewFeeds(int workId) throws IOException {
		long startTime = System.nanoTime();
		Thread[] nonCurrencyWorkers = new Thread[tagFiles.length];
		Thread[] currencyWorkers = new Thread[currencyTaggers.length];


		for (int i=0; i<tagFiles.length; ++i) {

			final BasicTagger tagger = normalTaggers[i];
			final String tagFile = tagFiles[i];
			final String destinationSuffix = destinationSuffixes[i];

			initWorkersAndStart(workId, nonCurrencyWorkers, i, tagger, tagFile, destinationSuffix, InfoContainer.NEW_FEEDS_PATH, false);
		}

		for (int i=0; i<currencyTaggers.length; ++i) {

			final BasicTagger tagger = currencyTaggers[i];
			final String tagFile = InfoContainer.currencyTagFiles[i];

			initWorkersAndStart(workId + 1, currencyWorkers, i, tagger, tagFile, destinationCurrencyTagSuffix, InfoContainer.NEW_FEEDS_PATH, false);
		}

		waitForWorkers(nonCurrencyWorkers);
		waitForWorkers(currencyWorkers);
		long finishTime = System.nanoTime();
		System.out.println("Finished work " + workId + ", time: " + ((finishTime-startTime)/ NewsAnalyzerMain.DENOMINATOR));
	}

	private static void createTagsAndCountriesFile() throws IOException {
		String resultFilePath = InfoContainer.TAGS_AND_COUNTRIES_FILE_PATH;
		Map<String,String> tagsAndCountries = ReaderCsvFiles.readTwoFilesAndReturnTagsWithCountries(countryTagFile, COUNTRIES_FILE_PATH);
		WriterCsvFiles.writeTagsAndCountries(resultFilePath, tagsAndCountries);
	}

	public static int[] getSourceDataPositions(String sourceFilePath) throws IOException {
		int[] toReturn = new int[4];
		char[] readBytes = new char[4];
		String readBytesString;

		FileReader tmpFileReader = new FileReader(sourceFilePath);
		if (tmpFileReader.read(readBytes, 0, 4) != 4) {
			System.out.println("Something strange is happening, file: " + sourceFilePath);
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

		return toReturn;
	}

	public static int[] getTagsDataPositions(String tagsFilePath) throws IOException {
		int[] toReturn = new int[2];
		String readBytesString;
		char [] readBytes = new char[2];

		FileReader tmpFileReader = new FileReader(tagsFilePath);
		if (tmpFileReader.read(readBytes, 0, 2) != 2) {
			System.out.println("Something strange is happening");
			tmpFileReader.close();
			throw new InvalidDataException();
		}
		tmpFileReader.close();
		readBytesString = new String(readBytes);
		if (readBytesString.equals("ID")) {
			toReturn[0] = 1;
			toReturn[1] = 2;
		} else {
			toReturn[0] = 0;
			toReturn[1] = 1;
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