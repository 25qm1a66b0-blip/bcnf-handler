package com.bcnf.db;

import com.bcnf.exception.BCNFViolationException;
import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A DBMS middleware layer that intercepts schema creation and validation
 * against a relational database (H2 in-memory or SQLite).
 *
 * <p>Before issuing a CREATE TABLE statement, the middleware validates the
 * proposed schema against BCNF. If a violation is found, a
 * {@link BCNFViolationException} is thrown <em>before</em> the database is
 * modified, acting as a guard that keeps the schema clean from the start.</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public final class BCNFDatabaseMiddleware {

    private final Connection connection;
    private final String url;

    public BCNFDatabaseMiddleware(String url) throws SQLException {
        this.url = url;
        if (url.startsWith("jdbc:sqlite:")) {
            Class.forName("org.sqlite.JDBC");
        } else if (url.startsWith("jdbc:h2:")) {
            Class.forName("org.h2.Driver");
        }
        this.connection = DriverManager.getConnection(url);
    }

    /**
     * Creates a new relation in the database, but only after validating that the
     * proposed schema satisfies BCNF.
     *
     * @param name        the table name
     * @param attributes  the set of columns
     * @param functionalDependencies the FDs that must hold on the relation
     * @throws BCNFViolationException if the schema violates BCNF
     * @throws SQLException            if a database access error occurs
     */
    public void createTable(String name, Set<String> attributes,
                            Set<FunctionalDependency> functionalDependencies)
            throws SQLException {
        LinkedHashSet<String> attrs = new LinkedHashSet<>(attributes);
        LinkedHashSet<FunctionalDependency> fds = new LinkedHashSet<>(functionalDependencies);
        Relation relation = new Relation(name, attrs, fds);

        // Intercept: validate BEFORE creating anything in the DBMS
        com.bcnf.algorithm.BCNFValidator.validate(relation);

        // Only reach here if BCNF is satisfied
        String sql = buildCreateTable(name, attributes);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Builds a CREATE TABLE statement from the relation attributes.
     */
    private static String buildCreateTable(String name, Set<String> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");
        boolean first = true;
        for (String attr : attributes) {
            if (!first) sb.append(", ");
            sb.append(attr).append(" VARCHAR(255)");
            first = false;
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * @return the JDBC connection URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Closes the underlying database connection.
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Factory method that creates an H2 in-memory middleware instance.
     */
    public static BCNFDatabaseMiddleware createH2() throws SQLException {
        return new BCNFDatabaseMiddleware("jdbc:h2:mem:bcnf;DB_CLOSE_DELAY=-1");
    }

    /**
     * Factory method that creates a SQLite file-backed middleware instance.
     */
    public static BCNFDatabaseMiddleware createSQLite(String path) throws SQLException {
        return new BCNFDatabaseMiddleware("jdbc:sqlite:" + path);
    }
}