package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.repository.FeedRepository;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class BasicTagger {
	public static final String REGEX = "[,\\./;'\\[\\]\\-=<>\\?:\"\\{\\}_\\+\\\\\\|!@#\\$%\\^&\\*\\(\\)\\s]+";
	public final static String UNNAMED_TAGGER = "UNNAMED TAGGER";

	protected String languageName;
	protected FeedRepository feedRepository;

	protected void tagFile(TagDataContainer tagDataContainer) throws IOException {
		long tagCount = 0;
		Set<String> words = new HashSet<>();

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			String[] contents = {tagDataContainer.getTitles().get(i).toLowerCase(), tagDataContainer.getDescriptions().get(i).toLowerCase()};

			words.clear();
			for (String content: contents) {
				String[] splitted = content.split(REGEX);
				for (String s : splitted) {
					if (!s.isEmpty()) {
						String key = s.toLowerCase();
						if (tagDataContainer.getStemmingDictionary() != null) {
							String stemmed = tagDataContainer.getStemmingDictionary().get(key);
							if (stemmed != null) {
								words.add(stemmed);
							} else {
								words.add(key);
							}
						} else {
							words.add(key);
						}
					}
				}
			}

			tagCount = doCoreWork(tagDataContainer, tagCount, words, i);
		}
		if (tagCount > 0)
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
	}

	public abstract void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException;

	protected static long doCoreWork(TagDataContainer tagDataContainer, long tagCount, Set<String> words, int i) throws IOException {

		tagLoop:
		for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
			for (String keyword: complexTag.getKeyWords()) {
				try {
					for (String word: words){
						if (word.startsWith(keyword) || keyword.startsWith(word)) {
							++tagCount;
							WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
							continue tagLoop;
						}
					}
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
				}
			}
		}
		return tagCount;
	}

	public BasicTagger(String languageName) {
		this.languageName = languageName;
		this.feedRepository = NewsAnalyzerMain.getConfigurableApplicationContext().getBean(FeedRepository.class);
	}
}
