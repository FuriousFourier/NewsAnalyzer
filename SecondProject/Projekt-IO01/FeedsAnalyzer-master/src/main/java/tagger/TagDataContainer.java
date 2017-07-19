package tagger;

import java.util.List;

/**
 * Created by pawel on 19.07.17.
 */
public class TagDataContainer {
	private List<String> feeds;
	private List<String> times;
	private List<String> titles;
	private List<String> descriptions;
	private List<String> tags;
	private List<String> tagsKeys;

	public TagDataContainer(List<String> feeds, List<String> times, List<String> titles, List<String> descriptions, List<String> tags, List<String> tagsKeys) {
		this.feeds = feeds;
		this.times = times;
		this.titles = titles;
		this.descriptions = descriptions;
		this.tags = tags;
		this.tagsKeys = tagsKeys;
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

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getTagsKeys() {
		return tagsKeys;
	}

	public void setTagsKeys(List<String> tagsKeys) {
		this.tagsKeys = tagsKeys;
	}
}
