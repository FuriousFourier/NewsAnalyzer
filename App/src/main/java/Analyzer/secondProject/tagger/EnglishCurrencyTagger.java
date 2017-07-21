package Analyzer.secondProject.tagger;

import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnglishCurrencyTagger extends CurrencyTagger{

	@Override
	public void tagFile(TagDataContainer tagDataContainer) throws IOException {

		long tagCount = 0;
		Set<String> words = new HashSet<>();

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			words.clear();
			String [] splitted = tagDataContainer.getTitles().get(i).split(REGEX);
			for (String s: splitted){
				if (!s.isEmpty()) {
					words.add(s.toLowerCase());
				}
			}
			splitted = tagDataContainer.getDescriptions().get(i).split(REGEX);
			for (String s: splitted){
				if (!s.isEmpty()) {
					words.add(s.toLowerCase());
				}
			}

			for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
				for (String keyword: complexTag.getKeyWords()) {
					try {
						if (words.contains(keyword)) {
							++tagCount;
							WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
							break;
						}
					} catch (IndexOutOfBoundsException e) {
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
					}
				}
			}
		}
		if (tagCount > 0)
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
	}

	@Override
	public void work(String tagFilePath, String sourceDirectoryPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceDirectoryPath);
		File[] files = file.listFiles();
		if (files == null) {
			throw new FileNotFoundException();
		}

		Set<ComplexTag> complexTags = null;

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

					if (complexTags == null) {
						complexTags = ReaderCsvFiles.getComplexTags(tagFilePath, dataPositions[4], dataPositions[5]);
					}

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

	public EnglishCurrencyTagger(String languageName) {
		this.languageName = languageName;
	}
}
