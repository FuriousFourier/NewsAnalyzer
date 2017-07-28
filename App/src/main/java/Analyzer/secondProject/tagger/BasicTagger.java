package Analyzer.secondProject.tagger;

import Analyzer.NewsAnalyzerMain;
import Analyzer.info.InfoContainer;
import Analyzer.repository.FeedRepository;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import com.sun.media.sound.InvalidDataException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static Analyzer.secondProject.tagger.MainTagger.getSourceDataPositions;
import static Analyzer.secondProject.tagger.MainTagger.getTagsDataPositions;

public abstract class BasicTagger {
	public static final String REGEX = "[,\\./;'\\[\\]\\-=<>\\?:\"\\{\\}_\\+\\\\\\|!@#\\$%\\^&\\*\\(\\)\\s]+";
	public final static String UNNAMED_TAGGER = "UNNAMED TAGGER";

	protected String languageName;
	protected FeedRepository feedRepository;
	protected Set<ComplexTag> complexTags;
	protected long globalTagCount;

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
						if (tagDataContainer.getStemmingDictionary() != null) {
							String stemmed = tagDataContainer.getStemmingDictionary().get(s);
							if (stemmed != null) {
								words.add(stemmed);
							} else {
								words.add(s);
							}
						} else {
							words.add(s);
						}
					}
				}
			}

			tagCount = doCoreWork(tagDataContainer, tagCount, words, i);
		}
		if (tagCount > 0) {
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
			synchronized (REGEX) {
				globalTagCount += tagCount;
			}
		}
	}

	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
		globalTagCount = 0;
		int[] tagPositions = getTagsDataPositions(tagsFilePath);
		complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, tagPositions[0], tagPositions[1], true);
		this.doFirstStageOfWork(tagsFilePath, sourceFolderPath, destinationFolderPath, isGeomedia);
		complexTags.clear();
		complexTags = null;
		System.err.println("FINISHED: " + sourceFolderPath + "; " + destinationFolderPath + "; " + globalTagCount + " tags");
	}

	protected void doFirstStageOfWork(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException{
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null files for " + sourceFolderPath);
			return;
		}

		Map<String, String> stemmingData = null;
		/*String stemmingFilePath = InfoContainer.STEMMING_FOLDER_PATH + "/" + "Stemming_" + this.languageName + ".csv";
		try {
			stemmingData = ReaderCsvFiles.readAtTwoPosition(stemmingFilePath, 0, 1, ' ');
		} catch (FileNotFoundException e) {
			System.out.println(stemmingFilePath + " doesn't exist");
		}*/

		for (File f: files){
			if (f.isFile()) {
				if (isGeomedia && !f.getName().equals(InfoContainer.GEOMEDIA_UNIQUE_FILE_NAME)) {
					continue;
				}
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
				List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
				List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
				List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

				System.out.println(sourceFilePath + " is being tagged");
				TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath, stemmingData);
				tagFile(tagDataContainer);
			} else if (f.isDirectory()) {
				boolean shouldBeGeomedia = isGeomedia || f.getName().startsWith("Geomedia");
				this.doFirstStageOfWork(tagsFilePath, sourceFolderPath + "/" + f.getName(), destinationFolderPath + "/" + f.getName(), shouldBeGeomedia);
			}
		}
	}

	protected long doCoreWork(TagDataContainer tagDataContainer, long tagCount, Set<String> words, int i) throws IOException {

		tagLoop:
		for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
			try {
				for (String word: words){
					if (complexTag.keyWords.contains(word)) {
						++tagCount;
						WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
						continue tagLoop;
					}
				}
			} catch (IndexOutOfBoundsException e) {
				System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
			}
		}
		return tagCount;
	}

	public BasicTagger(String languageName) {
		this.complexTags = null;
		this.globalTagCount = 0;
		this.languageName = languageName;
		this.feedRepository = NewsAnalyzerMain.getConfigurableApplicationContext().getBean(FeedRepository.class);
	}
}
