# dbsearch - Search an Oracle database for strings

This project is distributed as an "uberjar" with all dependencies included. To
run:

```bash
java -jar dbsearch-<release>.jar
```

```
Usage: dbsearch [-chsV] [-p[=<password>]] [-m=<maxResultsPerTable>]
                [-r=<maxRowCount>] [-u=<user>] [-k=schema[,schema...]]...
                [-o=schema[,schema...]]... <jdbcUrl> <searchString>
Searches a database for a given string
      <jdbcUrl>         JDBC URL of the database
      <searchString>    substring to search for
  -c, --includeBct      don't exclude Banner Conversion Toolkit tables
  -h, --help            Show this help message and exit.
  -k, --excludeOwners=schema[,schema...]
                        exclude table owned by these schemas, separated by
                          commas
  -m, --maxHits=<maxResultsPerTable>
                        limit results to maxHits per table (default: 10)
  -o, --owner=schema[,schema...]
                        limit search to tables owned by these schemas,
                          separated by commas
  -p, --password[=<password>]
                        DB password
  -r, --maxRows=<maxRowCount>
                        exclude tables with more than maxRows, helpful for
                          searching configuration tables
  -s, --includeSystem   don't exclude system schemas
  -u, --user=<user>     DB user
  -V, --version         Print version information and exit.
```
