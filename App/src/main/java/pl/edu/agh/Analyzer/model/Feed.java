package pl.edu.agh.Analyzer.model;

import org.h2.util.New;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import javax.persistence.*;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
public class Feed {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String section;

    @OneToMany(targetEntity = PressRelease.class)
    private List pressReleases;

    @ManyToOne(targetEntity = Newspaper.class)
    private Newspaper newspaper;

    public Feed(String name, String section) {
        this.name = name;
        this.section = section;
    }

    public Feed() {
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
}
