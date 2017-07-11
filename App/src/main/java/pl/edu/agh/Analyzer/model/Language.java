package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Languages")
public class Language {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class)
    @JoinColumn(name = "languageID")
    private List newspapers;

    public Language(String name, List newspapers) {
        this.name = name;
        this.newspapers = newspapers;
    }

    public Language() {
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

    public List getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(List newspapers) {
        this.newspapers = newspapers;
    }
}
