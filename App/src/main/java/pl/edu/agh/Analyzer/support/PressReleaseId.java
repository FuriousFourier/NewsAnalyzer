package pl.edu.agh.Analyzer.support;

import java.util.Date;

/**
 * Created by pawel on 14.07.17.
 */
public class PressReleaseId {
    private String title;
    private Date date;

    public PressReleaseId(String title, Date date) {
        this.title = title;
        this.date = date;
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

    @Override
    public boolean equals(Object object) {
        return object instanceof PressReleaseId && (this.date.compareTo(((PressReleaseId) object).getDate()) == 0) && (this.title.equals(((PressReleaseId) object).getTitle()));
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() / 2 + this.title.hashCode() / 2;
    }
}
