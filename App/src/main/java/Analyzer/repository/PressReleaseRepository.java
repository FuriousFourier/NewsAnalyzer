package Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Feed;
import Analyzer.model.PressRelease;

import java.util.Date;
import java.util.Set;

/**
 * Created by pawel on 11.07.17.
 */
public interface PressReleaseRepository extends PagingAndSortingRepository<PressRelease, Integer> {

    Set<PressRelease> findByFeed(Feed feed);

	Set<PressRelease> findByTitle(String name);

/*
	PressRelease findByTitleAndDateAndFeed(String title, Date date, Feed feed);
*/
}
