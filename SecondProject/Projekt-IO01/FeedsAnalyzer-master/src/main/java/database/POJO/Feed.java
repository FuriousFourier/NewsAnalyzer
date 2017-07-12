package database.POJO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "feeds")
public class Feed {
    @Id@GeneratedValue
    @Column
    private int ID;
    @Column(length = 100)
    private String name;
    @ManyToOne
    @JoinColumn(name = "newspaperID")
    private Newspaper newspaperID;
    @Column(length = 25)
    private String section;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feedID")
    private Set<PressRelease> pressReleases = new HashSet<PressRelease>(0);

    public Feed() {
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

    public Newspaper getNewspaperID() {
        return newspaperID;
    }

    public void setNewspaperID(Newspaper newspaperID) {
        this.newspaperID = newspaperID;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Set<PressRelease> getPressReleases() {
        return pressReleases;
    }

    public void setPressReleases(Set<PressRelease> pressReleases) {
        this.pressReleases = pressReleases;
    }
}
