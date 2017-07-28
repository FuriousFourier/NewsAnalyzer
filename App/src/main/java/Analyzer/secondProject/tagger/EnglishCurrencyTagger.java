package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import com.sun.media.sound.InvalidDataException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static Analyzer.secondProject.tagger.MainTagger.getSourceDataPositions;

public class EnglishCurrencyTagger extends BasicTagger {

	@Override
	protected void doFirstStageOfWork(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			throw new FileNotFoundException();
		}

		Map<String, String> stemmingData = null;
		/*String stemmingFilePath = InfoContainer.STEMMING_FOLDER_PATH + "/" + "Stemming_" + this.languageName + ".csv";
		try {
			stemmingData = ReaderCsvFiles.readAtTwoPosition(stemmingFilePath, 0, 1);
		} catch (FileNotFoundException e) {
			System.out.println(stemmingFilePath + " doesn't exist");
		}*/

		for (File f: files){
			if (f.isFile()) {
				if (isGeomedia && !f.getName().equals(InfoContainer.GEOMEDIA_UNIQUE_FILE_NAME)) {
					continue;
				}

				try {
					String sourceFilePath = f.getAbsolutePath();
					String destinationFilePath = destinationFolderPath + "/" + f.getName();
					int [] dataPositions = null;
					int[] tagPositions;
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
			} else if (f.isDirectory()) {
				boolean shouldBeGeomedia = isGeomedia || f.getName().startsWith("Geomedia");
				this.doFirstStageOfWork(tagsFilePath, sourceFolderPath + "/" + f.getName(), destinationFolderPath + "/" + f.getName(), shouldBeGeomedia);
			}
		}
	}

	public EnglishCurrencyTagger(String languageName) {
		super(languageName);
		this.languageName = languageName;
	}
}
