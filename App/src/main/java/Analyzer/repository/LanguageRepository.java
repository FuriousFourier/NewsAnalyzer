package Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Language;

/**
 * Created by pawel on 10.07.17.
 */

public interface LanguageRepository extends PagingAndSortingRepository<Language, Integer> {
    Language findByName(String name);
}
