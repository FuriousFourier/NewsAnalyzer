package Analyzer.secondProject.csv.reader;

import au.com.bytecode.opencsv.CSVReader;
import Analyzer.secondProject.tagger.ComplexTag;
import Analyzer.secondProject.tagger.MainTagger;
import com.sun.media.sound.InvalidDataException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReaderCsvFiles {

	private final static char[] separators = {9, 32, ','};

	private ReaderCsvFiles() {
	}

	public static Map<String, String> readTwoFilesAndReturnTagsWithCountries(String tagsFilePath, String countriesFilePath) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			List<String> countries = readAtPosition(countriesFilePath, 1);
			for (String country : countries) {
				String foundTag = findTagFittingToCountry(country, tagsFilePath);
				if (foundTag != null) {
					result.put(foundTag, country);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> readAtPosition(String filepath, int position) throws IOException {
		List<String> result = null;

		char separator = getSeparator(filepath);

		try (FileReader fileReader = new FileReader(filepath)) {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = new ArrayList<>();
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				try {
					result.add(nextLine[position]);
				} catch (ArrayIndexOutOfBoundsException e) {
					result.add(MainTagger.DEFAULT_CONTENT);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static char getSeparator(String filepath) throws IOException {
		char separator = 0;
		for (char tmpSeparator : separators) {
			FileReader tmpFileReader = new FileReader(filepath);
			CSVReader reader = new CSVReader(tmpFileReader, tmpSeparator);
			String[] tmpLine = reader.readNext();
			if (tmpLine == null) {
				throw new InvalidDataException();
			}
			if (tmpLine.length > 1) {
				//ok
				separator = tmpSeparator;
				reader.close();
				tmpFileReader.close();
				break;
			}
			reader.close();
			tmpFileReader.close();
		}
		if (separator == 0) {
			throw new InvalidDataException();
		}
		return separator;
	}

	public static Map<String, String> readAtTwoPosition(String filePath, int firstPosition, int secondPosition) throws IOException {
		Map<String, String> result = null;
		char separator = getSeparator(filePath);
		if (separator == 0) {
			System.out.println("No separator found");
			System.exit(33);
		}
		FileReader fileReader = new FileReader(filePath);
		try {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = new HashMap<>();
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				result.put(nextLine[firstPosition], nextLine[secondPosition]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fileReader.close();
		}
		return result;
	}

	private static String findTagFittingToCountry(String country, String filePath) throws IOException {
		FileReader fileReader = new FileReader(filePath);
		try {
			CSVReader reader = new CSVReader(fileReader, '\t');
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine[2].equals(country)) {
					return nextLine[1];
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fileReader.close();
		}
		return null;
	}

	public static Set<ComplexTag> getComplexTags(String pathToTagFile, int tagPosition, int keyWordPosition, boolean shouldGoToLowercase) throws IOException {

		char separator = getSeparator(pathToTagFile);
		Set<ComplexTag> result = null;

		try (FileReader fileReader = new FileReader(pathToTagFile)) {
			CSVReader reader = new CSVReader(fileReader, separator);
			result = Collections.newSetFromMap(new ConcurrentHashMap<>());
			String[] nextLine = reader.readNext();
			while (nextLine != null && nextLine.length <= tagPosition) {
				nextLine = reader.readNext();
			}
			if (nextLine == null) {
				return result;
			}
			ComplexTag complexTag = new ComplexTag(nextLine[tagPosition]);
			while (nextLine != null) {
				try {
					if (!nextLine[tagPosition].equals(complexTag.getName())) {
						result.add(complexTag);
						complexTag = new ComplexTag(nextLine[tagPosition]);
					}
					if (shouldGoToLowercase)
						complexTag.getKeyWords().add(nextLine[keyWordPosition].toLowerCase());
					else
						complexTag.getKeyWords().add(nextLine[keyWordPosition]);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Index out of bound: " + e.getMessage());
				}
				nextLine = reader.readNext();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
