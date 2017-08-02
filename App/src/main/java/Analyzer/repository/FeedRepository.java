package Analyzer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Feed;
import Analyzer.model.Newspaper;

import java.util.Set;

/**
 * Created by pawel on 11.07.17.
 */
public interface FeedRepository extends PagingAndSortingRepository<Feed, Integer> {
    Set<Feed> findByNewspaper(Newspaper newspaper);

    Feed findByName(String name);
}
