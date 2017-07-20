package Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Country;

/**
 * Created by pawel on 12.07.17.
 */

public interface CountryRepository extends PagingAndSortingRepository<Country, Integer> {

    Country findByName(String name);
}
