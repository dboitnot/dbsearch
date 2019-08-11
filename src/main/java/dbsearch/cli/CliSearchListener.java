package dbsearch.cli;

import dbsearch.SearchListener;
import dbsearch.db.SearchResult;
import dbsearch.db.SearchTable;

public class CliSearchListener implements SearchListener {
    private boolean lastWasTable = true;

    @Override
    public void searchingTable(SearchTable tbl) {
        if (!lastWasTable)
            System.out.println();
        System.out.print("\rSearching table: " + tbl + " " + (char)27 + "[K");
        lastWasTable = true;
    }

    @Override
    public void handleResult(SearchResult result) {
        if (lastWasTable)
            System.out.println();
        System.out.println("   " + result);
        lastWasTable = false;
    }

    @Override
    public void tooManyTableResults(SearchTable tbl) {
        System.out.println("   ...");
    }

    @Override
    public void permissionError(SearchTable tbl, Exception ex) {
        System.out.println("\nPermission Error: " + tbl);
    }
}
