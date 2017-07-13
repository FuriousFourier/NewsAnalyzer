package Analyzer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import Analyzer.model.Feed;
import Analyzer.model.PressRelease;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

/**
 * Created by pawel on 11.07.17.
 */
public interface PressReleaseRepository extends PagingAndSortingRepository<PressRelease, Integer> {
  Set<PressRelease> findByFeed(Feed feed);
	Set<PressRelease> findByTitle(String name);

  @Query("SELECT r FROM PressRelease r WHERE MONTH(r.date)=:month AND YEAR(r.date)=:year")
  List<PressRelease> findByMonthAndYear(@Param("month") String month, @Param ("year") String year);
  @Query("SELECT r FROM PressRelease r ORDER BY r.date ASC")
  List<PressRelease> getSortedByDate();
}
