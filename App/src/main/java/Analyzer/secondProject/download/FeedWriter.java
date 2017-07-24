package Analyzer.secondProject.download;

import Analyzer.secondProject.tagger.MainTagger;
import au.com.bytecode.opencsv.CSVWriter;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class FeedWriter {

	public static final String FEED_DIRECTORY_PATH = "../SecondProject/Projekt-IO01/FeedsAnalyzer-master/Feeds";

	public static void writeFeeds(DownloadedFeed[] downloadedFeeds){
		File directory = new File(FEED_DIRECTORY_PATH);
		directory.mkdirs();
		for (DownloadedFeed feed: downloadedFeeds){
            String filename = FEED_DIRECTORY_PATH + "/" + feed.getName() +".csv";
            if (feed.getSyndFeed() != null) {
                writeFeed(filename, feed.getSyndFeed(), feed.getName());
            } else {
                System.out.println("null dla " + feed.getName());
            }
        }
    }

    protected static void writeFeed(String filename, SyndFeed syndFeed, String feedTitle){
        if (syndFeed == null) {
			System.out.println("No syndFeed in " + filename);
			return;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CSVWriter writer = null;
        try {
			File file = new File(filename);
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file, true);
            writer = new CSVWriter(fileWriter, ' ');
            List entries = syndFeed.getEntries();
            for (Object entry1 : entries) {
                final SyndEntry entry = (SyndEntry) entry1;
				if (entry.getDescription() != null) {
					writer.writeNext((feedTitle + "#" + dateFormat.format(entry.getPublishedDate()) + "#" + entry.getTitle() +
							"#" + entry.getDescription().getValue()).split("#"));
				}
				else {
					writer.writeNext((feedTitle + "#" + dateFormat.format(entry.getPublishedDate()) + "#" + entry.getTitle() +
							"#" + MainTagger.DEFAULT_CONTENT).split("#"));
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
