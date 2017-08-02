package Analyzer.controller;

import Analyzer.MainUI;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.itextpdf.text.*;
import Analyzer.model.*;
import Analyzer.repository.*;
import Analyzer.ui.GraphHandler;
import Analyzer.ui.ReportCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static Analyzer.info.InfoContainer.analysisCsvPath;

/**
 * Created by karolina on 12.07.17.
 */
@Controller
public class AnalysisController {
	@Autowired
    private FeedRepository feedRepository;
	@Autowired
    private NewspaperRepository newspaperRepository;
	@Autowired
	private LanguageRepository languageRepository;
	@Autowired
	private CountryRepository countryRepository;
	@Autowired
	private PressReleaseRepository pressReleaseRepository;
	@Autowired
	private TagRepository tagRepository;

    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private List<Newspaper> fetchedNewspapers = new ArrayList<>();
    private Set<PressRelease> fetchedNotes = new HashSet<>();
    private SortedSet<Tag> fetchedTags = new TreeSet<>();
    private Date firstDate = new Date();
    private Date lastDate = new Date();
	private String value;
	private Newspaper currentNewspaper;
    private static boolean isAskingForValue = false;
    private static boolean isIteratingOverDates = false;
    private static boolean isIteratingOverWeeks = false;
    private static boolean isIteratingOverDays = false;
    private Calendar cal = Calendar.getInstance();
	private List<String> newspaperTitles = new ArrayList<>();
	private String titleToCompare = "";
	private int nrOfNewspapers;
	private String date1, date2;
	private String reportTitle;
	private boolean isChosenToCompare;
	private String descr;
	private String dateToGet;

    ReportCreator reportCreator = new ReportCreator();

    public synchronized static void setIsAskingForValue(boolean val){
        isAskingForValue = val;
    }
    public synchronized static void setIsIteratingOverDates(boolean val) { isIteratingOverDates = val; }
	public synchronized static void setIsIteratingOverWeeks(boolean val) { isIteratingOverWeeks = val; }
	public synchronized static void setIsIteratingOverDays(boolean val) { isIteratingOverDays = val; }


	@GetMapping("/results")
	public synchronized String printResults(){
        System.out.println("Result: ");
        for (PressRelease pr : fetchedNotes) {
            System.out.print("ID: " + pr.getId() + "; Tags:");
            for (Tag t: pr.getTags()){
                System.out.print(t.getName()+", ");
            }
            System.out.print("Country:" + pr.getFeed().getNewspaper().getCountry().getName() + "; Newspaper:" + pr.getFeed().getNewspaper().getName() + "\n");
        }
        return "foo";
    }
	@GetMapping("/news")
    public synchronized String getAllNewspapers(){
        System.out.println("Newspapers");
        if (newspaperRepository== null){
            System.out.println("Repository not initialised");
            return("foo");
        }
        List<Newspaper> result = (List<Newspaper>)newspaperRepository.findAll();
        for (Newspaper n : result) {
            System.out.println(n.getName());
        }
        fetchedNewspapers = result;
        return "foo";
    }
	@GetMapping("/dates")
    public synchronized String getPressReleasesSortedByDate(){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return "foo";
        }
        List<PressRelease> result = pressReleaseRepository.getSortedByDate();
        int size = result.size();
        firstDate = result.get(0).getDate();
        lastDate = result.get(size-1).getDate();
        System.out.println("First date: " + firstDate);
        System.out.println("Last date: " + lastDate);

