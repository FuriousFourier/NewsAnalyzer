package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.repository.FeedRepository;

import java.io.IOException;

public abstract class CurrencyTagger {

	public static final String REGEX = "[,\\./;'\\[\\]\\-=<>\\?:\"\\{\\}_\\+\\\\\\|!@#\\$%\\^&\\*\\(\\)\\s]+";
	protected String languageName;
	protected FeedRepository feedRepository;

	protected abstract void tagFile(TagDataContainer tagDataContainer) throws IOException;

	public abstract void work(String tagFilePath, String sourceDirectoryPath, String destinationFolderPath) throws IOException;

	public CurrencyTagger() {
		this.feedRepository = NewsAnalyzerMain.getConfigurableApplicationContext().getBean(FeedRepository.class);
	}
}
