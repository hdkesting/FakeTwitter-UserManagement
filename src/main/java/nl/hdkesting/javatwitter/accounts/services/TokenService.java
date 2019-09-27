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
    }

    /**
     * Cleanup all expired tokens by removing them from the database.
     * To be called by a timer-based function.
     */
    public void cleanupExpiredTokens() {
        String query = "DELETE FROM Tokens WHERE ExpiryDate < ?";
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setDate(1, getNow());

            int cnt = statement.executeUpdate();
            Logger.getGlobal().info("Removed " + cnt + " expired tokens.");
        } catch (SQLException ex) {
            Logger.getGlobal().severe(ex.toString());
        }
    }

    /**
     * Creates (and stores) a new token with the default expiry timespan.
     * @param accountId
     * @return
     */
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

    /**
     * Revoke a token by removing it from the database.
     * @param token
     */
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

    public boolean tokenIsValid(String token) {
        String query = "SELECT 1 FROM Tokens WHERE token=? AND expirydate>=?";

        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, token);
            statement.setDate(2, getNow());

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                // found a matching, not-expired token!
                updateExpiry(token, connection);
                return true;
            } else {
                revokeToken(token);
                return false;
            }
        } catch (SQLException ex) {
            Logger.getGlobal().severe(ex.toString());
        }

        return false;
    }

    private void updateExpiry(String token, Connection connection) {
        String query = "UPDATE Tokens SET expirydate = ? WHERE token = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, getNewExpiryDate());
            statement.setString(2, token);

            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
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

    public static Date getNow() {
        return new Date(new java.util.Date().getTime());
    }
}
