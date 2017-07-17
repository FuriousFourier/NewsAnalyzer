package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Feeds")
public class Feed {

    private final static String DUMMY_NAME = "UNNAMED_FEED";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String section;

    @OneToMany(targetEntity = PressRelease.class, mappedBy = "feed")
    private Set<PressRelease> pressReleases;

    @ManyToOne(targetEntity = Newspaper.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "newspaperID", referencedColumnName = "ID")
    private Newspaper newspaper;

    public Feed(String name, String section) {
        this.name = name;
        this.section = section;
    }

    public Feed() {
        this.name = DUMMY_NAME;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Set<PressRelease> getPressReleases() {
        return pressReleases;
    }

    public void setPressReleases(Set<PressRelease> pressReleases) {
        this.pressReleases = pressReleases;
    }

    public Newspaper getNewspaper() {
        return newspaper;
    }

    public void setNewspaper(Newspaper newspaper) {
        this.newspaper = newspaper;
    }

    @Override
    public boolean equals(Object feed) {
        return feed instanceof Feed && this.name.equals(((Feed) feed).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
