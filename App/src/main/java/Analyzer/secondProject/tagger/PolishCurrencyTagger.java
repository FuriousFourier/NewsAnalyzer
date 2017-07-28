package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import au.com.bytecode.opencsv.CSVReader;
import com.sun.media.sound.InvalidDataException;
import org.codehaus.groovy.transform.SourceURIASTTransformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static Analyzer.secondProject.tagger.MainTagger.getSourceDataPositions;
import static Analyzer.secondProject.tagger.MainTagger.getTagsDataPositions;

public class PolishCurrencyTagger extends BasicTagger {

	@Override
	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
		globalTagCount = 0;
		complexTags = PolishCurrencyTagger.getComplexTags(tagsFilePath);
		this.doFirstStageOfWork(tagsFilePath, sourceFolderPath, destinationFolderPath, isGeomedia);
		complexTags.clear();
		complexTags = null;
		System.err.println("FINISHED: " + sourceFolderPath + "; " + destinationFolderPath + "; " + globalTagCount + " tags");
	}

	@Override
	public void tagFile(TagDataContainer tagDataContainer) throws IOException {
		List<String> words = new ArrayList<>();
		Map<String, CurrencyTag> usedShortTags = new HashMap<>();
		Set<CurrencyTag> usedLongTags = new HashSet<>();
		long tagCount = 0;

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			String title = tagDataContainer.getTitles().get(i).toLowerCase();
			String description = tagDataContainer.getDescriptions().get(i).toLowerCase();
			words.clear();
			usedShortTags.clear();
			usedLongTags.clear();
			words.addAll(Arrays.asList(title.split(REGEX)));
			words.addAll(Arrays.asList(description.split(REGEX)));

			if (tagDataContainer.getComplexTags() == null) {
				System.out.println("AAAAAAAA");
				System.exit(33);
			}
			tagLoop:
			for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
				CurrencyTag currencyTag = ((CurrencyTag) complexTag);
				int indexOfWord;
				for (indexOfWord=0; indexOfWord<words.size(); ++indexOfWord){
					if (words.get(indexOfWord).startsWith(currencyTag.getMainKeyword())) {
						break;
					}
				}
				if (indexOfWord < words.size()){
					int left = Math.max(indexOfWord - 1, 0);
					int right = Math.min(indexOfWord + 1, words.size() - 1);
					int j;
					if (currencyTag.keyWords.isEmpty()) {
						usedShortTags.put(currencyTag.getMainKeyword(), currencyTag);
						continue tagLoop;
					}

					for (j=left; j<=right; ++j) {
						for (String keyWord: currencyTag.getKeyWords()){
							if (words.get(j).startsWith(keyWord)){
								usedLongTags.add(currencyTag);
								continue tagLoop;
							}
						}
					}
				}
			}
			tagCount += usedLongTags.size();
			for (CurrencyTag currencyTag : usedLongTags) {
				WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), currencyTag.getName());
				usedShortTags.remove(currencyTag.getMainKeyword());
			}
			tagCount += usedLongTags.size();
			for (CurrencyTag currencyTag : usedShortTags.values()) {
				WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), currencyTag.getName());
			}
		}
		if (tagCount > 0) {
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
			globalTagCount += tagCount;
		}
	}

	@Override
	protected void doFirstStageOfWork(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {

		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			throw new FileNotFoundException();
		}

		Map<String, String> stemmingData = null;
		String stemmingFilePath = InfoContainer.STEMMING_FOLDER_PATH + "/" + "Stemming_" + this.languageName + ".csv";
		try {
			stemmingData = ReaderCsvFiles.readAtTwoPosition(stemmingFilePath, 0, 1);
		} catch (FileNotFoundException e) {
			System.out.println(stemmingFilePath + " doesn't exist");
		}

		for (File f: files){
			if (f.isFile()) {
				try {
					String sourceFilePath = f.getAbsolutePath();
					String destinationFilePath = destinationFolderPath + "/" + f.getName();
					int [] dataPositions;
					try {
						dataPositions = getSourceDataPositions(sourceFilePath);
					} catch (InvalidDataException e) {
						System.err.println("Handling " + f.getName() + " is impossible, skipping");
						continue;
					}
					List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
					if (feeds.isEmpty()) {
						System.out.println("Empty file: " + sourceFilePath);
						continue;
					}
					String feedName = feeds.get(0);
					Feed feed = feedRepository.findByName(feedName);
					if (feed == null) {
						System.err.println("Feed " + feedName + " not found in DB");
						continue;
					}

					if (!feed.getNewspaper().getLanguage().getName().equals(this.languageName)) {
						continue;
					}

					List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
					List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
					List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

					System.out.println(sourceFilePath + " is being tagged");
					TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath, stemmingData);
					this.tagFile(tagDataContainer);
				} catch (IOException e) {
					System.err.println("Something went wrong for " + f.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
	}

	private static Set<ComplexTag> getComplexTags(String tagFilePath) throws InvalidDataException {
		char separator;
		try {
			separator = ReaderCsvFiles.getSeparator(tagFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InvalidDataException();
		}
		Set<ComplexTag> result = null;

		try (FileReader fileReader = new FileReader(tagFilePath)) {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = new HashSet<>();
			String[] nextLine = reader.readNext();

			while (nextLine != null) {
				String[] keyWords = nextLine[1].split(BasicTagger.REGEX);
				ComplexTag complexTag = new CurrencyTag(nextLine[0], keyWords[0].toLowerCase());
				for (int i=1; i<keyWords.length; ++i) {
					complexTag.keyWords.add(keyWords[i].toLowerCase());
				}
				result.add(complexTag);
				nextLine = reader.readNext();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result == null) {
			System.out.println("LOOOOOOOOOOL");
			System.exit(44);
		}
		return result;
	}

	public PolishCurrencyTagger(String languageName) {
		super(languageName);
	}
}
