package Analyzer.secondProject.tagger;

import Analyzer.secondProject.csv.writer.WriterCsvFiles;

import java.io.IOException;

public class OrganizationTagger extends BasicTagger {

	public OrganizationTagger() {
		super(BasicTagger.UNNAMED_TAGGER);
	}

	@Override
	protected void tagFile(TagDataContainer tagDataContainer) throws IOException {
		long tagCount = 0;
		for (int i = 0; i < tagDataContainer.getTitles().size(); ++i) {
			String title = tagDataContainer.getTitles().get(i).toLowerCase();
			String description = tagDataContainer.getDescriptions().get(i).toLowerCase();

			for (ComplexTag complexTag: tagDataContainer.getComplexTags()) {
				for (String keyword: complexTag.getKeyWords()) {
					try {
						if ((title.contains(keyword)) || (description.contains(keyword))) {
							WriterCsvFiles.write(tagDataContainer.getDestinationFilePath(), tagDataContainer.getFeeds().get(i), tagDataContainer.getTimes().get(i), tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i), complexTag.getName());
							++tagCount;
							break;
						}
					} catch (IndexOutOfBoundsException e) {
						System.err.println("Index out of bound: " + e.getMessage() + ", i: " + i);
					}
				}
			}
		}
		if (tagCount > 0) {
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
			globalTagCount += tagCount;
		}
	}

	/*@Override
	public void doFirstStageOfWork(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
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

	}*/
}
