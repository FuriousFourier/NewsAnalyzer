package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Tag;

import java.util.List;

/**
 * Created by pawel on 12.07.17.
 */
public interface TagRepository extends PagingAndSortingRepository<Tag, Integer> {

    Tag findByName(String name);
}
