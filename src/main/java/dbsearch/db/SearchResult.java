package dbsearch.db;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * dbsearch: SearchResult
 * Created by dboitnot on 12/29/13.
 */
public class SearchResult {
    private final String tableOwner;
    private final String tableName;
    private final Map<String,String> rowValues = new HashMap<>();

    private SearchResult(String tableOwner, String tableName) {
        this.tableOwner = tableOwner;
        this.tableName = tableName;
    }

    public SearchResult(SearchTable table, ResultSet res) throws SQLException {
        this(table.getOwner(), table.getTableName());
        this.putRowValues(res);
    }

    public String getTableOwner() {
        return tableOwner;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, String> getRowValues() {
        return rowValues;
    }

    private void putRowValues(ResultSet res) throws SQLException {
        ResultSetMetaData metaData = res.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = metaData.getColumnName(i);
            String value;
            try {
                value = res.getString(i);
            } catch (SQLException ex) {
                value = "<?>";
            }
            rowValues.put(key, value);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tableOwner", tableOwner)
                .append("tableName", tableName)
                .append("rowValues", rowValues)
                .toString();
    }
}
