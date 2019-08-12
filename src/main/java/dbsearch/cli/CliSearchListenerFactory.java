package dbsearch.cli;

import dbsearch.SearchConf;
import dbsearch.SearchListener;

class CliSearchListenerFactory {
    SearchListener getListener(SearchConf conf) {
        if (isTerminal())
            return new TerminalSearchListener(conf);
        return new FileSearchListener(System.out);
    }

    // Return true if System.out is a terminal rather than a file.
    private static boolean isTerminal() {
        return System.console() != null;
    }
}
