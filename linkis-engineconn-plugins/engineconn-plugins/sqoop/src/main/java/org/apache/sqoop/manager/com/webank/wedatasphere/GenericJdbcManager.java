package org.apache.sqoop.manager.com.webank.wedatasphere;

import java.sql.Connection;
import java.sql.SQLException;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.sqoop.SqoopOptions;
import org.apache.sqoop.cli.RelatedOptions;

public class GenericJdbcManager extends SqlManager {


    public static final Log LOG = LogFactory.getLog(
            GenericJdbcManager.class.getName());

    private String jdbcDriverClass;
    private Connection connection;
    private static final String SCHEMA = "schema";

    public GenericJdbcManager(final String driverClass, final SqoopOptions opts) {
        super(opts);

        this.jdbcDriverClass = driverClass;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (null == this.connection) {
            this.connection = makeConnection();
        }

        return this.connection;
    }

    protected boolean hasOpenConnection() {
        return this.connection != null;
    }

    /**
     * Any reference to the connection managed by this manager is nulled.
     * If doClose is true, then this method will attempt to close the
     * connection first.
     * @param doClose if true, try to close the connection before forgetting it.
     */
    public void discardConnection(boolean doClose) {
        if (doClose && hasOpenConnection()) {
            try {
                this.connection.close();
            } catch(SQLException sqe) {
            }
        }

        this.connection = null;
    }

    public void close() throws SQLException {
        super.close();
        discardConnection(true);
    }

    public String getDriverClass() {
        return jdbcDriverClass;
    }

    public String parseExtraScheArgs(String[] args,RelatedOptions opts) throws ParseException {
        // No-op when no extra arguments are present
        if (args == null || args.length == 0) {
            return null;
        }

        // We do not need extended abilities of SqoopParser, so we're using
        // Gnu parser instead.
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine = parser.parse(opts, args, true);

        //Apply parsed arguments
        return applyExtraScheArguments(cmdLine);
    }

    public String applyExtraScheArguments(CommandLine cmdLine) {
        if (cmdLine.hasOption(SCHEMA)) {
            String schemaName = cmdLine.getOptionValue(SCHEMA);
            LOG.info("We will use schema " + schemaName);

            return schemaName;
        }

        return null;
    }
}
