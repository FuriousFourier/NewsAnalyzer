package pl.edu.agh.Analyzer.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import pl.edu.agh.Analyzer.model.PressRelease;

import java.util.Date;
import java.util.List;

/**
 * Created by pawel on 11.07.17.
 */
public interface PressReleaseRepository extends PagingAndSortingRepository<PressRelease, Integer> {

}
