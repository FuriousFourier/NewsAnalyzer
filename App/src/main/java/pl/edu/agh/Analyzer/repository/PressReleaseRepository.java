package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.Feed;
import pl.edu.agh.Analyzer.model.PressRelease;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by pawel on 11.07.17.
 */
public interface PressReleaseRepository extends PagingAndSortingRepository<PressRelease, Integer> {

    Set<PressRelease> findByFeed(Feed feed);

	Set<PressRelease> findByTitle(String name);
}
