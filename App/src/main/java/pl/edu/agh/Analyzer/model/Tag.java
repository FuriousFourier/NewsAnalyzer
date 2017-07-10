package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
public class Tag {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToOne(targetEntity = Country.class)
    private Country country;

    @ManyToMany(targetEntity = PressRelease.class)
    private List pressReleases;

    public Tag(String name, Country country, List pressReleases) {
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

    public List getPressReleases() {
        return pressReleases;
    }

    public void setPressReleases(List pressReleases) {
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
}
