package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Country;
import pl.edu.agh.Analyzer.model.Language;
import pl.edu.agh.Analyzer.model.Newspaper;

import java.util.List;

/**
 * Created by pawel on 10.07.17.
 */
public interface NewspaperRepository extends PagingAndSortingRepository<Newspaper, Integer> {
    List<Newspaper> findByLanguage(Language language);

    List<Newspaper> findByCountry(Country country);

    Newspaper findById(Integer id);

}
