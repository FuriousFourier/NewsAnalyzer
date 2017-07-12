package pl.edu.agh.Analyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;
import pl.edu.agh.Analyzer.model.PressRelease;
import pl.edu.agh.Analyzer.repository.FeedRepository;
import pl.edu.agh.Analyzer.repository.LanguageRepository;
import pl.edu.agh.Analyzer.repository.NewspaperRepository;
import pl.edu.agh.Analyzer.repository.PressReleaseRepository;

import java.io.*;
import java.time.ZoneId;
import java.util.*;

@Controller
public class AnalysisController {
    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private NewspaperRepository newspaperRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private PressReleaseRepository pressReleaseRepository;

    public List<Language> getAllLanguages(){
      return (List<Language>)languageRepository.findAll();
    }
    public List<Newspaper> getAllNewspapers(){
      return (List<Newspaper>)newspaperRepository.findAll();
    }

    public List<PressRelease> getPressReleases(Date date){
        String month = Integer.toString(date.getMonth()+1);
        String year = Integer.toString(date.getYear());
      return pressReleaseRepository.findByMonthAndYear(month, year);
    }


}