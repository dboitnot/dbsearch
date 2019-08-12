package dbsearch.cli;

import dbsearch.SearchConf;
import dbsearch.SearchListener;
import dbsearch.db.Db;
import dbsearch.db.Search;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(
        description = "Searches a database for a given string",
        name = "dbsearch", mixinStandardHelpOptions = true, version = "0.1"
)
public class Main implements Callable<Void> {
    private static final String[] SYS_EXCLUDED_OWNERS = { "SYS", "SYSTEM", "XDB", "DBSNMP" };
    private static final String[] CONV_EXCLUDED_OWNERS = { "SCTCVT" };
    private static final String[] CONV_EXCLUDED_OWNER_PATTERNS = { "%CVT" };
    private static final String[] CONV_EXCLUDED_TABLE_NAME_PATTERNS = { "%CVT" };


    @Parameters(index = "0", description = "JDBC URL of the database")
    private String jdbcUrl = null;

    @Parameters(index = "1", description = "substring to search for")
    private String searchString = null;

    @Option(names = {"-u", "--user"}, description = "DB user")
    private String user = null;

    @Option(names = {"-p", "--password"}, description = "DB password", arity = "0..1", interactive = true)
    private String password = null;

    @SuppressWarnings("FieldCanBeLocal")
    @Option(names = {"-m", "--maxHits"}, description = "limit results to maxHits per table (default: ${DEFAULT-VALUE})")
    private int maxResultsPerTable = 10;

    @Option(names = {"-r", "--maxRows"}, description = "exclude tables with more than maxRows, helpful for searching configuration tables")
    private Long maxRowCount = null;

    @Option(names = {"-o", "--owner"}, split = ",", paramLabel = "schema",
            description = "limit search to tables owned by these schemas, separated by commas")
    private List<String> includeOwners = null;

    @Option(names = {"-k", "--excludeOwners"}, split = ",", paramLabel = "schema",
            description = "exclude table owned by these schemas, separated by commas")
    private List<String> excludeOwners = null;

    @SuppressWarnings("FieldCanBeLocal")
    @Option(names = {"-s", "--includeSystem"}, description = "don't exclude system schemas")
    private boolean includeSystem = false;

    @SuppressWarnings("FieldCanBeLocal")
    @Option(names = {"-c", "--includeBct"}, description = "don't exclude Banner Conversion Toolkit tables")
    private boolean includeBct = false;

    private SearchConf searchConf = new SearchConf() {
        // Exclusion lists will be built lazily based on command line arguments
        private List<String> exOwners;
        private List<String> exOwnerPatts;
        private List<String> exTablePatts;

        private void buildLazy() {
            if (exOwners != null)
                return; // Already built
            exOwners = new ArrayList<>();
            exOwnerPatts = new ArrayList<>();
            exTablePatts = new ArrayList<>();

            // If --owners is specified, then we don't do any exclusions
            if (includeOwners != null && includeOwners.size() > 0)
                return;

            if (excludeOwners != null)
                exOwners.addAll(excludeOwners);

            if (!includeSystem)
                exOwners.addAll(Arrays.asList(SYS_EXCLUDED_OWNERS));

            if (!includeBct) {
                exOwners.addAll(Arrays.asList(CONV_EXCLUDED_OWNERS));
                exOwnerPatts.addAll(Arrays.asList(CONV_EXCLUDED_OWNER_PATTERNS));
                exTablePatts.addAll(Arrays.asList(CONV_EXCLUDED_TABLE_NAME_PATTERNS));
            }
        }

        @Override
        public String getSearchString() {
            return searchString;
        }

        @Override
        public List<String> getExcludedOwners() {
            buildLazy();
            return exOwners;
        }

        @Override
        public List<String> getIncludedOwners() {
            return includeOwners;
        }

        @Override
        public List<String> getExcludedOwnerPatterns() {
            buildLazy();
            return exOwnerPatts;
        }

        @Override
        public List<String> getExcludedTableNamePatterns() {
            buildLazy();
            return exTablePatts;
        }

        @Override
        public Optional<Long> getMaxRowCount() {
            return Optional.ofNullable(maxRowCount);
        }

        @Override
        public Optional<Integer> getMaxResultsPerTable() {
            if (maxResultsPerTable < 1)
                return Optional.empty();
            return Optional.of(maxResultsPerTable);
        }
    };

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        System.exit(cmd.execute(args));
    }

    @Override
    public Void call() throws Exception {
        SearchListener listener = new CliSearchListenerFactory().getListener(searchConf);

        Db db = null;
        try {
            db = new Db(jdbcUrl, user, password);
            Search.search(db, searchConf, listener);
        } finally {
            if (db != null)
                db.close();
        }
        return null;
    }
}
