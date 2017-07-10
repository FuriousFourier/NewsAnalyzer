package pl.edu.agh.Analyzer.model;

import org.h2.util.New;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
public class Country {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class)
    private List newspapers;

    @OneToOne(targetEntity = Tag.class)
    private Tag tag;

    public Country(String name, List newspapers, Tag tag) {
        this.name = name;
        this.newspapers = newspapers;
        this.tag = tag;
    }

    public Country() {
    }

    public List getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(List newspapers) {
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
}
