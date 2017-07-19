package rss;


import download.DownloadedFeed;
import download.FeedWriter;
import rss.reader.RssReader;

public class Main {
    public static void main(String[] args){
        DownloadedFeed[] downloadedFeeds = {
                new DownloadedFeed("fr_FRA_lmonde_int","http://rss.lemonde.fr/c/205/f/3052/index.rss"),
                new DownloadedFeed("en_CHN_mopost_int", "http://www.scmp.com/rss/91/feed"),
                new DownloadedFeed("en_IND_tindia_int" ,"http://timesofindia.indiatimes.com/rssfeeds/1221656.cms"),
                new DownloadedFeed("es_MEX_univer_int", "http://archivo.eluniversal.com.mx/rss/mundo.xml"),
                new DownloadedFeed("en_USA_nytime_int", "http://rss.nytimes.com/services/xml/rss/nyt/World.xml"),
                new DownloadedFeed("en_CAN_starca_int", "http://www.thestar.com/feeds.articles.news.world.rss"),
                new DownloadedFeed("en_IND_hindti_int", "http://www.hindustantimes.com/rss/world/rssfeed.xml"),
                new DownloadedFeed("en_NZL_nzhera_int", "http://rss.nzherald.co.nz/rss/xml/nzhrsscid_001503711.xml"),
                new DownloadedFeed("en_SGP_twoday_int", "http://www.todayonline.com/feed/world"),
                new DownloadedFeed("en_ZWE_chroni_int", "http://www.chronicle.co.zw/feed/"),
                new DownloadedFeed("es_BOL_patria_int", "http://lapatriaenlinea.com/rss/Internacional.xml"),
                new DownloadedFeed("es_ESP_catalu_int", "http://www.elperiodico.com/es/rss/internacional/rss.xml"),
                new DownloadedFeed("es_ESP_elpais_int", "http://ep00.epimg.net/rss/internacional/portada.xml"),
                new DownloadedFeed("fr_BEL_derheu_int", "http://www.dhnet.be/rss/section/actu.xml"),
                new DownloadedFeed("fr_BEL_lesoir_int", "http://www.lesoir.be/feed/Actualit%C3%A9/Fil%20Info/destination_principale_block"),
                new DownloadedFeed("fr_CAN_jmontr_int", "http://www.journaldemontreal.com/actualite/rss.xml"),
                new DownloadedFeed( "fr_DZA_elwata_int", "http://www.elwatan.com/international/rss.xml"),
                new DownloadedFeed("pl_POL_wp_int", "http://wiadomosci.wp.pl/rss.xml"),
                new DownloadedFeed("pl_POL_interia_int", "http://fakty.interia.pl/feed"),
                new DownloadedFeed("pl_POL_dorzeczy_int", "https://dorzeczy.pl/feed/swiat/"),
                new DownloadedFeed("pl_POL_fakt_int", "http://www.fakt.pl/rss"),
                new DownloadedFeed("pl_POL_newsweek_int", "http://www.newsweek.pl/rss.xml"),
                new DownloadedFeed("pl_POL_tvn24_int", "http://www.tvn24.pl/najnowsze.xml"),
                new DownloadedFeed("pl_POL_rmf24_int", "http://www.rmf24.pl/fakty/swiat/feed")


        };
        RssReader reader = new RssReader();
        reader.readAndWriteToFile(downloadedFeeds);
        FeedWriter.writeFeeds(downloadedFeeds);
    }
}
