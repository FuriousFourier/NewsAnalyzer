package Analyzer.secondProject.tagger;

import Analyzer.secondProject.csv.reader.ReaderCsvFiles;
import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FrenchCurrencyTagger extends CurrencyTagger {

	public FrenchCurrencyTagger() {
		super("French");
	}

	@Override
	protected Set<ComplexTag> getComplexTags(String tagFilePath) throws IOException {
		char separator = ReaderCsvFiles.getSeparator(tagFilePath);
		Set<ComplexTag> result = null;

		try (FileReader fileReader = new FileReader(tagFilePath)) {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = new HashSet<>();
			String[] nextLine = reader.readNext();

			while (nextLine != null) {
				String[] keyWords = nextLine[1].split(BasicTagger.REGEX);
				ComplexTag complexTag = new CurrencyTag(nextLine[0], keyWords[0].toLowerCase());
				result.add(complexTag);
				nextLine = reader.readNext();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}