package pl.edu.agh.Analyzer.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
public class Language {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    @OneToMany(targetEntity = Newspaper.class)
    private List newspaperList;

    public Language(String name, List newspaperList) {
        this.name = name;
        this.newspaperList = newspaperList;
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

    public List getNewspaperList() {
        return newspaperList;
    }

    public void setNewspaperList(List newspaperList) {
        this.newspaperList = newspaperList;
    }
}
