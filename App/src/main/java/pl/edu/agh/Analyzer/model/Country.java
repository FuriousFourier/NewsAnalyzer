package pl.edu.agh.Analyzer.model;

import org.h2.util.New;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Countries")
public class Country {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class, mappedBy = "country")
    private Set<Newspaper> newspapers;

    @OneToOne(targetEntity = Tag.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "countryid", referencedColumnName = "ID")
    private Tag tag;

    public Country(String name, Set<Newspaper> newspapers, Tag tag) {
        this.name = name;
        this.newspapers = newspapers;
        this.tag = tag;
    }

    public Country() {
    }

    public Set<Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(Set<Newspaper> newspapers) {
        this.newspapers = newspapers;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
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
