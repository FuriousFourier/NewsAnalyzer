package database.POJO;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pressreleases")
public class PressRelease {
    @Id@GeneratedValue
    @Column
    private int ID;
    @ManyToOne
    @JoinColumn(name = "feedID")
    private Feed feedID;
    @Column(length = 10000)
    private String title;
    @Column
    private Date date;
    @Column(length = 100000)
    private String content;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pressReleaseID")
    private Set<PressReleasesTag> pressReleasesTags = new HashSet<PressReleasesTag>(0);

    public PressRelease() {
        //Constructor for Hibernate
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Feed getFeedID() {
        return feedID;
    }

    public void setFeedID(Feed feedID) {
        this.feedID = feedID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<PressReleasesTag> getPressReleasesTags() {
        return pressReleasesTags;
    }

    public void setPressReleasesTags(Set<PressReleasesTag> pressReleasesTags) {
        this.pressReleasesTags = pressReleasesTags;
    }
}
