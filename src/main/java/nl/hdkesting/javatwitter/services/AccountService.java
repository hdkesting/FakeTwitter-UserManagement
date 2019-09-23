package nl.hdkesting.javatwitter.services;

import javax.management.InvalidApplicationException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountService {
    private String connectionString;

    public AccountService() throws SQLException {
        this(System.getenv("dbconnstr"));
    }

    public AccountService(String connStr) throws SQLException {
        System.setProperty("java.net.preferIPv6Addresses", "true");
        this.connectionString = connStr;

        try (Connection connection = DriverManager.getConnection(this.connectionString)) {
            // great, there is connection!
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public boolean emailExists(String emailAddress) throws InvalidApplicationException {
        String query = "SELECT 1 FROM Accounts WHERE email=?";

        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, emailAddress);

            // I just want to know whether there *is* a result
            ResultSet result = statement.executeQuery();
            return result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            throw new InvalidApplicationException(ex);
        }
    }

    public boolean nicknameIsAvailable(String nickname) throws InvalidApplicationException {
        String query = "SELECT 1 FROM Accounts WHERE nickname=?";
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, nickname);

            // I just want to know whether there is *no* result
            ResultSet result = statement.executeQuery();
            return !result.next();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            throw new InvalidApplicationException(ex);
        }
    }

    public String getFreeNickname(String nickname) throws InvalidApplicationException {
        String query = "SELECT nickname FROM Accounts WHERE nickname like ?";
        try (Connection connection = DriverManager.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, nickname + "%");

            ResultSet result = statement.executeQuery();
            List<String> knownnicks = new ArrayList<>();
            while (result.next()) {
                knownnicks.add(result.getString(1));
            }

            int min = 100;
            int range = 900;
            if (knownnicks.size() > 400) {
                min = 10_000;
                range = 90_000;
            }

            while (true) {
                // min and range are chosen to always give 3 digit or 5 digit values. So no need to add prefix 0's
                int number = (int)(Math.random() * range + min);
                String num = Integer.toString(number);
                String potential = nickname + num;

                if (knownnicks.stream().noneMatch(s -> s.equalsIgnoreCase(potential))) {
                    // found one that doesn't exist yet!
                    return potential;
                }
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            throw new InvalidApplicationException(ex);
        }

    }
}
