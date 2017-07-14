package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.repository.*;
import pl.edu.agh.Analyzer.ui.AnalysisHandler;

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

    public static void setIsAskingForValue(boolean val){
        isAskingForValue = val;
    }

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
            System.out.println(c.getName() + "("+c.getTag()+")");
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
          //result = (List<PressRelease>)pressReleaseRepository.findAll();
          return "foo";
      }
        System.out.println("Result: ");
            for (PressRelease pr : result) {
                System.out.println("ID: " + pr.getId() + "; Title: " + pr.getTitle() + "; Content: " + pr.getContent());
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
        //find newspaper
        Newspaper newspaper = newspaperRepository.findByName(title);
        //get feed
        List<PressRelease> notesFromAllFeeds = new ArrayList<>();
        if (newspaper != null) {
            List<Feed> feeds = newspaper.getFeeds();
            //find notes for feed
            List<PressRelease> result;
            for (Feed f : feeds) {
                result = pressReleaseRepository.findByFeed(f);
                if (result != null)
                    notesFromAllFeeds.addAll(result);
            }
        }

        if (notesFromAllFeeds == null || notesFromAllFeeds.size() < 1) {
            System.out.println("Couldn't find current feed");
            //notesFromAllFeeds = (List<PressRelease>) pressReleaseRepository.findAll();
            return "foo";
        }
        System.out.println("Result: ");
            for (PressRelease pr : notesFromAllFeeds) {
                System.out.println("ID: " + pr.getId() + "; Title: " + pr.getTitle() + "; Content: " + pr.getContent());
            }
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
          firstMonth = firstDate.getMonth();
          firstYear = firstDate.getYear()+1900;
          lastMonth  = lastDate.getMonth();
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
            value = (j>10 ? "0" : "") + j + "-" + i;
            setIsAskingForValue(false);
            System.out.println("******************* *Date: "+value + " ************************");
          if (getPressReleasesSortedByDate().equals("foo")) {
              AnalysisHandler.graphCreator(fetchedNotes);
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
        System.out.println("Newspaper has been fetched");
      }
      for (Newspaper n : fetchedNewspapers) {
            value = n.getName();
            setIsAskingForValue(false);
            System.out.println("******************* *Newspaper: "+value + " ************************");
          if (getPressReleasesByNews().equals("foo")) {
              AnalysisHandler.graphCreator(fetchedNotes);
          }
      }
      return "foo";
    }

}