package database.POJO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "languages")
public class Language {
    @Id@GeneratedValue
    @Column
    private int ID;
    @Column(length = 25)
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "languageID")
    private Set<Newspaper> newspapers = new HashSet<Newspaper>(0);

    public Language() {
    }

    public Language(int id) {
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
