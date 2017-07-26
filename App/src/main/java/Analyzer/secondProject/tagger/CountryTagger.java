package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.model.Feed;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static Analyzer.secondProject.tagger.MainTagger.getDataPositions;

public class CountryTagger extends BasicTagger {

	public CountryTagger() {
		super(UNNAMED_TAGGER);
	}

	@Override
	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		File[] files = file.listFiles();
		if (files == null) {
			System.out.println("Null files for " + sourceFolderPath);
			return;
		}

		Map<String, String> stemmingData = null;
		/*String stemmingFilePath = InfoContainer.STEMMING_FOLDER_PATH + "/" + "Stemming_" + this.languageName + ".txt";
		try {
			stemmingData = ReaderCsvFiles.readAtTwoPosition(stemmingFilePath, 0, 1, ' ');
		} catch (FileNotFoundException e) {
			System.out.println(stemmingFilePath + " doesn't exist");
		}*/

		Set<ComplexTag> complexTags = null;

		for (File f: files){
			if (f.isFile()) {
				String sourceFilePath = f.getAbsolutePath();
				String destinationFilePath = destinationFolderPath + "/" + f.getName();
				int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

				List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);


				List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
				List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
				List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

				if (complexTags == null) {
					complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, dataPositions[4], dataPositions[5]);
				}

				System.out.println(sourceFilePath + " is being tagged");
				TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath, stemmingData);
				tagFile(tagDataContainer);
			}
		}
	}
}
