package dbsearch.db;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * dbsearch: Search
 * Created by dboitnot on 12/29/13.
 */
public class Search {
    private static final String COL_SQL = "select c.owner, c.table_name, c.column_name from dba_tab_cols c, dba_tables t where (c.owner = t.owner and c.table_name = t.table_name and t.tablespace_name is not null) and (%s) order by num_rows";

    private final Db db;
    private final String searchString;
    private final SearchListener listener;
    private final boolean includeSys;
    private int maxTableResults = 20;

    public Search(Db db, String searchString, SearchListener listener, boolean includeSys) {
        this.db = db;
        this.searchString = searchString;
        this.listener = listener;
        this.includeSys = includeSys;
    }

    private String getColSql() {
        String where = "(data_type like '%CHAR%')";
        where += String.format(" and (data_length >= %d) and num_rows > 0", searchString.length());

        // TODO: Formalize this.
        where += " and num_rows < 100";

        // TODO: Replace this with a list of excluded schemas.
        if (!includeSys) {
            where += " and c.owner not in ('SYS', 'SYSTEM', 'SCTCVT', 'XDB', 'DBSNMP')";
        }

        // TODO: Formalize this.
        where += " and c.owner not like '%CVT' and c.table_name not like '%CVT'";

        return String.format(COL_SQL, where);
    }

    private void search() throws SQLException {
        Statement stm = null;
        try {
            stm = db.createStatement();

            String sql = getColSql();
            //System.out.println("SQL: " + sql);

            ResultSet res = stm.executeQuery(sql);

            SearchTable searchTable = null;
            while (res.next()) {
                String owner = res.getString(1);
                String table = res.getString(2);

                if (searchTable == null || searchTable.isDifferentTable(owner, table)) {
                    // This is a new table. Store the old one and create a new one.
                    if (searchTable != null)
                        searchInTable(searchTable);
                    searchTable = new SearchTable(owner, table);
                }

                SearchColumn column = new SearchColumn(res.getString(3));
                searchTable.addSearchColumn(column);
            }
        } finally {
            if (stm != null)
                stm.close();
        }
    }

    private void searchInTable(SearchTable table) throws SQLException {
        listener.searchingTable(table);

        String escapedSearchString = StringEscapeUtils.escapeSql(searchString.toLowerCase());

        List<String> searchClauses = new ArrayList<>();
        for (SearchColumn column: table.getSearchColumns()) {
            searchClauses.add(String.format("lower(\"%s\") like '%%%s%%'", column.getName(), escapedSearchString));
        }
        String where = "(" + StringUtils.join(searchClauses, " or ") + ")";

        String sql = String.format("select * from \"%s\".\"%s\" where %s", table.getOwner(), table.getTableName(), where);

        //System.out.println("SQL: " + sql);

        Statement stm = null;
        try {
            stm = db.createStatement();
            ResultSet res = stm.executeQuery(sql);
            int count = 0;
            while (res.next()) {
                SearchResult result = new SearchResult(table, res);
                listener.handleResult(result);
                if (++count > maxTableResults) {
                    listener.tooManyTableResults(table);
                    break;
                }
            }
        } finally {
            if (stm != null)
                stm.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        Db db = null;
        try {
            db = new Db(args[0], args[1], args[2]);
            SearchListener listener = new SearchListener() {
                boolean lastWasTable = true;

                @Override
                public void searchingTable(SearchTable tbl) {
                    if (!lastWasTable)
                        System.out.println();
                    System.out.print("\rSearching table: " + tbl);
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
            };
            Search search = new Search(db, "prod", listener, false);
            search.search();
        } finally {
            if (db != null)
                db.close();
        }
    }
}
