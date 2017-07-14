package pl.edu.agh.Analyzer.model;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

//date + title is unique
@Entity
@Table(name = "Pressreleases")
public class PressRelease {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Integer id;

    @Length(max = 10000)
    private String title;

    private Date date;

    @Length(max = 100000)
    private String content;

    @ManyToMany(targetEntity = Tag.class, cascade = {CascadeType.ALL})
    @JoinTable(name = "Pressreleasestag", joinColumns = @JoinColumn(name = "pressreleaseid", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "tagid", referencedColumnName = "ID"))
    private List<Tag> tags;

    @ManyToOne(targetEntity = Feed.class, cascade = {CascadeType.ALL})
    @JoinColumn(name = "feedID", referencedColumnName = "ID")
    private Feed feed;

    public PressRelease(String title, Date date, String content, List<Tag> tags, Feed feed) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.tags = tags;
        this.feed = feed;
    }

    public PressRelease() {
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public boolean equals(Object object) {
        return object instanceof PressRelease && (this.date.compareTo(((PressRelease) object).getDate()) == 0) && (this.title.equals(((PressRelease) object).getTitle()));
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() / 2 + this.title.hashCode() / 2;
    }
}
