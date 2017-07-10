package pl.edu.agh.Analyzer.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */

@Entity
public class PressRelease {

    @Id
    @GeneratedValue
    private Integer id;

    private String title;

    private Date date;

    private String content;

    @ManyToMany(targetEntity = Tag.class)
    private List tags;

    @ManyToOne(targetEntity = Feed.class)
    private Feed feed;

    public PressRelease(String title, Date date, String content, List tags, Feed feed) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.tags = tags;
        this.feed = feed;
    }

    public PressRelease() {
    }

    public List getTags() {
        return tags;
    }

    public void setTags(List tags) {
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
