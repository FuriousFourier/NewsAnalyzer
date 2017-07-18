package pl.edu.agh.Analyzer.support;

import pl.edu.agh.Analyzer.model.Feed;

import java.util.Date;

/**
 * Created by pawel on 14.07.17.
 */
public class PressReleaseId {
    private String title;
    private Date date;
    private Feed feed;

    public PressReleaseId(String title, Date date, Feed feed) {
        this.title = title;
        this.date = date;
        this.feed = feed;
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

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	@Override
    public boolean equals(Object object) {
        return object instanceof PressReleaseId && (this.date.compareTo(((PressReleaseId) object).getDate()) == 0) && (this.title.equals(((PressReleaseId) object).getTitle())) && (this.feed.equals(((PressReleaseId)object).getFeed()));
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() / 2 + this.title.hashCode() / 2 + this.feed.hashCode() / 2;
    }
}
