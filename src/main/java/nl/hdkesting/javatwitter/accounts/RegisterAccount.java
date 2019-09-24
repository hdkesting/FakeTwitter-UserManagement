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
        if (accountService == null) {
            throw new IllegalArgumentException("accountService parameter should not be null.");
        }

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

        context.getLogger().info("HTTP trigger processing a request for RegisterAccount");
        if (this.accountService == null) {
            // shouldn't happen
            context.getLogger().severe("RegisterAccount: Account service is NULL!");
            throw new NullPointerException("Account service is NULL!");
        }

        // get JSON body and parse to "account object"
        Optional<String> json = request.getBody();
        if (!json.isPresent()) {
            // 417 EXPECTATION FAILED
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E01 - Please pass an account to register.").build();
        }

        Gson gson = new Gson();
        AccountRegistration newAccount = gson.fromJson(json.get(), AccountRegistration.class);

        // validate account, including password requirements (just "at least x chars"), available email and nick
        if (!seemsValidEmail(newAccount.getEmail())) {
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E02 - Please pass a valid email address.").build();
        }

        if (!emailIsAvailable(newAccount.getEmail())) {
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E03 - There already is an account for this email address.").build();
        }

        if (!seemsValidNick(newAccount.getNickname())) {
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E04 - Please pass a valid nickname.").build();
        }

        if (!nicknameIsAvailable(newAccount.getNickname())) {
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E05 - There already is an account with this email nickname.").build();
        }

        if (!seemsValidPassword(newAccount.getPassword())) {
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E06 - The password is not acceptable.").build();
        }

        // and finally do it
        if (createAccount(newAccount)) {
            return request.createResponseBuilder(HttpStatus.OK).build();
        }

        return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E07 - Something went wrong, possibly the email or nick was just taken.").build();
    }

    private boolean seemsValidEmail(String email) {
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

    private boolean seemsValidNick(String nickname) {
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

    private boolean seemsValidPassword(String clearTextPassword) {
        return clearTextPassword != null && clearTextPassword.length() >= MINPASSWORDLENGTH;
    }

    private boolean createAccount(AccountRegistration account) {
        // change cleartext into salted hash
        account.setPassword(Encryptor.encrypt(account.getPassword()));
        return this.accountService.createAccount(account);
    }
}
