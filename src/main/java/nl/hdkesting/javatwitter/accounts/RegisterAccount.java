package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;

import com.google.gson.Gson;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.services.Encryptor;
import nl.hdkesting.javatwitter.accounts.support.AccountRegistration;

import javax.management.InvalidApplicationException;

public class RegisterAccount {
    private AccountService accountService;
    private static final int MINPASSWORDLENGTH = 6;

    public RegisterAccount(AccountService accountService) {
        Objects.requireNonNull(accountService, "accountService parameter should not be null.");

        this.accountService = accountService;
    }

    public RegisterAccount() throws SQLException {
        this(new AccountService());
        // Fake DI
    }

    @FunctionName("RegisterAccount")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        context.getLogger().fine("HTTP trigger processing a request for RegisterAccount");

        // get JSON body and parse to "account object"
        Optional<String> json = request.getBody();
        if (!json.isPresent()) {
            // 400 BAD REQUEST
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E01 - Please pass an account to register.").build();
        }

        Gson gson = new Gson();
        AccountRegistration newAccount = gson.fromJson(json.get(), AccountRegistration.class);

        // validate account, including password requirements (just "at least x chars"), available email and nick
        if (!isValidEmail(newAccount.getEmail())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E02 - Please pass a valid email address.").build();
        }

        if (!emailIsAvailable(newAccount.getEmail())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E03 - There already is an account for this email address.").build();
        }

        if (!isValidNick(newAccount.getNickname())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E04 - Please pass a valid nickname.").build();
        }

        if (!nicknameIsAvailable(newAccount.getNickname())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E05 - There already is an account with this email nickname.").build();
        }

        if (!isValidPassword(newAccount.getPassword())) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E06 - The password is not acceptable.").build();
        }

        // and finally do it
        context.getLogger().info("Registering new account for " + newAccount.getEmail());
        if (createAccount(newAccount)) {
            return request.createResponseBuilder(HttpStatus.OK).build();
        }

        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("E07 - Something went wrong, possibly the email or nick was just taken.").build();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().length() < 3) {
            return false;
        }

        return email.matches("^[^@]+@[^@]+\\.[a-z]+$");
    }

    private boolean emailIsAvailable(String email) {
        try {
            return !this.accountService.emailExists(email);
        } catch (InvalidApplicationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidNick(String nickname) {
        return nickname != null && nickname.matches("^[a-zA-Z0-9]{3,}$");
    }

    private boolean nicknameIsAvailable(String nickname) {
        try {
            return this.accountService.nicknameIsAvailable(nickname);
        } catch (InvalidApplicationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidPassword(String clearTextPassword) {
        return clearTextPassword != null && clearTextPassword.length() >= MINPASSWORDLENGTH;
    }

    private boolean createAccount(AccountRegistration account) {
        // change cleartext into salted hash
        account.setPassword(Encryptor.encrypt(account.getPassword()));
        return this.accountService.createAccount(account);
    }
}
