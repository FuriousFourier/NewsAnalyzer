package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.model.Feed;
import Analyzer.repository.FeedRepository;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
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
				ComplexTag complexTag = new CurrencyTag(nextLine[0], keyWords[0]);
				for (int i=1; i<keyWords.length; ++i) {
					complexTag.keyWords.add(keyWords[i]);
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
