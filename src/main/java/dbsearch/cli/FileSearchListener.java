package dbsearch.cli;

import dbsearch.SearchListener;
import dbsearch.db.SearchResult;
import dbsearch.db.SearchTable;

import java.io.OutputStream;
import java.io.PrintStream;

public class FileSearchListener implements SearchListener {
    private final PrintStream out;

    private final PrintStream err = System.err;
    private final String indent = "  ";

    private String lastTableName = null;

    FileSearchListener(OutputStream out) {
        this.out = new PrintStream(out);
    }

    @Override
    public void searchingTable(SearchTable tbl) {}

    @Override
    public void handleResult(SearchResult result) {
        String tableName = String.format("%s.%s", result.getTableOwner(), result.getTableName());
        if (!tableName.equals(lastTableName)) {
            if (lastTableName != null)
                out.println();

            out.printf("%s:\n", tableName);
            lastTableName = tableName;
        }
        out.printf("%s%s\n", indent, result.getRowValues().toString());
    }

    @Override
    public void permissionError(SearchTable tbl, Exception ex) {
        err.printf("\nPermission Error: %s\n\n", tbl);
    }

    @Override
    public void tooManyTableResults(SearchTable tbl) {
        out.printf("%s...", indent);
    }
}
