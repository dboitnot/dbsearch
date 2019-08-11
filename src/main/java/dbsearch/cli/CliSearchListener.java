package dbsearch.cli;

import dbsearch.SearchConf;
import dbsearch.SearchListener;
import dbsearch.db.SearchResult;
import dbsearch.db.SearchTable;

import static java.lang.String.format;

public class CliSearchListener implements SearchListener {
    private final SearchConf conf;

    private boolean lastWasTable = true;

    CliSearchListener(SearchConf conf) {
        this.conf = conf;
    }

    @Override
    public void searchingTable(SearchTable tbl) {
        if (!lastWasTable)
            System.out.println();
        System.out.print(format("\rSearching table: %s \033[K", tbl));
        lastWasTable = true;
    }

    @Override
    public void handleResult(SearchResult result) {
        if (lastWasTable)
            System.out.println();
        System.out.println("   " + formatResult(result));
        lastWasTable = false;
    }

    private String formatResult(SearchResult r) {
        return r.getRowValues().toString()
                .replaceAll(
                        format("(?i)%s", conf.getSearchString()),
                        format("\033[7m%s\033[0m", conf.getSearchString()));
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
