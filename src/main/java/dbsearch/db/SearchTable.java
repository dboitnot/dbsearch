package dbsearch.db;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * dbsearch: SearchTable
 * Created by dboitnot on 12/29/13.
 */
public class SearchTable {
    private final String owner;
    private final String tableName;
    private final List<SearchColumn> searchColumns = new ArrayList<>();

    public SearchTable(String owner, String tableName) {
        if (owner == null)
            throw new RuntimeException("owner cannot be null");
        if (tableName == null)
            throw new RuntimeException("tableName cannot be null");
        this.owner = owner;
        this.tableName = tableName;
    }

    public String getOwner() {
        return owner;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SearchColumn> getSearchColumns() {
        return searchColumns;
    }

    public void addSearchColumn(SearchColumn c) {
        searchColumns.add(c);
    }

    public boolean isSameTable(String owner, String tableName) {
        return this.owner.equals(owner) && this.tableName.equals(tableName);
    }

    public boolean isDifferentTable(String owner, String tableName) {
        return !isSameTable(owner, tableName);
    }

    @Override
    public String toString() {
        return owner + "." + tableName;
    }
}
