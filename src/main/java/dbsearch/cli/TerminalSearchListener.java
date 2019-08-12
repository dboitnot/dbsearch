package dbsearch.cli;

import dbsearch.SearchConf;
import dbsearch.SearchListener;
import dbsearch.db.SearchResult;
import dbsearch.db.SearchTable;

import java.io.PrintStream;

import static java.lang.String.format;

public class TerminalSearchListener implements SearchListener {
    private final SearchConf conf;
    private final PrintStream out;
    private final PrintStream err;

    private final String indent = "   ";

    private boolean lastWasTable = true;

    TerminalSearchListener(SearchConf conf) {
        this.conf = conf;

        out = System.out;
        err = System.err;
    }

    @Override
    public void searchingTable(SearchTable tbl) {
        if (!lastWasTable)
            out.println();
        out.printf("\rSearching table: %s \033[K", tbl);
        lastWasTable = true;
    }

    @Override
    public void handleResult(SearchResult result) {
        if (lastWasTable)
            out.println();
        out.printf("%s%s\n", indent, formatResult(result));
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
        out.printf("%s...\n", indent);
    }

    @Override
    public void permissionError(SearchTable tbl, Exception ex) {
        err.printf("\nPermission Error: %s\n", tbl);
    }
}
