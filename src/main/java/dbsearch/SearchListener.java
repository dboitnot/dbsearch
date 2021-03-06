package dbsearch;

import dbsearch.db.SearchResult;
import dbsearch.db.SearchTable;

/**
 * dbsearch: SearchListener
 * Created by dboitnot on 12/29/13.
 */
public interface SearchListener {
    void searchingTable(SearchTable tbl);
    void handleResult(SearchResult result);
    void permissionError(SearchTable tbl, Exception ex);
    void tooManyTableResults(SearchTable tbl);
}
