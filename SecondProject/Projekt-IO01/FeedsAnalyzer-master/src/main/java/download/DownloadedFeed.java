package download;

import com.sun.syndication.feed.synd.SyndFeed;

public class DownloadedFeed {
    private String name;
    private String rssUrl;
    private SyndFeed syndFeed;


    public DownloadedFeed(String name, String rssUrl) {
        this.name = name;
        this.rssUrl = rssUrl;
    }

    public SyndFeed getSyndFeed() {
        return syndFeed;
    }

    public void setSyndFeed(SyndFeed syndFeed) {
        this.syndFeed = syndFeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }
}
