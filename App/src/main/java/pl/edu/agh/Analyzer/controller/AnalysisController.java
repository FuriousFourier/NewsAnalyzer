package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.Analyzer.model.*;
import pl.edu.agh.Analyzer.repository.*;
import pl.edu.agh.Analyzer.ui.AnalysisHandler;
import pl.edu.agh.Analyzer.ui.GraphHandler;
import pl.edu.agh.Analyzer.ui.ReportCreator;
import pl.edu.agh.Analyzer.ui.ReportInput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
    private List<PressRelease> fetchedNotes = new ArrayList<>();
    private Date firstDate = new Date();
    private Date lastDate = new Date();
    private static boolean isAskingForValue = false;
    private String value;
    private static boolean isIteratingOverDates = false;

    public static void setIsAskingForValue(boolean val){
        isAskingForValue = val;
    }
    public static void setIsIteratingOverDates(boolean val) { isIteratingOverDates = val; }


    @RequestMapping("/ana")
    public String analysisIndex() {

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

        String month = date.substring(0, 2);
        String year = date.substring(3, 7);
        Integer monthInt = Integer.parseInt(month);
        Integer yearInt = Integer.parseInt(year);

      List<PressRelease> result = pressReleaseRepository.findByMonthAndYear(monthInt, yearInt);
      if (result == null || result.size() < 1){
          System.out.println("Couldn't find current date");
          return "foo";
      }
      int count = 0; //DO LATWIEJSZEGO DEBUGOWANIA!!!
      if (isAskingForValue) {
          System.out.println("Result: ");
          for (PressRelease pr : result) {
              if (count > 10)    //J.W.
                  break;
              System.out.print("ID: " + pr.getId() + "; Tags:");
              for (Tag t: pr.getTags()){
                  System.out.print(t.getName()+", ");
              }
              System.out.print("Country:" + pr.getFeed().getNewspaper().getCountry().getName() + "; Newspaper:" + pr.getFeed().getNewspaper().getName() + "\n");
              count++;
          }
      }
        fetchedNotes = result;
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
        List<PressRelease> notesFromAllFeeds = new ArrayList<>();
        if (newspaper != null) {
            List<Feed> feeds = newspaper.getFeeds();//get feeds
            List<PressRelease> result;
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
        int count = 0;
        if (isAskingForValue) {
          System.out.println("Result: ");
          for (PressRelease pr : notesFromAllFeeds) {
              if (count > 10)    //J.W.
                  break;
              System.out.print("ID: " + pr.getId() + "; Tags:");
              for (Tag t: pr.getTags()){
                  System.out.print(t.getName()+", ");
              }
              System.out.print("Country:" + pr.getFeed().getNewspaper().getCountry().getName() + "; Newspaper:" + pr.getFeed().getNewspaper().getName() + "\n");
              count++;
          }
      }
        fetchedNotes = notesFromAllFeeds;
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
        List<PressRelease> notesFromAllFeeds = new ArrayList<>();
        Language language = languageRepository.findByName(name);
        if(language != null) {
            for (Newspaper n : language.getNewspapers()) {
                if (n != null) {
                    List<Feed> feeds = n.getFeeds();//get feeds
                    List<PressRelease> result;
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
        int count = 0;
        if (isAskingForValue) {
          System.out.println("Result: ");
          for (PressRelease pr : notesFromAllFeeds) {
              if (count > 10)    //J.W.
                  break;
              System.out.print("ID: " + pr.getId() + "; Tags:");
              /*for (Tag t: pr.getTags()){
                  System.out.print(t.getName()+", ");
              }*/
              System.out.print("Country:" + pr.getFeed().getNewspaper().getCountry().getName() + "; Newspaper:" + pr.getFeed().getNewspaper().getName() + "\n");
              count++;
          }
      }
        fetchedNotes = notesFromAllFeeds;
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
        List<PressRelease> notesFromAllFeeds = new ArrayList<>();
        Country country = countryRepository.findByName(name);
        if(country != null) {
            for (Newspaper n : country.getNewspapers()) {
                if (n != null) {
                    List<Feed> feeds = n.getFeeds();//get feeds
                    List<PressRelease> result;
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
        int count = 0;
        if (isAskingForValue) {
          System.out.println("Result: ");
          for (PressRelease pr : notesFromAllFeeds) {
              if (count > 10)    //J.W.
                  break;
              System.out.print("ID: " + pr.getId() + "; Tags:");
              /*for (Tag t: pr.getTags()){
                  System.out.print(t.getName()+", ");
              }*/
              System.out.print("Country:" + pr.getFeed().getNewspaper().getCountry().getName() + "; Newspaper:" + pr.getFeed().getNewspaper().getName() + "\n");
              count++;
          }
      }
      fetchedNotes = notesFromAllFeeds;
        return "foo";
    }

    @GetMapping("/analyseDate")
    public String analyseByDate(){
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
      for (int i = firstYear; i <= lastYear; i++) {
        int j;
        int lastJ;
        if (i == firstYear) 
          j = firstMonth;
        else
          j = 1;
        if (i == lastYear)
          lastJ = lastMonth;
        else 
          lastJ = 12;

        for (; j <= lastJ; j++) {
            //value = (j<10 ? "0" : "") + j + "-" + i;
            value = i + "-" + (j<10 ? "0" : "") + j;
            setIsAskingForValue(false);
          if (getPressReleasesByDate().equals("foo") && fetchedNotes != null && !fetchedNotes.isEmpty()) {
              System.out.println("******************* *Date: "+value + " ************************");
              GraphHandler.graphCreator("Date", value, fetchedNotes);
          }
        }
      }
      return "foo";
    }
    @GetMapping("/analyseNewspaper")
    public String analyseByNewspaper(){
      if (pressReleaseRepository == null){
        System.out.println("repository not initialised");
        return "foo";
      }
      if (getAllNewspapers().equals("foo")){ //we're sure it's finished
        System.out.println("Newspapers have been fetched");
      }
      for (Newspaper n : fetchedNewspapers) {
            value = n.getName();

            setIsAskingForValue(false);
          if ((getPressReleasesByNews().equals("foo")) && (fetchedNotes != null) && (!fetchedNotes.isEmpty())){
              System.out.println("******************* *Newspaper: "+value + " ************************");
              if (!isAskingForValue && isIteratingOverDates){
                  Map<String, List<PressRelease>> newspaperNotes = new HashMap<String, List<PressRelease>>();
                  //wrzucam notki do list w hashmapie
                  for (PressRelease p : fetchedNotes){
                      int pMonth = p.getDate().getMonth()+1;
                      int pYear = p.getDate().getYear()+1900;
                      String date = pYear + "-" + (pMonth>10 ? "" : "0") + pMonth;
                      newspaperNotes.putIfAbsent(date, new ArrayList<PressRelease>());
                      newspaperNotes.get(date).add(p);
                      if (p.getTags()!=null && !p.isEmpty()){
                          System.out.print("ID: " + p.getId() + "; Tags:");
                          for (Tag t: p.getTags()){
                              System.out.print(t.getName()+", ");
                          }
                          System.out.println();
                      }

                  }
                  fetchedNotes =null;
                  SortedSet<String> notesKeySet = new TreeSet<>(newspaperNotes.keySet());
                  //List<ReportInput> inputs = new ArrayList<>();
                  for (String d : notesKeySet){
                      System.out.println("------> " + d);
                      GraphHandler.resetInput();
                      GraphHandler.graphCreator("Newspaper and date" , value+"("+d+")", newspaperNotes.get(d));
                      /*ReportInput input = GraphHandler.getInput();
                      if (input != null)
                        inputs.add(GraphHandler.getInput());*/
                  }
                  //ReportCreator.showChart(inputs);

                  //remove values
                  newspaperNotes = null;
                  notesKeySet = null;
                  //inputs = null;
              }
          }
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
              GraphHandler.graphCreator("Countries", value, fetchedNotes);
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
              GraphHandler.graphCreator("Language", value, fetchedNotes);
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
        List<Tag> tags = (List<Tag>)tagRepository.findAll();
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