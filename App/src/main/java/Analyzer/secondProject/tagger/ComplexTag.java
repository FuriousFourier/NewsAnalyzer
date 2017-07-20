package Analyzer.secondProject.tagger;

import java.util.HashSet;
import java.util.Set;

public class ComplexTag {
	protected String name;
	protected Set<String> keyWords;

	public ComplexTag(String name) {
		this.name = name;
		this.keyWords = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(Set<String> keyWords) {
		this.keyWords = keyWords;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof ComplexTag && name.equals(((ComplexTag) object).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}
}
