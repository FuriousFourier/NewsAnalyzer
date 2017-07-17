package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Country;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by pawel on 12.07.17.
 */

public interface CountryRepository extends PagingAndSortingRepository<Country, Integer> {

    Country findByName(String name);
}
