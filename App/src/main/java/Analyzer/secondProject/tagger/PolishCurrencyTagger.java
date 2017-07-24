package Analyzer.secondProject.tagger;

import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import au.com.bytecode.opencsv.CSVReader;
import com.sun.media.sound.InvalidDataException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PolishCurrencyTagger extends CurrencyTagger {

	@Override
	public void tagFile(TagDataContainer tagDataContainer) throws IOException {
		List<String> words = new ArrayList<>();
		Map<String, CurrencyTag> usedShortTags = new HashMap<>();
		Map<String, CurrencyTag> usedLongTags = new HashMap<>();
		long tagCount = 0;

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			String title = tagDataContainer.getTitles().get(i).toLowerCase();
			String description = tagDataContainer.getDescriptions().get(i).toLowerCase();
			words.clear();
			usedShortTags.clear();
			usedLongTags.clear();
			words.addAll(Arrays.asList(title.split(REGEX)));
			words.addAll(Arrays.asList(description.split(REGEX)));

			tagLoop:
			for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
				CurrencyTag currencyTag = ((CurrencyTag) complexTag);
				int indexOfWord;
				for (indexOfWord=0; indexOfWord<words.size(); ++indexOfWord){
					if (words.get(indexOfWord).contains(currencyTag.getMainKeyword())) {
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
							if (words.get(j).contains(keyWord)){
								usedLongTags.put(currencyTag.getMainKeyword(), currencyTag);
								continue tagLoop;
							}
						}
					}
				}
			}
			tagCount += usedLongTags.values().size();
			for (CurrencyTag currencyTag : usedLongTags.values()) {
				WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), currencyTag.getName());
				usedShortTags.remove(currencyTag.getMainKeyword());
			}
			tagCount += usedLongTags.values().size();
			for (CurrencyTag currencyTag : usedShortTags.values()) {
				WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), currencyTag.getName());
			}
		}
		if (tagCount > 0)
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
	}

	@Override
	public void work(String tagFilePath, String sourceDirectoryPath, String destinationFolderPath) throws IOException {
		Set<ComplexTag> complexTags = PolishCurrencyTagger.getComplexTags(tagFilePath);

		File file = new File(sourceDirectoryPath);
		File[] files = file.listFiles();
		if (files == null) {
			throw new FileNotFoundException();
		}

		for (File f: files){
			if (f.isFile()) {
				try {
					String sourceFilePath = f.getAbsolutePath();
					String destinationFilePath = destinationFolderPath + "/" + f.getName();
					int[] dataPositions = MainTagger.getDataPositions(sourceFilePath, tagFilePath);

					List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
					Feed feed = feedRepository.findByName(feeds.get(0));
					if (feed == null) {
						System.err.println("Feed " + feeds.get(0) + " not found in DB");
						continue;
					}

					if (!feed.getNewspaper().getLanguage().getName().equals(this.languageName)) {
						continue;
					}

					List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
					List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
					List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

					System.out.println(sourceFilePath + " is being tagged");
					TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath);
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
				String[] keyWords = nextLine[1].split(CurrencyTagger.REGEX);
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
		return result;
	}

	public PolishCurrencyTagger(String languageName) {
		super();
		this.languageName = languageName;
	}
}
