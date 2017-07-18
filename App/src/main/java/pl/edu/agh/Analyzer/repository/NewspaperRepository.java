package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;

import java.util.Set;

/**
 * Created by pawel on 10.07.17.
 */
public interface NewspaperRepository extends PagingAndSortingRepository<Newspaper, Integer> {
    Set<Newspaper> findByLanguage(Language language);

    Set<Newspaper> findByCountry(Country country);

    Newspaper findByName(String name);


}
