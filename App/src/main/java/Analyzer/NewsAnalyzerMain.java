package Analyzer;

import Analyzer.info.InfoContainer;
import Analyzer.util.DbUtil;
import Analyzer.util.FeedDownloaderWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import Analyzer.secondProject.tagger.MainTagger;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by pawel on 07.07.17.
 */


@SpringBootApplication
public class NewsAnalyzerMain {
    public static final long DENOMINATOR = 1000000000;
    private static ConfigurableApplicationContext configurableApplicationContext;
    private static Long securityNumber;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line;
		Random random = new Random(System.currentTimeMillis());
		securityNumber = random.nextLong();
		configurableApplicationContext = SpringApplication.run(NewsAnalyzerMain.class, args);
		try {
			MainTagger.initializeMainTagger();
		} catch (IOException e) {
			System.err.println("Error occurred");
			e.printStackTrace();
			System.exit(1);
		}

		DbUtil dbUtil = new DbUtil();

		System.out.println("Checking if DB is empty...");

		try {
			if (dbUtil.checkIfDbEmpty()){
				while (true) {
					System.out.println("DB seems to be empty. Would you like to initialize it?");
					String answer = scanner.nextLine();
					if (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes")) {
						try {
							if (!dbUtil.initDb()) {
								System.out.println("Something went wrong.");
								System.exit(2);
							}
							break;
						} catch (IOException e) {
							System.err.println("Error occurred. Look:");
							e.printStackTrace();
							System.exit(1);
						}
					} else if (answer.toLowerCase().equals("n") || answer.toLowerCase().equals("no")) {
						break;
					} else {
						System.out.println("Unknown answer. Do you understand my question?");
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Service seems to be dead. Additional information:");
			e.printStackTrace();
			System.exit(1);
		}

		FeedDownloaderWorker feedDownloaderWorker = new FeedDownloaderWorker(dbUtil);
		new Thread(feedDownloaderWorker).start();

        while (true) {
            System.out.println("Type \"p\" and I will do it. (You can type other things too)");
            line = scanner.nextLine();
			try {
				if (line.equals("p")) {
					if (!dbUtil.getNewFeeds(true)){
						System.out.println("Something went wrong");
					}
				} else if (line.equals("d")) {
					if (!dbUtil.addNewData()){
						System.out.println("Something went wrong");
					}
				} else if (line.equals("q")) {
					System.out.println("Bye");
					System.exit(0);
				} else if(line.equals("tl")){
					MainTagger.tagNewFeeds(InfoContainer.NEW_FEEDS_PATH, 1);
				} else if (line.equals("cs")) {
					if (!dbUtil.createCurrencyTagStats()) {
						System.out.println("Something went wrong");
					}
				} else if (line.equals("ts")) {
					if (!dbUtil.createTagStats()) {
						System.out.println("Something went wrong");
					}
				} else if (line.equals("csfn")) {
					if (!dbUtil.createCurrencyTagStatsForNewspapers()) {
						System.out.println("Something went wrong");
					}
				} else if (line.equals("to")){
					MainTagger.tagGeomedia(1);
				} else if (line.equals("fbn")) {
					if (!dbUtil.findBiggerNewspaper()) {
						System.out.println("Something went wrong");
					}
				} else if (line.equals("aef")) {
					if (!dbUtil.addExistingFeeds(false)) {
						System.out.println("Something went wrong");
					}
				} else if (line.equals("cps")) {
					if (!dbUtil.createPairStats()) {
						System.out.println("Something went wrong");
					}
				} else if (line.startsWith("cpsfn")) {
					if (!dbUtil.createPairStatsForNewspaper()) {
						System.out.println("Something went wrong");
					}
				} else {
					System.out.println("Unknown command");
				}
			} catch (Exception e) {
				System.err.println("Something went wrong :/");
				e.printStackTrace();
			}
		}

    }

    private static void tagAllFeeds() throws IOException {
		MainTagger.tagNewFeeds(InfoContainer.NEW_FEEDS_PATH, 1);
		MainTagger.tagGeomedia(3);
	}

    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }

	public static Long getSecurityNumber() {
		return securityNumber;
	}
}
