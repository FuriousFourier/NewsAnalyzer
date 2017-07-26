package Analyzer.secondProject.tagger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pawel on 19.07.17.
 */
public class TagDataContainer {
	private List<String> feeds;
	private List<String> times;
	private List<String> titles;
	private List<String> descriptions;
	private Set<ComplexTag> complexTags;
	private String destinationFilePath;
	private Map<String, String>  stemmingDictionary;

	public TagDataContainer(List<String> feeds, List<String> times, List<String> titles, List<String> descriptions, Set<ComplexTag> complexTags, String destinationFilePath, Map<String, String> stemmingDictionary) {
		this.feeds = feeds;
		this.times = times;
		this.titles = titles;
		this.descriptions = descriptions;
		this.complexTags = complexTags;
		this.destinationFilePath = destinationFilePath;
		this.stemmingDictionary = stemmingDictionary;
	}

	public List<String> getFeeds() {
		return feeds;
	}

	public void setFeeds(List<String> feeds) {
		this.feeds = feeds;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	public Set<ComplexTag> getComplexTags() {
		return complexTags;
	}

	public void setComplexTags(Set<ComplexTag> complexTags) {
		this.complexTags = complexTags;
	}

	public String getDestinationFilePath() {
		return destinationFilePath;
	}

	public void setDestinationFilePath(String destinationFilePath) {
		this.destinationFilePath = destinationFilePath;
	}

	public Map<String, String> getStemmingDictionary() {
		return stemmingDictionary;
	}

	public void setStemmingDictionary(Map<String, String> stemmingDictionary) {
		this.stemmingDictionary = stemmingDictionary;
	}
}
