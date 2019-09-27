package nl.hdkesting.javatwitter.accounts.services;

import nl.hdkesting.javatwitter.accounts.support.AccountRegistration;

import javax.management.InvalidApplicationException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AccountService {
    private String connectionString;

    public AccountService() throws SQLException {
        this(System.getenv("dbconnstr"));
    }

    public AccountService(String connStr) throws SQLException {
        this.connectionString = connStr;
    }

    public boolean emailExists(String emailAddress) throws InvalidApplicationException {
        String query = "SELECT 1 FROM Accounts WHERE email=?";

        try (Connection connection = ConnectionPool.getConnection(this.connectionString);
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
        try (Connection connection = ConnectionPool.getConnection(this.connectionString);
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

        List<String> knownnicks = new ArrayList<>();

        String query = "SELECT nickname FROM Accounts WHERE nickname like ?";
        try (Connection connection = ConnectionPool.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, nickname + "%");

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                knownnicks.add(result.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new InvalidApplicationException(ex);
        }

        int min = 100;
        int range = 900;
        if (knownnicks.size() > 400) {
            min = 10_000;
            range = 90_000;
        }

        while (true) {
            // min and range are chosen to always give 3 digit or 5 digit values. So no need to add prefix 0's
            int number = (int) (Math.random() * range + min);
            String num = Integer.toString(number);
            String potential = nickname + num;

            if (knownnicks.stream().noneMatch(s -> s.equalsIgnoreCase(potential))) {
                // found one that doesn't exist yet!
                return potential;
            }
        }
    }

    public boolean createAccount(AccountRegistration account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        String query = "INSERT INTO Accounts (email, password, fullname, nickname, latestlogin) VALUES (?,?,?,?,null)";
        try (Connection connection = ConnectionPool.getConnection(this.connectionString);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setString(1, account.getEmail());
            statement.setString(2, account.getPassword());
            statement.setString(3, account.getFullname());
            statement.setString(4, account.getNickname());

            int res = statement.executeUpdate();
            return res == 1;
        } catch (SQLException ex) {
            // assume this is a unique constraint violation on email or nickname
            return false;
        }
    }

    public int getAccountIdByLogin(String email, String cleartextPassword) {
        String queryPassword = "SELECT id, password FROM Accounts WHERE email=?";
        String updateLogin = "UPDATE Accounts SET latestlogin=? WHERE id=?";

        try (Connection connection = ConnectionPool.getConnection(this.connectionString);
             PreparedStatement queryStatement = connection.prepareStatement(queryPassword);
             PreparedStatement updateStatement = connection.prepareStatement(updateLogin);) {

            queryStatement.setString(1, email);

            ResultSet result = queryStatement.executeQuery();
            if (result.next()) {
                // found an email match, but is the password correct?
                int id = result.getInt(1);
                String hashedPassword = result.getString(2);
                if (Encryptor.validate(cleartextPassword, hashedPassword)) {
                    // yes, the password is correct!
                    // so update "latest login"
                    updateStatement.setDate(1, TokenService.getNow());
                    updateStatement.setInt(2, id);
                    updateStatement.executeUpdate();

                    // and return the (positive) account id
                    return id;
                }
            } else {
                Logger.getGlobal().warning("No account found for " + email);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getGlobal().severe(ex.toString());
        }

        // any problems end up here
        return -1;
    }
}