      return "foo";
    }
	@GetMapping("/notesDate")
    public synchronized  String getPressReleasesByDate(){

        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return "foo";
        }
        String date = null;
        if (isAskingForValue) {
            try {
                date = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            date = value;

        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        Integer monthInt = Integer.parseInt(month);
        Integer yearInt = Integer.parseInt(year);

      Set<PressRelease> result = pressReleaseRepository.findByMonthAndYear(monthInt, yearInt);
      if (result == null || result.size() < 1){
          System.out.println("Couldn't find current date");
          return "foo";
      }
      fetchedNotes = result;
      if (isAskingForValue)
          printResults();
        return "foo";
    }
	@GetMapping("/notesNews")
    public synchronized String getPressReleasesByNews(){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return "foo";
        }
        String title = null;
        if (isAskingForValue) {
            try {
                title = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            title = value;
        System.out.println("Title: " + title);
		Newspaper newspaper = newspaperRepository.findByName(title); //find newspaper
		fetchedNotes = new HashSet<>();
        if (newspaper != null) {
			currentNewspaper = newspaper;
        	getNotesForOneNewspaper();
        }
        if (fetchedNotes.size() < 1) {
            System.out.println("Couldn't find feeds");
            return "foo";
        }
        if (isAskingForValue)
            printResults();
        return "foo";
    }
	@GetMapping("/notesForOneNewspaper")
	public synchronized String getNotesForOneNewspaper(){
		Set<Feed> feeds = currentNewspaper.getFeeds();//get feeds
		Set<PressRelease> result;
		for (Feed f : feeds) {//find notes for feed
			result = pressReleaseRepository.findByFeed(f);
			if (result != null)
				fetchedNotes.addAll(result);
		}
		return "foo";
	}
	@GetMapping("/analyseDate")
	public synchronized String analyseByDate() throws IOException, DocumentException {
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		fetchedTags = new TreeSet<>((List<Tag>)tagRepository.findAll());
		int firstMonth=0, firstYear=0, lastMonth=0, lastYear=0;
		if (getPressReleasesSortedByDate().equals("foo")){ //we're sure it's finished
			cal.setTime(firstDate);
			firstMonth = cal.get(Calendar.MONTH)+1;
			firstYear = cal.get(Calendar.YEAR);
			cal.setTime(lastDate);
			lastMonth  = cal.get(Calendar.MONTH)+1;
			lastYear = cal.get(Calendar.YEAR);
			System.out.println("First month: " + firstMonth + "; first year: "+ firstYear);
			System.out.println("Last month: "+ lastMonth + "; last year: "+ lastYear);
		}

		String graphFileName = analysisCsvPath + "Month.csv";
		String nodesFileName = analysisCsvPath + "Month_nodes.csv";
		String edgesFileName = analysisCsvPath + "Month_edges.csv";
		File graphFile = new File(graphFileName);
		File nodesFile = new File(nodesFileName);
		File edgesFile = new File(edgesFileName);
		graphFile.delete();
		nodesFile.delete();
		edgesFile.delete();
		graphFile.createNewFile();
		nodesFile.createNewFile();
		edgesFile.createNewFile();
		CSVWriter graphWriter = new CSVWriter(new FileWriter(graphFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter nodesWriter = new CSVWriter(new FileWriter(nodesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter edgesWriter = new CSVWriter(new FileWriter(edgesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);

		boolean initColumns = true;

		for (int i = firstYear; i <= lastYear; i++) {
			int j; int lastJ;
			j = ((i == firstYear) ? firstMonth : 1);
			lastJ = ((i == lastYear) ? lastMonth : 12);

			for (; j <= lastJ; j++) {
				value = i + "-" + (j<10 ? "0" : "") + j;
				setIsAskingForValue(false);
				if (getPressReleasesByDate().equals("foo") && fetchedNotes != null && !fetchedNotes.isEmpty()) {
					System.out.println("******************* *Date: "+value + " ************************");
					GraphHandler.reset();
					GraphHandler.initGraphFromPressReleases(fetchedNotes);
					GraphHandler.graphCreator(value, "", fetchedTags, graphWriter, nodesWriter, edgesWriter, initColumns);
					initColumns = false;
				}
			}

		}
		graphWriter.close();
		nodesWriter.close();
		return "foo";
	}
	@GetMapping("/analyseNewspaper")
	public synchronized String analyseByNewspaper() throws IOException, DocumentException {
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		if (getAllNewspapers().equals("foo")){ //we're sure it's finished
			System.out.println("Newspapers have been fetched");
		}

		fetchedTags = new TreeSet<>((List<Tag>)tagRepository.findAll());
		String graphGlobalFileName;
		String nodesGlobalFileName;
		String edgesGlobalFileName;
		File globalGraphFile;
		File globalNodesFile;
		File globalEdgesFile;
		CSVWriter graphGlobalWriter = null;
		CSVWriter nodesGlobalWriter = null;
		CSVWriter edgesGlobalWriter = null;
		boolean initColumns = true;
		String descr = "";
		if (isIteratingOverDays)
			descr = "days";
		else if (isIteratingOverWeeks)
			descr ="weeks";
		else if (isIteratingOverDates)
			descr = "months";
		if (!isIteratingOverDates) {
			graphGlobalFileName = analysisCsvPath + "Newspaper.csv";
			nodesGlobalFileName = analysisCsvPath + "Newspaper_nodes.csv";
			edgesGlobalFileName = analysisCsvPath + "Newspaper_edges.csv";
			globalGraphFile = new File(graphGlobalFileName);
			globalNodesFile = new File(nodesGlobalFileName);
			globalEdgesFile = new File(edgesGlobalFileName);

			globalGraphFile.delete();
			globalNodesFile.delete();
			globalEdgesFile.delete();
			globalGraphFile.createNewFile();
			globalNodesFile.createNewFile();
			globalEdgesFile.createNewFile();
			graphGlobalWriter = new CSVWriter(new FileWriter(graphGlobalFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
			nodesGlobalWriter = new CSVWriter(new FileWriter(nodesGlobalFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
			edgesGlobalWriter = new CSVWriter(new FileWriter(edgesGlobalFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
		}
		//String[] newspaperList = { "Interia", "Fakt", "Newsweek", "The New York Times"};
		String[] newspaperList = {"The Guardian", "New Zealand Herald"};
		for (Newspaper n : fetchedNewspapers) {
			value = n.getName();
		/*for (String s: newspaperList) {
			value = s;*/
			setIsAskingForValue(false);
			if ((getPressReleasesByNews().equals("foo")) && (fetchedNotes != null) && (!fetchedNotes.isEmpty())){
				System.out.println("******************* *Newspaper: "+value + " ************************");
				if (isIteratingOverDates){
					initColumns = true;
					Map<String, Set<PressRelease>> newspaperNotes = new HashMap<String, Set<PressRelease>>();
					//wrzucam notki do list w hashmapie
					for (PressRelease p : fetchedNotes){
						cal.setTime(p.getDate());
						int pMonth = cal.get(Calendar.MONTH)+1;
						int pYear = cal.get(Calendar.YEAR);
						int pDay = cal.get(Calendar.DAY_OF_MONTH);
						int pWeek = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH); //czy ok
						String date;
						if (isIteratingOverDays)
							date = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth + "-" + (pDay<10 ? "0" : "") + pDay;
						else if (isIteratingOverWeeks)
							date = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth + "w" + pWeek;
						else
							date = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth;
						newspaperNotes.putIfAbsent(date, new HashSet<>());
						newspaperNotes.get(date).add(p);
					}
					fetchedNotes =null;
					SortedSet<String> notesKeySet = new TreeSet<>(newspaperNotes.keySet());

					String graphFileName = analysisCsvPath+value+"("+descr+").csv";
					String nodesFileName = analysisCsvPath+value+"("+descr+")_nodes.csv";
					String edgesFileName = analysisCsvPath+value+"("+descr+")_edges.csv";
					File graphFile = new File(graphFileName);
					File nodesFile = new File(nodesFileName);
					File edgesFile = new File(edgesFileName);
					graphFile.delete();
					nodesFile.delete();
					edgesFile.delete();
					graphFile.createNewFile();
					nodesFile.createNewFile();
					edgesFile.createNewFile();
					CSVWriter graphWriter = new CSVWriter(new FileWriter(graphFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
					CSVWriter nodesWriter = new CSVWriter(new FileWriter(nodesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
					CSVWriter edgesWriter = new CSVWriter(new FileWriter(edgesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);

					for (String d : notesKeySet){
						System.out.println("------> " + d);

						GraphHandler.reset();
						GraphHandler.initGraphFromPressReleases(newspaperNotes.get(d));
						GraphHandler.graphCreator(d, value, fetchedTags, graphWriter, nodesWriter, edgesWriter, initColumns);
						initColumns = false;
					}
					graphWriter.close();
					nodesWriter.close();
					edgesWriter.close();

					//remove values
					newspaperNotes = null;
					notesKeySet = null;
				}
				else {
					GraphHandler.reset();
					GraphHandler.initGraphFromPressReleases(fetchedNotes);
					GraphHandler.graphCreator("", value, fetchedTags, graphGlobalWriter, nodesGlobalWriter, edgesGlobalWriter, initColumns);
					initColumns = false;
				}
			}
		}
		if (!isIteratingOverDates){
			graphGlobalWriter.close();
			nodesGlobalWriter.close();
			edgesGlobalWriter.close();
		}
		return "foo";
	}

	@GetMapping("/getDate")
	public synchronized String getDate() throws ParseException {
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		cal.setTime(formatter.parse(dateToGet));
		int pMonth = cal.get(Calendar.MONTH)+1;
		int pYear = cal.get(Calendar.YEAR);
		int pDay = cal.get(Calendar.DAY_OF_MONTH);
		int pWeek = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH); //czy ok

		if (isIteratingOverDays)
			dateToGet = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth + "-" + (pDay<10 ? "0" : "") + pDay;
		else if (isIteratingOverWeeks)
			dateToGet = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth + "w" + pWeek;
		else
			dateToGet = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth;
		return "foo";
	}
	@GetMapping("/broadAnalysis")
	public synchronized String chooseParams(){
		try {
			int nrOfTopTags = 0;
			fetchedTags = new TreeSet<>((List<Tag>) tagRepository.findAll());

			System.out.println("Choose option" +
					"\tm -> manually type newspapers' titles to compare together\n");
			String option = br.readLine();
			System.out.print("Choose the shortest time range to consider:" +
					"\t m -> month\n" +
					"\t w -> week\n" +
					"\t d -> day\n");
			String timeRange = br.readLine();
			descr = "";
			if (timeRange.startsWith("m")) {
				descr = "months";
				setIsIteratingOverDates(true);
			}
			else if (timeRange.startsWith("w")) {
				descr = "weeks";
				setIsIteratingOverWeeks(true);
			}
			else {
				descr = "days";
				setIsIteratingOverDays(true);
			}

			date1 = "2017-07-04";
			date2 = "2017-07-28";
			System.out.println("Enter date range");
			System.out.print("Date 1: ");
			date1 = br.readLine();
			System.out.print("Date 2: ");
			date2 = br.readLine();
			dateToGet = date1;
			getDate();
			date1 = dateToGet;
			System.out.println("Date 1: " + date1);

			dateToGet = date2;
			getDate();
			date2 = dateToGet;
			System.out.println("Date 2: " + date2);

			System.out.print("Nr of TOP tags to compare: ");
			nrOfTopTags = Integer.parseInt(br.readLine());
			if (option.startsWith("m")) {
				System.out.print("Nr of newspapers to compare: ");
				nrOfNewspapers = Integer.parseInt(br.readLine());
				System.out.println("Enter one title per line:");
				reportTitle = "";
				newspaperTitles = new ArrayList<>();
				for (int i = 0; i < nrOfNewspapers; i++) {
					titleToCompare = br.readLine();
					newspaperTitles.add(titleToCompare);
					reportTitle += titleToCompare + "_";
				}
			}
			System.out.println("Create pdf with charts? (t/n)");
			boolean willCreateReport = br.readLine().startsWith("t");

			if (option.startsWith("m")) {
				isChosenToCompare = false;
				compare();
			}

				Document report = null;
				try {
					String daysFileName = analysisCsvPath + reportTitle + "(" + date1 + "_" + date2 + ")_"+descr+".csv";
					String graphFileName = analysisCsvPath + reportTitle + "(" + date1 + "_" + date2 + ").csv";
					String nodesFileName = analysisCsvPath + reportTitle + "(" + date1 + "_" + date2 + ")_nodes.csv";
					if (willCreateReport)
						report = reportCreator.createReportBase(reportTitle + "(" + date1 + "_" + date2 + ")");
					System.out.println("Nr of newspaper titles: " + newspaperTitles.size());
					reportCreator.showChart(daysFileName, newspaperTitles.size(), report, reportTitle + "(" + date1 + "_" + date2 + ") - " + descr + " and " + descr, nrOfTopTags, 3,false, false, willCreateReport);
					reportCreator.showChart(graphFileName, newspaperTitles.size(), report, reportTitle + "(" + date1 + "_" + date2 + ")", nrOfTopTags, 3,false,false, willCreateReport);
					reportCreator.showChart(nodesFileName, newspaperTitles.size(), report, reportTitle + "(" + date1 + "_" + date2 + ")", nrOfTopTags, -1, true, true, willCreateReport);

					String daysNodesFileName = analysisCsvPath+reportTitle+"("+date1+"_"+date2+")_"+descr+"_TOP.csv";
					File nodeFile = new File(daysNodesFileName);

					nodeFile.delete();
					nodeFile.createNewFile();

					CSVWriter graphWriter = new CSVWriter(new FileWriter(daysNodesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);

					for (String n : newspaperTitles){
						System.out.println("Newspaper (in compare()): "+ n);
						String filePath = analysisCsvPath +n+"("+descr+")_nodes.csv";
						FileReader fileReader;
						try {
							fileReader = new FileReader(filePath);
						} catch(FileNotFoundException e){
							System.out.println("Necessary file ("+filePath+") hasn't been found - \""+n+"\" won't be taken into account");
							continue;
						}
						CSVReader reader = new CSVReader(fileReader, '\t');
						reportCreator.extractRelevantInputs(reader, graphWriter, date1, date2, true, true);
					}
					graphWriter.close();
					for (int i = 0; i < nrOfTopTags; i++){
						reportCreator.showChart(daysNodesFileName, newspaperTitles.size(), report, reportTitle + "(" + date1 + "_" + date2 + ") - " + descr + " and " + descr, nrOfTopTags, i+3, true, false, willCreateReport);
					}

				} catch (Exception e) {
					System.out.println("EXCEPTION IN chooseParams()");
					e.printStackTrace();
				} finally {
					if (report != null)
						report.close();
				}
		}catch (Exception e){
			System.out.println("EXCEPTION IN chooseParams()!!! (out try)");
			e.printStackTrace();
		}finally {

			return "foo";
		}
	}

	@GetMapping("/broadAnalysis2")
	public synchronized String compare() throws IOException, DocumentException, ParseException {
		String daysFileName = analysisCsvPath+reportTitle+"("+date1+"_"+date2+")_"+descr+".csv";
		File graphFile = new File(daysFileName);
		if (!isChosenToCompare) {
			graphFile.delete();
		}
		graphFile.createNewFile();

		CSVWriter graphWriter = new CSVWriter(new FileWriter(daysFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);

		for (String n : newspaperTitles){
			System.out.println("Newspaper (in compare()): "+ n);
			String filePath = analysisCsvPath+n+"("+descr+").csv";
			FileReader fileReader;
			try {
				fileReader = new FileReader(filePath);
			} catch(FileNotFoundException e){
				System.out.println("Necessary file ("+filePath+") hasn't been found - \""+n+"\" won't be taken into account");
				continue;
			}
			CSVReader reader = new CSVReader(fileReader, '\t');
			reportCreator.extractRelevantInputs(reader, graphWriter, date1, date2, true, false);
		}
		graphWriter.close();
		//utworzenie plikow zbiorczych
		String graphFileName = analysisCsvPath+reportTitle+"("+date1+"_"+date2+").csv";
		String nodesFileName = analysisCsvPath+reportTitle+"("+date1+"_"+date2+")_nodes.csv";
		String edgesFileName = analysisCsvPath+reportTitle+"("+date1+"_"+date2+")_edges.csv";
		graphFile = new File(graphFileName);
		File nodesFile = new File(nodesFileName);
		File edgesFile = new File(edgesFileName);
		if (!isChosenToCompare) {
			graphFile.delete();
			nodesFile.delete();
			edgesFile.delete();
		}
		graphFile.createNewFile();
		nodesFile.createNewFile();
		edgesFile.createNewFile();
		graphWriter = new CSVWriter(new FileWriter(graphFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter nodesWriter = new CSVWriter(new FileWriter(nodesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter edgesWriter = new CSVWriter(new FileWriter(edgesFileName, true), '\t', CSVWriter.NO_QUOTE_CHARACTER);

		for (String n : newspaperTitles) {
			//read appropriate files
			String filePath = analysisCsvPath+ n + "("+descr+")_edges.csv";
			FileReader fileReader;
			try {
				fileReader = new FileReader(filePath);
			} catch (FileNotFoundException e) {
				System.out.println("Necessary file ("+filePath+") hasn't been found - \""+n+"\" won't be taken into account");
				continue;
			}
			CSVReader reader = new CSVReader(fileReader, '\t');
			GraphHandler.reset();
			GraphHandler.initGraphFromCsv(date1, date2, reader);
			GraphHandler.graphCreator(date1 + "_" + date2, n, fetchedTags, graphWriter, nodesWriter, edgesWriter, true);
		}

		graphWriter.close();
		nodesWriter.close();
		edgesWriter.close();
		return "foo";
	}

	@GetMapping("/getTags")
	public synchronized String getAllTags(){
		if (tagRepository == null){
			return  "foo";
		}
		fetchedTags = new TreeSet<>((List<Tag>)tagRepository.findAll());
		System.out.println("Tags:");
		for (Tag t: fetchedTags){
			System.out.println(t.getName()+"; notes:");
			for (PressRelease p: t.getPressReleases()){
				System.out.println("\t"+p.getFeed().getNewspaper().getName());
			}
			System.out.println();
		}
		return "foo";
	}
}