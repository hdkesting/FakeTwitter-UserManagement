package nl.hdkesting.javatwitter.accounts.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPool {
    private static final Map<String, HikariDataSource> datasources = new HashMap<>();

    // no instances
    private ConnectionPool() {}

    public static Connection getConnection(String connectionString) throws SQLException {
        return datasources.computeIfAbsent(connectionString, cs -> createDatasource(cs)).getConnection();
    }

    private static HikariDataSource createDatasource(String connectionString) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionString);
        return new HikariDataSource(config);
    }
}
