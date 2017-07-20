package Analyzer.secondProject.database.POJO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "newspapers")
public class Newspaper {
    @Id@GeneratedValue
    @Column
    private int ID;
    @Column(length = 100)
    private String name;
    @ManyToOne
    @JoinColumn(name = "countryID")
    private Country countryID;
    @ManyToOne
    @JoinColumn(name = "languageID")
    private Language languageID;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "newspaperID")
    private Set<Feed> feeds = new HashSet<Feed>(0);

    public Newspaper() {
        //Constructor for Hibernate
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountryID() {
        return countryID;
    }

    public void setCountryID(Country countryID) {
        this.countryID = countryID;
    }

    public Language getLanguageID() {
        return languageID;
    }

    public void setLanguageID(Language languageID) {
        this.languageID = languageID;
    }
}
