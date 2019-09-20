package nl.hdkesting.javatwitter.services;

import javax.management.InvalidApplicationException;
import java.sql.*;

public class AccountService {
    private String connectionString;

    public AccountService() {
        this(System.getenv("dbconnstr"));
    }

    public AccountService(String connStr) {
        System.setProperty("java.net.preferIPv6Addresses", "true");
        this.connectionString = connStr;
    }

    public boolean emailExists(String emailAddress) throws InvalidApplicationException {
        String query = "SELECT 1 FROM Accounts WHERE email=?";
//        try {
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            throw new InvalidApplicationException(e);
//        }
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, emailAddress);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return true;
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            throw new InvalidApplicationException(ex);
        }

        return false;
    }
}
