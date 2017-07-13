package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.repository.*;

import java.io.*;
import java.time.ZoneId;
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
        return "foo";
    }


    public List<PressRelease> getPressReleasesSortedByDate(){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return new ArrayList<>();
        }
      return pressReleaseRepository.getSortedByDate();
    }

    public List<PressRelease> getPressReleases(String month, String year){
        if (pressReleaseRepository == null){
            System.out.println("repository not initialised");
            return new ArrayList<>();
        }
      List<PressRelease> result = pressReleaseRepository.findByMonthAndYear(month, year);
      if (result == null || result.size() < 1){
          System.out.println("Couldn't find current date - all entries will be retrieved");
          result = (List<PressRelease>)pressReleaseRepository.findAll();
      }
        return result;
    }


}