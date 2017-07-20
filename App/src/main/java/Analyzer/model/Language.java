package Analyzer.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Languages")
public class Language {

    private final static String DUMMY_NAME = "UNNAMED_LANGUAGE";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class, mappedBy = "language")
    private Set<Newspaper> newspapers;

    public Language(String name, Set<Newspaper> newspapers) {
        this.name = name;
        this.newspapers = newspapers;
    }

    public Language() {
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

    public Set<Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(Set<Newspaper> newspapers) {
        this.newspapers = newspapers;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Language && this.name.equals(((Language) o).getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
