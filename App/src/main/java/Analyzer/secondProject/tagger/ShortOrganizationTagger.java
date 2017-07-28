package Analyzer.secondProject.tagger;

import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import Analyzer.secondProject.csv.writer.WriterCsvFiles;

import java.io.IOException;
import java.util.*;

import static Analyzer.secondProject.tagger.MainTagger.getTagsDataPositions;

public class ShortOrganizationTagger extends BasicTagger {

	public ShortOrganizationTagger() {
		super(BasicTagger.UNNAMED_TAGGER);
	}

	@Override
	public void tagFile(TagDataContainer tagDataContainer) throws IOException {
		long tagCount = 0;
		Set<String> words = new HashSet<>();

		for (int i = 0; i < tagDataContainer.getTitles().size(); i++) {
			String[] contents = {tagDataContainer.getTitles().get(i), tagDataContainer.getDescriptions().get(i)};

			words.clear();
			for (String content : contents) {
				String[] splitted = content.split(REGEX);
				for (String s : splitted) {
					if (!s.isEmpty()) {
						words.add(s);
					}
				}
			}

			tagCount = this.doCoreWork(tagDataContainer, tagCount, words, i);
		}
		if (tagCount > 0) {
			System.err.println(tagDataContainer.getDestinationFilePath() + "; " + tagCount);
			globalTagCount += tagCount;
		}
	}

	public void work(String tagsFilePath, String sourceFolderPath, String destinationFolderPath, boolean isGeomedia) throws IOException {
		globalTagCount = 0;
		int[] tagPositions = getTagsDataPositions(tagsFilePath);
		complexTags = ReaderCsvFiles.getComplexTags(tagsFilePath, tagPositions[0], tagPositions[1], false);
		this.doFirstStageOfWork(tagsFilePath, sourceFolderPath, destinationFolderPath, isGeomedia);
		complexTags.clear();
		complexTags = null;
		System.err.println("FINISHED: " + sourceFolderPath + "; " + destinationFolderPath + "; " + globalTagCount + " tags");
	}

}

