package database.POJO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "countries")
public class Country {
    @Id@GeneratedValue
    @Column
    private int ID;
    @Column(length = 100)
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "countryID")
    private Set<Newspaper> newspapers = new HashSet<Newspaper>(0);
    @OneToOne(mappedBy = "country")
    private TAG tag;

    public Country(Integer id, String name) {
        this.ID = id;
        this.name = name;
    }

    public TAG getTag() {
        return tag;
    }

    public void setTag(TAG tag) {
        this.tag = tag;
    }

    public Country() {
    }

    public Country(int id) {
        this.ID = id;
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

    public Set<Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(Set<Newspaper> newspapers) {
        this.newspapers = newspapers;
    }
}
