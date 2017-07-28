package Analyzer.secondProject.tagger;

import Analyzer.info.InfoContainer;
import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;
import com.sun.media.sound.InvalidDataException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static Analyzer.secondProject.tagger.MainTagger.getSourceDataPositions;
import static Analyzer.secondProject.tagger.MainTagger.getTagsDataPositions;

public class CountryTagger extends BasicTagger {
	private List<Thread> workers;

	public CountryTagger() {
		super(UNNAMED_TAGGER);
		this.workers = new LinkedList<>();
	}

	protected long doCoreWork(TagDataContainer tagDataContainer, long tagCount, Set<String> words, int i) throws IOException {

		tagLoop:
		for (ComplexTag complexTag: tagDataContainer.getComplexTags()){
			for (String keyword: complexTag.getKeyWords()) {
				try {
					for (String word: words){
						if (word.startsWith(keyword)) {
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
				Thread worker = new Thread(() -> {
					try {
						tagFile(tagDataContainer);
					} catch (IOException e) {
						System.err.println("Error in CountryTagger worker :/");
						e.printStackTrace();
					}
				});
				worker.start();
				workers.add(worker);
			} else if (f.isDirectory()) {
				boolean shouldBeGeomedia = isGeomedia || f.getName().startsWith("Geomedia");
				this.doFirstStageOfWork(tagsFilePath, sourceFolderPath + "/" + f.getName(), destinationFolderPath + "/" + f.getName(), shouldBeGeomedia);
			}
		}
	}

	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
		globalTagCount = 0;
		int[] tagPositions = getTagsDataPositions(tagsFilePath);
		complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, tagPositions[0], tagPositions[1], true);
		this.doFirstStageOfWork(tagsFilePath, sourceFolderPath, destinationFolderPath, isGeomedia);
		for (Thread thread : workers) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Interrupted in main worker of CountryTagger");
				e.printStackTrace();
			}
		}
		complexTags.clear();
		complexTags = null;
		System.err.println("FINISHED: " + sourceFolderPath + "; " + destinationFolderPath + "; " + globalTagCount + " tags");
	}
}
