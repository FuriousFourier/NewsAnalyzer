package Analyzer.support;

import Analyzer.model.Tag;

public class TagPairContainer {

	private Tag tag1;
	private Tag tag2;
	private long count;

	public TagPairContainer(Tag tag1, Tag tag2, long count) {
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.count = count;
	}

	public Tag getTag1() {
		return tag1;
	}

	public void setTag1(Tag tag1) {
		this.tag1 = tag1;
	}

	public Tag getTag2() {
		return tag2;
	}

	public void setTag2(Tag tag2) {
		this.tag2 = tag2;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
