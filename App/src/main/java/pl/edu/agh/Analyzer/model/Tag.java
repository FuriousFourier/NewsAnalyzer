package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "TAGs")
public class Tag {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    /*@OneToOne(targetEntity = Country.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "countryid", referencedColumnName = "ID")*/
    @OneToOne (targetEntity = Country.class, mappedBy = "tag")
    private Country country;

    @ManyToMany(mappedBy = "tags")
    private List<PressRelease> pressReleases;

    public Tag(String name, Country country, List<PressRelease> pressReleases) {
        this.name = name;
        this.country = country;
        this.pressReleases = pressReleases;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public List<PressRelease> getPressReleases() {
        return pressReleases;
    }

    public void setPressReleases(List<PressRelease> pressReleases) {
        this.pressReleases = pressReleases;
    }

    public Tag() {
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
    public boolean equals(Object object) {
        return object instanceof Tag && this.name.equals(((Tag) object).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
