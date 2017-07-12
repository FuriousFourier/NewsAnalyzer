package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Language;

import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */
public interface LanguageRepository extends PagingAndSortingRepository<Language, Integer> {
    Language findByName(String name);
}
