package Analyzer.controller;

import Analyzer.info.InfoContainer;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;

import static Analyzer.NewsAnalyzerMain.DENOMINATOR;
import static Analyzer.info.InfoContainer.*;

import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import Analyzer.secondProject.tagger.MainTagger;
import com.sun.media.sound.InvalidDataException;
import org.hibernate.Hibernate;
import org.hibernate.exception.DataException;
import Analyzer.NewsAnalyzerMain;
import Analyzer.model.*;
import Analyzer.repository.*;
import Analyzer.support.PressReleaseId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static Analyzer.info.InfoContainer.languages;
import static Analyzer.secondProject.tagger.MainTagger.deleteFolder;

/**
 * Created by pawel on 12.07.17.
 */

@Controller
public class DbController {

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

	private static Map<String, Language> languageMap = new HashMap<>();
	private static Map<String, Country> countryMap = new HashMap<>();
	private static Map<String, Newspaper> newspaperMap = new HashMap<>();
	private static Map<String, Feed> feedMap = new HashMap<>();
	private static Map<PressReleaseId, PressRelease> pressReleaseMap = new HashMap<>();
	private static Map<String, Tag> tagMap = new HashMap<>();

	private static Country unknownCountry = null;

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

	@GetMapping("/addNewThingsToDB")
	@ResponseBody
	public synchronized boolean addNewData(@RequestParam("secNum") Long securityNumber) throws IOException {

		if (!securityNumber.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		long startTime = System.nanoTime();

		languageMap.clear();
		countryMap.clear();
		newspaperMap.clear();
		feedMap.clear();
		tagMap.clear();
		pressReleaseMap.clear();
		List<Set<PressReleaseId>> changedPRs = new ArrayList<>(2);

		System.out.println("Im getting collections from DB");
		getThingsFromDb(true);
		System.out.println("Press releases");
		//pressReleases
		try {
			changedPRs.add(addPressReleasesToDBNewWay(NEW_FEEDS_PATH, true));
		} catch (InvalidDataException e) {
			return false;
		}


		System.out.println("Linking table");
		//linking table
		try {
			changedPRs.add(addPressReleasesTagsDataNewWay(DESTINATION_TAGS_FOLDER_PATHS));
		} catch (InvalidDataException e) {
			return false;
		}

		Map<PressReleaseId, PressRelease> theMap = new HashMap<>();

		for (Set<PressReleaseId> set : changedPRs) {
			for (PressReleaseId pressReleaseId : set) {
				theMap.put(pressReleaseId, pressReleaseMap.get(pressReleaseId));
			}
		}
		pressReleaseMap.clear();
		pressReleaseMap = theMap;

		long startDBTime = saveThingsToDb();

		System.out.println("I have saved");
		feedMap.clear();
		countryMap.clear();
		languageMap.clear();
		newspaperMap.clear();
		tagMap.clear();
		pressReleaseMap.clear();
		long currentTime = System.nanoTime();
		System.out.println("Database updated, time: " + ((currentTime - startDBTime) / 1000000000) + " sec");
		System.out.println("Execution time: " + ((currentTime - startTime) / 1000000000) + " sec");
		System.gc();
		return true;
	}

	private void getThingsFromDb(boolean shouldTakePressReleases) {
		Iterable<Language> languagesFromDb = languageRepository.findAll();
		Iterable<Country> countriesFromDb = countryRepository.findAll();
		Iterable<Newspaper> newspapersFromDb = newspaperRepository.findAll();
		Iterable<Feed> feedsFromDb = feedRepository.findAll();
		Iterable<Tag> tagsFromDb = tagRepository.findAll();
		Iterable<PressRelease> pressReleasesFromDb = null;

		if (shouldTakePressReleases) {
			pressReleasesFromDb = pressReleaseRepository.findAll();
		}

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
		if (shouldTakePressReleases) {
			for (PressRelease pressRelease : pressReleasesFromDb) {
				pressReleaseMap.put(new PressReleaseId(pressRelease.getTitle(), pressRelease.getDate(), pressRelease.getFeed()), pressRelease);
			}
		}

		getUnknownCountry();
	}

	private Set<PressReleaseId> extractFeedsFilesAndSaveNewWay(File file, boolean newFeeds) throws IOException {
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null for " + file.getAbsolutePath());
			throw new InvalidDataException();
		}
		Set<PressReleaseId> result = new HashSet<>();
		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().contains(".csv") && !f.getName().equals(GEOMEDIA_RSS_FILE_NAME) && !f.getName().equals(GEOMEDIA_EBOLA_TAGGED_FILE_NAME)
						&& !f.getName().equals(EBOLA_TAG_FILE_NAME)
						&& !f.getName().equals(COUNTRY_TAG_FILE_NAME)) {

					String filePath = f.getAbsolutePath();
					List<String> feedsNames;
					List<String> dates;
					List<String> titles;
					List<String> contents;
					try {
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
					} catch (InvalidDataException e) {
						System.err.println("Impossible to handle " + filePath);
						continue;
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
							PressRelease pressRelease;
							pressRelease = pressReleaseMap.get(pressReleaseId);
							if (pressRelease == null) {
								pressRelease = new PressRelease();
								pressRelease.setFeed(feed);
								pressRelease.setTitle(titles.get(i));
								pressRelease.setDate(date);
								pressRelease.setContent(contents.get(i));
								feed.getPressReleases().add(pressRelease);
								pressRelease.setTags(new HashSet<>());
								pressReleaseMap.put(pressReleaseId, pressRelease);
								result.add(pressReleaseId);
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
		return result;
	}

	private Set<PressReleaseId> addPressReleasesToDBNewWay(String path, boolean newFeeds) throws IOException {
		File file = new File(path);
		return extractFeedsFilesAndSaveNewWay(file, newFeeds);
	}

	private static Set<PressReleaseId> addPressReleasesTagsDataNewWay(String path) throws IOException {
		File file = new File(path);
		return extractTaggedFeedsFilesAndSaveNewWay(file);
	}

	private static Set<PressReleaseId>  extractTaggedFeedsFilesAndSaveNewWay(File file) throws IOException {
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("There is null here, file: " + file.getAbsolutePath());
			throw new InvalidDataException();
		}

		Set<PressReleaseId> result = new HashSet<>();
		for (File f : files) {
			if (f.isFile()) {
				if (f.getName().endsWith(".csv")) {
					String filePath = f.getAbsolutePath();
					List<String> feeds = ReaderCsvFiles.readAtPosition(filePath, 0);
					List<String> titles = ReaderCsvFiles.readAtPosition(filePath, 2);
					List<String> tags = ReaderCsvFiles.readAtPosition(filePath, 4);
					List<String> dates = ReaderCsvFiles.readAtPosition(filePath, 1);

					if (feeds.isEmpty()) {
						System.out.println("Empty file: " + filePath);
						continue;
					}
					String feedName = feeds.get(0);
					Feed feed = feedMap.get(feedName);
					if (feed == null) {
						System.out.println("There is no feed called \"" + feedName + "\"");
						continue;
					}
					for (int i = 0; i < titles.size(); i++) {
						if (tags.get(i).equals("")) {
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
						result.add(pressReleaseId);
					}
				}

			} else if (f.isDirectory()) {
				extractTaggedFeedsFilesAndSaveNewWay(f);
			}
		}
		return result;
	}


	private static void addFeedsToDb(String section, String[] myFeedNames, String[] myNewspapersNames) {
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

	private static void addNewspapersToDb(String[] myNewspapersNames, String[] myNewspapersCountry, String[] myNewspapersLanguage) {
		for (int i = 0; i < myNewspapersNames.length; i++) {
			Country country = countryMap.get(myNewspapersCountry[i]);
			Language language = languageMap.get(myNewspapersLanguage[i]);
			if (language == null) {
				continue;
			}
			if (country == null) {
				country = unknownCountry;
			}
			Newspaper newspaper = newspaperMap.get(myNewspapersNames[i]);
			if (newspaper == null) {
				newspaper = new Newspaper();
				newspaper.setName(myNewspapersNames[i]);
				newspaper.setFeeds(new HashSet<>());
			}
			newspaper.setCountry(country);
			newspaper.setLanguage(language);
			Hibernate.initialize(language);
			language.getNewspapers().add(newspaper);
			country.getNewspapers().add(newspaper);
			newspaperMap.put(myNewspapersNames[i], newspaper);
		}
	}

	private static void addTagsFromFile(String filepath, int tagPosition, int countryPosition, String category) throws IOException {
		List<String> tags = ReaderCsvFiles.readAtPosition(filepath, tagPosition);
		List<String> countries = ReaderCsvFiles.readAtPosition(filepath, countryPosition);
		for (int i = 0; i < tags.size(); i++) {
			Country country = countryMap.get(countries.get(i));
			if (country == null) {
				country = unknownCountry;
			}
			Tag tag = tagMap.get(tags.get(i));
			if (tag == null) {
				tag = new Tag();
				tag.setName(tags.get(i));
				tag.setPressReleases(new HashSet<>());
				tag.setCategory(category);
			}
			tag.setCountry(country);
			country.getTags().add(tag);
			tagMap.put(tags.get(i), tag);
		}
	}

	@GetMapping("/checkIfDBEmpty")
	@ResponseBody
	public synchronized Boolean checkIfDbEmpty(@RequestParam("secNum") Long secNum) {
		if (!secNum.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		return ((List<Language>) languageRepository.findAll()).isEmpty();
	}

	@GetMapping("/getNewFeeds")
	@ResponseBody
	public synchronized boolean getNewFeeds(@RequestParam("secNum") Long secNum, @RequestParam("shouldDelete") boolean shouldDelete) {
		if (!secNum.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		String[] tmp = new String[0];
		if (shouldDelete) {
			System.out.println("Im gonna delete feed folder");
			try {
				deleteFolder(InfoContainer.NEW_FEEDS_PATH);
			} catch (IOException e) {
				System.err.println("I cant delete feed folder, it probably didnt exist");
			}
		}
		System.out.println("Im gonna download feeds");
		Analyzer.secondProject.rss.Main.main(tmp);
		System.out.println("Im tagging");
		try {
			MainTagger.tagNewFeeds(1);
			System.out.println("Lets go with db");
			addNewData(NewsAnalyzerMain.getSecurityNumber());
			System.out.println("Work finished");
		} catch (IOException e) {
			System.err.println("Something went wrong :/");
			e.printStackTrace();
		}
		try {
			deleteFolder(InfoContainer.NEW_FEEDS_PATH);
			deleteFolder(InfoContainer.DESTINATION_TAGS_FOLDER_PATHS);
		} catch (IOException e) {
			System.err.println("I cant even throw away the litter :/");
			e.printStackTrace();
		}
		return true;
	}

	@GetMapping("/getPlTaggedPressReleases")
	public synchronized String getPlTaggedPressReleases() {
		Language language = languageRepository.findByName("Polish");
		if (language == null) {
			System.out.println("No language found");
			return "myError";
		}
		for (Newspaper newspaper : language.getNewspapers()) {
			System.out.println(newspaper.getName());
			for (Feed feed : newspaper.getFeeds()) {
				for (PressRelease pressRelease : feed.getPressReleases()) {
					System.out.println("\t" + pressRelease.getTitle());
					Set<Tag> tags = pressRelease.getTags();
					if (!tags.isEmpty()) {
						System.out.println(pressRelease.getTitle() + "; " + pressRelease.getContent());
						for (Tag tag : tags) {
							System.out.println("\t" + tag.getName());
						}
					}
				}
			}
		}
		return "foo";
	}

	@GetMapping("/initDB")
	@ResponseBody
	public synchronized boolean initializeDb(@RequestParam("secNum") Long securityNumber) throws IOException {
		if (!securityNumber.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		languageMap.clear();
		countryMap.clear();
		newspaperMap.clear();
		feedMap.clear();
		tagMap.clear();
		pressReleaseMap.clear();

		long startTime = System.nanoTime();
		//get data from db
		System.out.println("Im getting collections from DB");
		getThingsFromDb(true);

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
		List<String> countries = ReaderCsvFiles.readAtPosition(TAGS_AND_COUNTRIES_FILE_PATH, 1);
		for (String country1 : countries) {
			Country addingCountry = countryMap.get(country1);
			if (addingCountry == null) {
				addingCountry = new Country();
				addingCountry.setName(country1);
				addingCountry.setNewspapers(new HashSet<>());
				addingCountry.setTags(new HashSet<>());
				countryMap.put(country1, addingCountry);
			}
		}

		getUnknownCountry();

		System.out.println("Newspapers");
		//newspapers
		addNewspapersToDb(newspapersNames, newspapersCountry, newspapersLanguage);
		addNewspapersToDb(nonGeomediaNewspapersNames, nonGeomediaNewspapersCountry, nonGeomediaNewspapersLanguage);

		System.out.println("Tags");
		//tags
		addTagsFromFile(TAGS_AND_COUNTRIES_FILE_PATH, 0, 1, "Country name");
		addTagsFromFile(ORGANIZATION_TAG_FILE_PATH, 0, 2, "Organization");
		addTagsFromFile(currencyTagFiles[1], 0, 2, "Currency");


		System.out.println("Feeds");
		//feeds
		addFeedsToDb("International", feedsNames, newspapersNames);

		//Here we should add pl feeds
		addFeedsToDb("Polish", nonGeomediaFeedsNames, nonGeomediaNewspapersNames);

		long startDbTime = saveThingsToDb();
		System.out.println("First save time: " + ((System.nanoTime() - startDbTime) / DENOMINATOR));

		languageMap.clear();
		countryMap.clear();
		newspaperMap.clear();
		feedMap.clear();
		tagMap.clear();
		System.gc();

		MainTagger.tagGeomedia(1);

		System.out.println("Lets take data from Db another time");
		getThingsFromDb(true);

		System.out.println("PressReleases");
		//pressReleases
		addPressReleasesToDBNewWay(oldFeedsFolderPaths, false);

		System.out.println("Linking table");
		//linking table
		addPressReleasesTagsDataNewWay(DESTINATION_TAGS_FOLDER_PATHS);

		System.out.println("PressReleases ebola tags");
		//pressReleaseMap ebola tags
		String[] ebolaFilePaths = new String[feedsNames.length];
		for (int i = 0; i < feedsNames.length; i++) {
			ebolaFilePaths[i] = oldFeedsFolderPaths + "/" + feedsNames[i] + "/rss_unique_TAG_country_Ebola.csv";
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

		long secondStartDbTime = saveThingsToDb();

		System.out.println("I have saved");
		feedMap.clear();
		countryMap.clear();
		languageMap.clear();
		newspaperMap.clear();
		tagMap.clear();
		pressReleaseMap.clear();

		try {
			deleteFolder(InfoContainer.NEW_FEEDS_PATH);
			deleteFolder(InfoContainer.DESTINATION_TAGS_FOLDER_PATHS);
		} catch (IOException e) {
			System.err.println("I cant throw away the litter :/");
			e.printStackTrace();
		}

		long currentTime = System.nanoTime();
		System.out.println("Database updated, time: " + ((currentTime - secondStartDbTime) / 1000000000) + " sec");
		System.out.println("Execution time: " + ((currentTime - startTime) / 1000000000) + " sec");
		System.gc();
		return true;
	}

	private long saveThingsToDb() {
		System.out.println("I will be saving this to DB now");
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
		System.out.println("I created collections, time: " + ((collectionTime - startDBTime) / DENOMINATOR));

		languageRepository.save(languageCollection);
		long languageTime = System.nanoTime();
		System.out.println("I saved languages, time:" + ((languageTime - collectionTime) / DENOMINATOR));

		countryRepository.save(countryCollection);
		long countryTime = System.nanoTime();
		System.out.println("I saved countries, time: " + ((countryTime - languageTime) / DENOMINATOR));

		newspaperRepository.save(newspaperCollection);
		long newspaperTime = System.nanoTime();
		System.out.println("I saved newspapers, time: " + ((newspaperTime - countryTime) / DENOMINATOR));

		tagRepository.save(tagCollection);
		long tagTime = System.nanoTime();
		System.out.println("I saved tags, time: " + ((tagTime - newspaperTime) / DENOMINATOR));

		feedRepository.save(feedCollection);
		long feedTime = System.nanoTime();
		System.out.println("I saved feeds, time: " + ((feedTime - tagTime) / DENOMINATOR));

		pressReleaseRepository.save(pressReleaseCollection);
		long pressReleaseTime = System.nanoTime();
		System.out.println("I saved pressReleases, time: " + ((pressReleaseTime - feedTime) / DENOMINATOR));
		return startDBTime;
	}

	public static void getUnknownCountry() {
		unknownCountry = countryMap.get(Country.UNKNOWN_COUNTRY_NAME);
		if (unknownCountry == null) {
			unknownCountry = new Country(Country.UNKNOWN_COUNTRY_NAME, new HashSet<>(), new HashSet<>());
			countryMap.put(Country.UNKNOWN_COUNTRY_NAME, unknownCountry);
		}
	}

	@GetMapping("/createCurrencyTagStats")
	@ResponseBody
	public boolean createCurrencyTagStats(@RequestParam("secNum") Long securityNumber) {
		if (!securityNumber.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		System.out.println("Im gonna find tags");
		List<Tag> tags = tagRepository.findByCategory("Currency");
		if (tags.isEmpty()) {
			System.out.println("No currency tags found");
			return false;
		}
		tags.sort((tag, t1) -> (tag.getName().compareTo(t1.getName())));
		long tagCount = 0;
		File destinationFile = new File("currencyTagsStats.csv");
		destinationFile.delete();
		for (Tag tag : tags) {
			try {
				long currentTagCount = tag.getPressReleases().size();
				tagCount += currentTagCount;
				WriterCsvFiles.write(destinationFile.getAbsolutePath(), tag.getName(), Long.toString(currentTagCount), tag.getCountry().getName());
			} catch (IOException e) {
				System.err.println("Error while handling tag: " + tag.getName());
			}
		}
		System.out.println("There are " + tagCount + " currency tags in PressReleases");
		return true;
	}

	@GetMapping("/createTagStats")
	@ResponseBody
	public boolean createTagStats(@RequestParam("secNum") Long securityNumber) {
		if (!securityNumber.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}
		System.out.println("Im gonna find tags");
		List<Tag> tags = ((List<Tag>) tagRepository.findAll());
		if (tags.isEmpty()) {
			System.out.println("No currency tags found");
			return false;
		}
		tags.sort((tag, t1) -> (tag.getName().compareTo(t1.getName())));
		long tagCount = 0;
		File destinationFile = new File("tagsStats.csv");
		destinationFile.delete();
		for (Tag tag : tags) {
			try {
				long currentTagCount = tag.getPressReleases().size();
				tagCount += currentTagCount;
				WriterCsvFiles.write(destinationFile.getAbsolutePath(), tag.getName(), Long.toString(currentTagCount), tag.getCountry().getName());
			} catch (IOException e) {
				System.err.println("Error while handling tag: " + tag.getName());
			}
		}
		System.out.println("There are " + tagCount + " tags in PressReleases");
		return true;
	}

	@GetMapping("/createCurrencyTagStatsForNewspapers")
	@ResponseBody
	public boolean createCurrencyTagStatsForNewspapers(@RequestParam("secNum") Long securityNumber) {
		if (!securityNumber.equals(NewsAnalyzerMain.getSecurityNumber())) {
			return false;
		}

		System.out.println("Lets start");
		final String destinationFilePath = "currencyTagStatsForNewspapers.csv";
		File destinationFile = new File(destinationFilePath);
		destinationFile.delete();
		List<Newspaper> newspapers = ((List<Newspaper>) newspaperRepository.findAll());
		if (newspapers.isEmpty()) {
			System.out.println("Something strange has happened");
			return false;
		}
		System.out.println("It may take some time");
		for (Newspaper newspaper : newspapers) {
			long currencyTagCount = 0;
			Set<Feed> feeds = newspaper.getFeeds();
			for (Feed feed : feeds) {
				Set<PressRelease> pressReleases = feed.getPressReleases();
				for (PressRelease pressRelease : pressReleases) {
					Set<Tag> tags = pressRelease.getTags();
					for (Tag tag : tags) {
						if (tag.getCategory().equals("Currency")) {
							++currencyTagCount;
						}
					}

				}
			}
			System.out.println("Newspaper: " + newspaper.getName() + "; tagCount: " + currencyTagCount);
			try {
				WriterCsvFiles.write(destinationFilePath, newspaper.getName(), Long.toString(currencyTagCount));
			} catch (IOException e) {
				System.err.println("Its impossible to write into " + destinationFilePath + ", newspaper's name: " + newspaper.getName());
				return false;
			}
		}
		return true;
	}
}
