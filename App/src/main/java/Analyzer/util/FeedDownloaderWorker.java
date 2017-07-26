package Analyzer.util;
import java.io.IOException;
import java.util.Date;

public class FeedDownloaderWorker implements Runnable {
	private DbUtil dbUtil;

	@Override
	public void run() {
		Date previousTime = new Date();
		while (true) {
			Date currentTime = new Date();
			if (currentTime.getDate() != previousTime.getDate()) {
				previousTime = currentTime;
				try {
					dbUtil.getNewFeeds();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public FeedDownloaderWorker(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public void setDbUtil(DbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
}
