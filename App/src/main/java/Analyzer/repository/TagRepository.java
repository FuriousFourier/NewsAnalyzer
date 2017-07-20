package Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Tag;

/**
 * Created by pawel on 12.07.17.
 */
public interface TagRepository extends PagingAndSortingRepository<Tag, Integer> {

    Tag findByName(String name);
}
