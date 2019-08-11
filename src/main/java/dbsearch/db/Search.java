package dbsearch.db;

import dbsearch.SearchConf;
import dbsearch.SearchListener;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * dbsearch: Search
 * Created by dboitnot on 12/29/13.
 */
public class Search {
    private static final String COL_SQL = "select c.owner, c.table_name, c.column_name from all_tab_cols c, all_tables t \n where (c.owner = t.owner and c.table_name = t.table_name and t.tablespace_name is not null) and (%s) order by num_rows";

    private final Db db;
    private final SearchConf conf;
    private final SearchListener listener;

    private Search(Db db, SearchConf conf, SearchListener listener) {
        this.db = db;
        this.conf = conf;
        this.listener = listener;
    }

    private String getColSql() {
        StringBuilder w = new StringBuilder("(data_type like '%CHAR%')");
        w.append(format(" and (data_length >= %d) and num_rows > 0", conf.getSearchString().length()));

        conf.getMaxRowCount().ifPresent(c -> w.append(format(" and num_rows <= %d", c)));

        inList(conf.getExcludedOwners()).ifPresent(l -> w.append(format(" and c.owner not in %s", l)));
        inList(conf.getIncludedOwners()).ifPresent(l -> w.append(format(" and c.owner in %s", l)));

        conf.getExcludedOwnerPatterns().forEach(p -> w.append(format(" and c.owner not like '%s'", p)));
        conf.getExcludedTableNamePatterns().forEach(p -> w.append(format(" and c.table_name not like '%s'", p)));

        return format(COL_SQL, w.toString());
    }

    private static Optional<String> inList(List<String> list) {
        if (list == null)
            return Optional.empty();
        String l = list.stream()
                .map(o -> format("'%s'", o.toUpperCase()))
                .collect(Collectors.joining(", "));
        if (l.length() > 0)
            return Optional.of(format("(%s)", l));
        return Optional.empty();
    }

    private void search() throws SQLException {
        try (Statement stm = db.createStatement()) {

            String sql = getColSql();

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
        }
    }

    private void searchInTable(SearchTable table) throws SQLException {
        listener.searchingTable(table);

        String escapedSearchString = StringEscapeUtils.escapeSql(conf.getSearchString().toLowerCase());

        List<String> searchClauses = new ArrayList<>();
        for (SearchColumn column: table.getSearchColumns()) {
            searchClauses.add(format("lower(\"%s\") like '%%%s%%'", column.getName(), escapedSearchString));
        }
        String where = "(" + StringUtils.join(searchClauses, " or ") + ")";

        String sql = format("select * from \"%s\".\"%s\" where %s", table.getOwner(), table.getTableName(), where);

        //System.out.println("SQL: " + sql);

        try (Statement stm = db.createStatement()) {
            ResultSet res = stm.executeQuery(sql);
            int count = 0;
            long maxTableResults = conf.getMaxRowCount().orElse(Long.MAX_VALUE);
            while (res.next()) {
                SearchResult result = new SearchResult(table, res);
                listener.handleResult(result);
                if (++count > maxTableResults) {
                    listener.tooManyTableResults(table);
                    break;
                }
            }
        } catch (SQLSyntaxErrorException ex) {
            listener.permissionError(table, ex);
        }
    }

    public static void search(Db db, SearchConf conf, SearchListener listener) throws SQLException {
        new Search(db, conf, listener).search();
    }
}
