package nl.hdkesting.javatwitter.accounts.services;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Logger;

public class TokenService {
    private String connectionString;
    private static final int TIMEOUT_MINUTES = 30;

    public static final String AUTHORIZE_HEADER = "Authorize";
    public static final String AUTHORIZE_PREFIX = "Token "; // with trailing space

    public TokenService() throws SQLException {
        this(System.getenv("dbconnstr"));
    }

    public TokenService(String connStr) throws SQLException {
        System.setProperty("java.net.preferIPv6Addresses", "true");
        this.connectionString = connStr;

        try (Connection connection = DriverManager.getConnection(this.connectionString)) {
            // great, there is connection!
            Logger.getGlobal().info("TokenService: There IS a database connection, using: " + this.connectionString);
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public void cleanupExpiredTokens() {
        String query = "DELETE FROM Tokens WHERE ExpiryDate < ?";
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            Date now = new Date(new java.util.Date().getTime());
            statement.setDate(1, now);

            int cnt = statement.executeUpdate();
            Logger.getGlobal().info("Removed " + cnt + " expired tokens.");
        } catch (SQLException ex) {
            Logger.getGlobal().severe(ex.toString());
        }
    }

    public String createToken(int accountId) {
        String query = "INSERT INTO Tokens (token, accountid, expirydate) VALUES (?, ?, ?)";
        String token = generateToken();
        Date expiry = getNewExpiryDate();

        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, token);
            statement.setInt(2, accountId);
            statement.setDate(3, expiry);

            int cnt = statement.executeUpdate();
            return token;
        } catch (SQLException ex) {
            // there is an extremely low probability that I generated an existing token
            Logger.getGlobal().severe(ex.toString());
        }

        // some error
        return null;
    }

    public void revokeToken(String token) {
        String query = "DELETE FROM Tokens WHERE token = ?";

        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, token);

            // I don't care whether it exists and I certainly must ignore an expiry
            statement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getGlobal().severe(ex.toString());
        }
    }

    private static String generateToken() {
        SecureRandom rng = new SecureRandom();
        byte[] token = new byte[30];
        rng.nextBytes(token);

        return Base64.getEncoder().encodeToString(token);
    }

    private static Date getNewExpiryDate() {
        return new Date(new java.util.Date().getTime() + TIMEOUT_MINUTES * 60 * 1000);
    }
}
