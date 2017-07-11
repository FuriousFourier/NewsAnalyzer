package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
@Table(name = "Pressreleases")
public class PressRelease {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Integer id;

    private String title;

    private Date date;

    private String content;

    @ManyToMany(targetEntity = Tag.class, cascade = CascadeType.ALL)
    @JoinTable(name = "Pressreleasestag", joinColumns = @JoinColumn(name = "pressreleaseid", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "tagid", referencedColumnName = "ID"))
    private List<Tag> tags;

    @ManyToOne(targetEntity = Feed.class)
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
}
