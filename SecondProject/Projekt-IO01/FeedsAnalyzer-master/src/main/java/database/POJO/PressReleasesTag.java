package database.POJO;

import javax.persistence.*;

@Entity
@Table(name = "pressreleasestag")
public class PressReleasesTag {
    @Id@GeneratedValue
    @Column
    private int ID;
    @ManyToOne
    @JoinColumn(name = "pressReleaseID")
    private PressRelease pressReleaseID;
    @ManyToOne
    @JoinColumn(name = "tagID")
    private TAG tagID;

    public PressReleasesTag() {
        //Constructor for Hibernate
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public PressRelease getPressReleaseID() {
        return pressReleaseID;
    }

    public void setPressReleaseID(PressRelease pressReleaseID) {
        this.pressReleaseID = pressReleaseID;
    }

    public TAG getTagID() {
        return tagID;
    }

    public void setTagID(TAG tagID) {
        this.tagID = tagID;
    }
}
