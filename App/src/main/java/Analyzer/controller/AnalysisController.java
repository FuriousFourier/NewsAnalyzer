package Analyzer.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import Analyzer.model.*;
import Analyzer.repository.*;
import Analyzer.ui.AnalysisHandler;
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
    private List<Feed> fetchedFeeds = new ArrayList<>();
    private List<Country> fetchedCountries = new ArrayList<>();
    private List<Language> fetchedLanguages = new ArrayList<>();
    private Set<PressRelease> fetchedNotes = new HashSet<>();
    private Date firstDate = new Date();
    private Date lastDate = new Date();
    private static boolean isAskingForValue = false;
    private String value;
    private static boolean isIteratingOverDates = false;
    ReportCreator reportCreator = new ReportCreator();

    public static void setIsAskingForValue(boolean val){
        isAskingForValue = val;
    }
    public static void setIsIteratingOverDates(boolean val) { isIteratingOverDates = val; }

    @RequestMapping("/ana")
    public String analysisIndex() {
        return "foo";
    }

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

    @GetMapping("/langs")
    public String getAllLanguages(){
        System.out.println("Languages:");
        if (languageRepository== null){
            System.out.println("Repository not initialised");
            return("foo");
        }
        List<Language> result = (List<Language>)languageRepository.findAll();
        for (Language l : result) {
            System.out.println(l.getName());
        }
        fetchedLanguages = result;
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

    @GetMapping("/countr")
    public String getAllCountries(){
        System.out.println("Countries (use tag in parenthesis to choose one):");
        if (countryRepository== null){
            System.out.println("Repository not initialised");
            return("foo");
        }
        List<Country> result = (List<Country>)countryRepository.findAll();
        for (Country c : result) {
            System.out.println(c.getName() + "("+c.getTag().getName()+")");
        }
        fetchedCountries = result;
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
        Set<PressRelease> notesFromAllFeeds = new HashSet<>();
        if (newspaper != null) {
            Set<Feed> feeds = newspaper.getFeeds();//get feeds
            Set<PressRelease> result;
            for (Feed f : feeds) {//find notes for feed
                result = pressReleaseRepository.findByFeed(f);
                if (result != null)
                    notesFromAllFeeds.addAll(result);
            }
        }
        if (notesFromAllFeeds.size() < 1) {
            System.out.println("Couldn't find feeds");
            return "foo";
        }
        fetchedNotes = notesFromAllFeeds;
        if (isAskingForValue)
            printResults();
        return "foo";
    }
    @GetMapping("/notesLangs")
    public String getPressReleasesByLangs(){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return "foo";
        }
        String name = null;
        if (isAskingForValue) {
            try {
                name = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            name = value;
        Set<PressRelease> notesFromAllFeeds = new HashSet<>();
        Language language = languageRepository.findByName(name);
        if(language != null) {
            for (Newspaper n : language.getNewspapers()) {
                if (n != null) {
                    Set<Feed> feeds = n.getFeeds();//get feeds
                    Set<PressRelease> result;
                    for (Feed f : feeds) {//find notes for feed
                        result = pressReleaseRepository.findByFeed(f);
                        if (result != null)
                            notesFromAllFeeds.addAll(result);
                    }
                }
            }
        }
        if (notesFromAllFeeds.size() < 1) {
            System.out.println("Couldn't find current feed");
            return "foo";
        }
        fetchedNotes = notesFromAllFeeds;
        if (isAskingForValue)
            printResults();
        return "foo";
    }
    @GetMapping("/notesCountr")
    public String getPressReleasesByCountries(){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return "foo";
        }
        String name = null;
        if (isAskingForValue) {
            try {
                name = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            name = value;
        Set<PressRelease> notesFromAllFeeds = new HashSet<>();
        Country country = countryRepository.findByName(name);
        if(country != null) {
            for (Newspaper n : country.getNewspapers()) {
                if (n != null) {
                    Set<Feed> feeds = n.getFeeds();//get feeds
                    Set<PressRelease> result;
                    for (Feed f : feeds) {//find notes for feed
                        result = pressReleaseRepository.findByFeed(f);
                        if (result != null)
                            notesFromAllFeeds.addAll(result);
                    }
                }
            }
        }
        if (notesFromAllFeeds.size() < 1) {
            System.out.println("Couldn't find current feed");
            return "foo";
        }
        fetchedNotes = notesFromAllFeeds;
        if (isAskingForValue)
            printResults();
        return "foo";
    }

    @GetMapping("/analyseDate")
    public String analyseByDate() throws FileNotFoundException, DocumentException {
      if (pressReleaseRepository == null){
        System.out.println("repository not initialised");
        return "foo";
      }

		int firstMonth=0, firstYear=0, lastMonth=0, lastYear=0;
		if (getPressReleasesSortedByDate().equals("foo")){ //we're sure it's finished
			firstMonth = firstDate.getMonth()+1;
			firstYear = firstDate.getYear()+1900;
			lastMonth  = lastDate.getMonth()+1;
			lastYear = lastDate.getYear()+1900;
			System.out.println("First month: " + firstMonth + "; first year: "+ firstYear);
			System.out.println("Last month: "+ lastMonth + "; last year: "+ lastYear);
		}
		List<ReportInput> inputs = new ArrayList<>();
		Document report = new Document();
		PdfWriter.getInstance(report, new FileOutputStream("./reports/Months.pdf"));

		report.open();
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
		Paragraph p = new Paragraph("Months", titleFont);

		report.add(p);

		for (int i = firstYear; i <= lastYear; i++) {
			int j; int lastJ;
			if (i == firstYear)
				j = firstMonth;
			else
				j = 1;
			if (i == lastYear)
				lastJ = lastMonth;
			else
				lastJ = 12;

			for (; j <= lastJ; j++) {
				value = i + "-" + (j<10 ? "0" : "") + j;
				setIsAskingForValue(false);
				if (getPressReleasesByDate().equals("foo") && fetchedNotes != null && !fetchedNotes.isEmpty()) {
					System.out.println("******************* *Date: "+value + " ************************");
					GraphHandler.resetInput();
					GraphHandler.graphCreator("Date" , value, fetchedNotes, report);
					ReportInput input = GraphHandler.getInput();
					if (input != null)
						inputs.add(GraphHandler.getInput());
				}
			}

		}
		reportCreator.showChart(inputs, report);
		report.close();

		return "foo";
	}
	@GetMapping("/analyseNewspaper")
	public String analyseByNewspaper() throws FileNotFoundException, DocumentException {
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		if (getAllNewspapers().equals("foo")){ //we're sure it's finished
			System.out.println("Newspapers have been fetched");
		}
		List<ReportInput> newsInputs = new ArrayList<>();
		Document newsReport = new Document();
		if (!isIteratingOverDates) {
			PdfWriter.getInstance(newsReport, new FileOutputStream("./reports/Newspaper.pdf"));
			newsReport.open();
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
			Paragraph p = new Paragraph("Newspaper", titleFont);
			newsReport.add(p);
		}
		//for (Newspaper n : fetchedNewspapers) {
		value =  "Interia";
		//value = n.getName();
		setIsAskingForValue(false);
		if ((getPressReleasesByNews().equals("foo")) && (fetchedNotes != null) && (!fetchedNotes.isEmpty())){
			System.out.println("******************* *Newspaper: "+value + " ************************");
			if (isIteratingOverDates){
				Map<String, Set<PressRelease>> newspaperNotes = new HashMap<String, Set<PressRelease>>();
				//wrzucam notki do list w hashmapie
				for (PressRelease p : fetchedNotes){
					int pMonth = p.getDate().getMonth()+1;
					int pYear = p.getDate().getYear()+1900;
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
				PdfWriter.getInstance(report, new FileOutputStream("./reports/"+value+".pdf"));
				report.open();
				Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,14,BaseColor.BLACK);
				Paragraph p = new Paragraph("Newspaper&date: " + value, titleFont);
				report.add(p);

				for (String d : notesKeySet){
					System.out.println("------> " + d);

					GraphHandler.resetInput();
					GraphHandler.graphCreator("Newspaper&date" , value+"("+d+")", newspaperNotes.get(d), report);
					ReportInput input = GraphHandler.getInput();
					if (input != null) {
						inputs.add(GraphHandler.getInput());
					}
				}
				reportCreator.showChart(inputs, report);
				report.close();

				//remove values
				newspaperNotes = null;
				notesKeySet = null;
				inputs = null;
			}
			else {
				GraphHandler.resetInput();
				GraphHandler.graphCreator("Newspaper" , value, fetchedNotes, newsReport);
				ReportInput input = GraphHandler.getInput();
				if (input != null)
					newsInputs.add(GraphHandler.getInput());
			}
		}
		//}
		if (!isIteratingOverDates){
			reportCreator.showChart(newsInputs, newsReport);
			newsReport.close();
		}
		return "foo";
	}



	@GetMapping("/analyseCountry")
	public String analyseByCountry(){
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		if (getAllCountries().equals("foo")){ //we're sure it's finished
			System.out.println("Countries have been fetched");
		}
		for (Country c : fetchedCountries) {
			value = c.getName();
			setIsAskingForValue(false);
			if (getPressReleasesByCountries().equals("foo") && fetchedNotes != null && !fetchedNotes.isEmpty()) {
				System.out.println("******************* *Country: "+value + " ************************");
				try {
					GraphHandler.graphCreator("Countries", value, fetchedNotes, null);
				} catch (DocumentException e) {
					e.printStackTrace();
				}
			}
		}
		return "foo";
	}
	@GetMapping("/analyseLanguage")
	public String analyseByLanguage(){
		if (pressReleaseRepository == null){
			System.out.println("repository not initialised");
			return "foo";
		}
		if (getAllLanguages().equals("foo")){ //we're sure it's finished
			System.out.println("Languages has been fetched");
		}
		for (Language l : fetchedLanguages) {
			value = l.getName();
			setIsAskingForValue(false);
			if (getPressReleasesByLangs().equals("foo") && fetchedNotes != null && !fetchedNotes.isEmpty()) {
				System.out.println("******************* *Language: "+value + " ************************");
				try {
					GraphHandler.graphCreator("Language", value, fetchedNotes, null);
				} catch (DocumentException e) {
					e.printStackTrace();
				}
			}
		}
		return "foo";
	}

	@GetMapping("/getTags")
	public String getAllTags(){
		if (tagRepository == null){
			System.out.println("Kiepsko");
			return  "foo";
		}
		Set<Tag> tags = (Set<Tag>)tagRepository.findAll();
		System.out.println("Tags:");
		for (Tag t: tags){
			System.out.println(t.getName()+"; notes:");
			for (PressRelease p: t.getPressReleases()){
				System.out.println("\t"+p.getFeed().getNewspaper().getName());
			}
			System.out.println();
		}
		return "foo";
	}
}