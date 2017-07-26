package Analyzer.secondProject.tagger;

import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static Analyzer.secondProject.tagger.MainTagger.getDataPositions;

public class OrganizationTagger extends BasicTagger {

	public OrganizationTagger() {
		super(BasicTagger.UNNAMED_TAGGER);
	}

	@Override
	protected void tagFile(TagDataContainer tagDataContainer) throws IOException {
		for (int i = 0; i < tagDataContainer.getTitles().size(); ++i) {
			String title = tagDataContainer.getTitles().get(i).toLowerCase();
			String description = tagDataContainer.getDescriptions().get(i).toLowerCase();

			for (ComplexTag complexTag: tagDataContainer.getComplexTags()) {
				for (String keyword: complexTag.getKeyWords()) {
					try {
						if ((title.contains(keyword)) || (description.contains(keyword))) {
							WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
							break;
						}
					} catch (IndexOutOfBoundsException e) {
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
					}
				}
			}
		}
	}

	@Override
	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath) throws IOException {
		File file = new File(sourceFolderPath);
		String[] directories = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (directories == null) {
			System.out.println("No directories in " + file.getAbsolutePath());
			return;
		}
		Set<ComplexTag> complexTags = null;

		for (String dir: directories){
			String sourceFilePath = sourceFolderPath + "/" + dir + "/rss_unique.csv";
			String destinationFilePath = destinationFolderPath + "/" + dir + ".csv";

			int[] dataPositions = getDataPositions(sourceFilePath, tagsFilePath);

			List<String> feeds = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[0]);
			List<String> times = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[1]);
			List<String> titles = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[2]);
			List<String> descriptions = ReaderCsvFiles.readAtPosition(sourceFilePath, dataPositions[3]);

			if (complexTags == null) {
				complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, dataPositions[4], dataPositions[5]);
			}

			System.out.println(sourceFilePath + " is being tagged");
			TagDataContainer tagDataContainer = new TagDataContainer(feeds, times, titles, descriptions, complexTags, destinationFilePath, null);
			tagFile(tagDataContainer);
		}

	}
}
