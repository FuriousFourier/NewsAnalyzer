package Analyzer.secondProject.tagger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by pawel on 19.07.17.
 */
public class CurrencyTag extends ComplexTag {
	private String mainKeyword;

	public CurrencyTag(String name, String mainKeyword) {
		super(name);
		this.mainKeyword = mainKeyword;
	}

	public String getMainKeyword() {
		return mainKeyword;
	}

	public void setMainKeyword(String mainKeyword) {
		this.mainKeyword = mainKeyword;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof CurrencyTag && name.equals(((CurrencyTag) o).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}
}
