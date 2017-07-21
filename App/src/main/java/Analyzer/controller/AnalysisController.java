package Analyzer.controller;

import au.com.bytecode.opencsv.CSVWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import Analyzer.model.*;
import Analyzer.repository.*;
import Analyzer.ui.GraphHandler;
import Analyzer.ui.ReportCreator;
import Analyzer.ui.ReportInput;

import java.io.*;
import java.util.*;
import java.util.List;

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
    private Calendar cal = Calendar.getInstance();
    ReportCreator reportCreator = new ReportCreator();

    public static void setIsAskingForValue(boolean val){
        isAskingForValue = val;
    }
    public static void setIsIteratingOverDates(boolean val) { isIteratingOverDates = val; }

    @GetMapping("/results")
    public String printResults(){
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
    public String getAllNewspapers(){
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
    public String getPressReleasesSortedByDate(){
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
    public String getPressReleasesByDate(){

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
    public String getPressReleasesByNews(){
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
	public String getNotesForOneNewspaper(){
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
	public String analyseByDate() throws IOException, DocumentException {
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
		List<ReportInput> inputs = new ArrayList<>();
		Document report = new Document();
		PdfWriter.getInstance(report, new FileOutputStream("src/main/resources/reports/Month.pdf"));
		report.open();
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
		Paragraph p = new Paragraph("Months", titleFont);
		report.add(p);

		String graphFileName = "src/main/resources/csv/Month.csv";
		String nodesFileName = "src/main/resources/csv/Month_nodes.csv";
		File graphFile = new File(graphFileName);
		File nodesFile = new File(nodesFileName);
		//if (!graphFile.isFile())
		graphFile.delete();
		//if(!nodesFile.isFile())
		nodesFile.delete();
		graphFile.createNewFile();
		nodesFile.createNewFile();
		CSVWriter graphWriter = new CSVWriter(new FileWriter(graphFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);
		CSVWriter nodesWriter = new CSVWriter(new FileWriter(nodesFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);
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
					GraphHandler.resetInput();
					GraphHandler.graphCreator(value, "", fetchedNotes, report, graphWriter, nodesWriter, fetchedTags, initColumns);
					initColumns = false;
					/*ReportInput input = GraphHandler.getInput();
					if (input != null)
						inputs.add(GraphHandler.getInput());*/
				}
			}

		}
		//reportCreator.showChart(inputs, report);
		report.close();
		graphWriter.close();
		nodesWriter.close();
		return "foo";
	}
	@GetMapping("/analyseNewspaper")
	public String analyseByNewspaper() throws IOException, DocumentException {
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		if (getAllNewspapers().equals("foo")){ //we're sure it's finished
			System.out.println("Newspapers have been fetched");
		}

		fetchedTags = new TreeSet<>((List<Tag>)tagRepository.findAll());

		List<ReportInput> newsInputs = new ArrayList<>();
		Document newsReport = new Document();
		String graphGlobalFileName;
		String nodesGlobalFileName;
		File globalGraphFile;
		File globalNodesFile;
		CSVWriter graphGlobalWriter = null;
		CSVWriter nodesGlobalWriter = null;
		boolean initColumns = true;
		if (!isIteratingOverDates) {
			PdfWriter.getInstance(newsReport, new FileOutputStream("src/main/resources/reports/Newspaper.pdf"));
			newsReport.open();
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
			Paragraph p = new Paragraph("Newspaper", titleFont);
			newsReport.add(p);

			graphGlobalFileName = "src/main/resources/csv/Newspaper.csv";
			nodesGlobalFileName = "src/main/resources/csv/Newspaper_nodes.csv";
			globalGraphFile = new File(graphGlobalFileName);
			globalNodesFile = new File(nodesGlobalFileName);
			System.out.println("Is graphFIle deleted: " + globalGraphFile.delete());
			System.out.println("Is nodeFIle deleted: " + globalNodesFile.delete());
			globalGraphFile.createNewFile();
			globalNodesFile.createNewFile();
			graphGlobalWriter = new CSVWriter(new FileWriter(graphGlobalFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			nodesGlobalWriter = new CSVWriter(new FileWriter(nodesGlobalFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);
		}
		//String[] newspaperList = { "Interia", "Fakt", "Newsweek", "RMF24", "Today", "China Daily"};
		for (Newspaper n : fetchedNewspapers) {
			value = n.getName();
		//for (String s: newspaperList) {
			//value = s;
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
						String date = pYear + "-" + (pMonth<10 ? "0" : "") + pMonth;
						newspaperNotes.putIfAbsent(date, new HashSet<PressRelease>());
						newspaperNotes.get(date).add(p);
						Set<Tag> tags = p.getTags();
						if (tags!=null && !tags.isEmpty()){
							System.out.print("ID: " + p.getId() + "; Date: "+ date+"; Tags:");
							for (Tag t: p.getTags()){
								System.out.print(t.getName()+", ");
							}
							System.out.println();
						}
					}
					fetchedNotes =null;
					SortedSet<String> notesKeySet = new TreeSet<>(newspaperNotes.keySet());
					List<ReportInput> inputs = new ArrayList<>();
					Document report = new Document();
					PdfWriter.getInstance(report, new FileOutputStream("src/main/resources/reports/"+value+".pdf"));
					report.open();
					Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
					Paragraph p = new Paragraph("Newspaper&date: " + value, titleFont);
					report.add(p);

					String graphFileName = "src/main/resources/csv/"+value+".csv";
					String nodesFileName = "src/main/resources/csv/"+value+"_nodes.csv";
					File graphFile = new File(graphFileName);
					File nodesFile = new File(nodesFileName);
					//if (!graphFile.isFile())
					System.out.println("Is graphFIle deleted: " + graphFile.delete());
					//if(!nodesFile.isFile())
					System.out.println("Is nodeFIle deleted: " + nodesFile.delete());
					graphFile.createNewFile();
					nodesFile.createNewFile();
					CSVWriter graphWriter = new CSVWriter(new FileWriter(graphFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);
					CSVWriter nodesWriter = new CSVWriter(new FileWriter(nodesFileName, true), '\t', CSVWriter.DEFAULT_QUOTE_CHARACTER);

					for (String d : notesKeySet){
						System.out.println("------> " + d);

						GraphHandler.resetInput();
						GraphHandler.graphCreator(d, value, newspaperNotes.get(d), report, graphWriter, nodesWriter, fetchedTags, initColumns);
						/*ReportInput input = GraphHandler.getInput();
						if (input != null)
							inputs.add(GraphHandler.getInput());*/
						initColumns = false;
					}
					//reportCreator.showChart(inputs, report);
					report.close();
					graphWriter.close();
					nodesWriter.close();

					//remove values
					newspaperNotes = null;
					notesKeySet = null;
					inputs = null;
				}
				else {
					GraphHandler.resetInput();
					GraphHandler.graphCreator("", value, fetchedNotes, newsReport, graphGlobalWriter, nodesGlobalWriter, fetchedTags, initColumns);
					/*ReportInput input = GraphHandler.getInput();
					if (input != null)
						newsInputs.add(GraphHandler.getInput());*/
					initColumns = false;
				}
			}
		}
		if (!isIteratingOverDates){
			//reportCreator.showChart(newsInputs, newsReport);
			newsReport.close();
			graphGlobalWriter.close();
			nodesGlobalWriter.close();
		}
		return "foo";
	}

	@GetMapping("/getTags")
	public String getAllTags(){
		if (tagRepository == null){
			System.out.println("Kiepsko");
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