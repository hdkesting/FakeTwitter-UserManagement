package nl.hdkesting.javatwitter.services;

import java.sql.*;

public class AccountService {
    private String connectionString;

    public AccountService() {
        this.connectionString = System.getenv("dbconnstr");
        System.setProperty("java.net.preferIPv6Addresses", "true");
    }

    public boolean emailExists(String emailAddress) {
        String query = "SELECT 1 FROM Accounts WHERE email=?";
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, emailAddress);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        catch (SQLException ex) {

        }

        return false;
    }
}
