package dbsearch.db;

/**
 * dbsearch: SearchListener
 * Created by dboitnot on 12/29/13.
 */
public interface SearchListener {
    public void searchingTable(SearchTable tbl);
    public void handleResult(SearchResult result);
    public void tooManyTableResults(SearchTable tbl);
}
