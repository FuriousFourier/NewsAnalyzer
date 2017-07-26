package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnglishCurrencyTagger extends BasicTagger {

	@Override
	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
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

		Set<ComplexTag> complexTags = null;

		for (File f: files){
			if (f.isFile()) {
				try {
					String sourceFilePath = f.getAbsolutePath();
					String destinationFilePath = destinationFolderPath + "/" + f.getName();
					int[] dataPositions = MainTagger.getDataPositions(sourceFilePath, tagsFilePath);

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
						complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, dataPositions[4], dataPositions[5]);
					}

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

	public EnglishCurrencyTagger(String languageName) {
		super(languageName);
		this.languageName = languageName;
	}
}
