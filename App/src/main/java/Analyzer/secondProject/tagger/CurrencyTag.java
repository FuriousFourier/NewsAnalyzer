package Analyzer.secondProject.tagger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by pawel on 19.07.17.
 */
public class CurrencyTag {
	private String name;
	private String mainKeyword;
	private Set<String> subKeywords;

	public CurrencyTag(String name, String mainKeyword) {
		this.name = name;
		this.mainKeyword = mainKeyword;
		this.subKeywords = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMainKeyword() {
		return mainKeyword;
	}

	public void setMainKeyword(String mainKeyword) {
		this.mainKeyword = mainKeyword;
	}

	public Set<String> getSubKeywords() {
		return subKeywords;
	}

	public void setSubKeywords(Set<String> subKeywords) {
		this.subKeywords = subKeywords;
	}
}
