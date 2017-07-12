package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.Newspaper;

import java.util.List;

/**
 * Created by pawel on 11.07.17.
 */
public interface FeedRepository extends PagingAndSortingRepository<Feed, Integer> {
    List<Feed> findByNewspaper(Newspaper newspaper);

    List<Feed> findByName(String name);
}
