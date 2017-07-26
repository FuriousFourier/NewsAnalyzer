package Analyzer.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Countries")
public class Country {

	public static final String UNKNOWN_COUNTRY_NAME = "UNKNOWN COUNTRY";
	final static private String DUMMY_NAME = "NOWHERE";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class, mappedBy = "country")
    private Set<Newspaper> newspapers;

    @OneToMany(targetEntity = Tag.class, mappedBy = "country")
    private Set<Tag> tags;

    public Country(String name, Set<Newspaper> newspapers, Set<Tag> tags) {
        this.name = name;
        this.newspapers = newspapers;
        this.tags = tags;
    }

    public Country() {
        this.name = DUMMY_NAME;
    }

    public Set<Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(Set<Newspaper> newspapers) {
        this.newspapers = newspapers;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object country) {
        return country instanceof Country && this.name.equals(((Country) country).getName());
    }

    @Override
    public int hashCode(){
        return this.name.hashCode();
    }

}
