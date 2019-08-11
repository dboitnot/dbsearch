package dbsearch;

import java.util.List;
import java.util.Optional;

public interface SearchConf {
    String getSearchString();
    List<String> getExcludedOwners();
    List<String> getIncludedOwners();
    List<String> getExcludedOwnerPatterns();
    List<String> getExcludedTableNamePatterns();
    Optional<Long> getMaxRowCount();
    Optional<Integer> getMaxResultsPerTable();
}
