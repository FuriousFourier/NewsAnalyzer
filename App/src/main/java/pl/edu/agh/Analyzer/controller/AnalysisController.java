package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public List<Language> getAllLanguages(){
      return (List<Language>)languageRepository.findAll();
    }
    public List<Newspaper> getAllNewspapers(){
      return (List<Newspaper>)newspaperRepository.findAll();
    }
    public List<Country> getAllCountries(){
      return (List<Country>)countryRepository.findAll();
    }

    public List<PressRelease> getPressReleasesSortedByDate(){
      return pressReleaseRepository.getSortedByDate();
    }
    public List<PressRelease> getPressReleases(String month, String year){
      List<PressRelease> result = pressReleaseRepository.findByMonthAndYear(month, year);
      if (result == null || result.size() < 1){
          System.out.println("Couldn't find current date - all entries will be retrieved");
          result = (List<PressRelease>)pressReleaseRepository.findAll();
      }
        return result;
    }


}