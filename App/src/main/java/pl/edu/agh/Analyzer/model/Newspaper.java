package pl.edu.agh.Analyzer.model;

import org.hibernate.mapping.Join;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Newspapers")
public class Newspaper {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @ManyToOne(targetEntity = Language.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "languageID", referencedColumnName = "ID")
    private Language language;

    @ManyToOne (targetEntity = Country.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "countryID", referencedColumnName = "ID")
    private Country country;

    @OneToMany(targetEntity = Feed.class, mappedBy = "newspaper")
    private List<Feed> feeds;

    public Newspaper(String name, Language language, Country country, List<Feed> feeds) {
        this.name = name;
        this.language = language;
        this.country = country;
        this.feeds = feeds;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public Newspaper() {
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
}
