package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.services.TokenService;
import nl.hdkesting.javatwitter.accounts.support.LoginDetails;

import javax.management.InvalidApplicationException;

public class Login {
    private AccountService accountService;
    private TokenService tokenService;

    public Login(AccountService accountService, TokenService tokenService) {
        if (accountService == null || tokenService == null) {
            throw new IllegalArgumentException("accountService and tokenService should not be null.");
        }

        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    public Login() throws SQLException {
        this(new AccountService(), new TokenService());
        // Fake DI
    }

    // the function, triggered by a POST HTTP request, with body containing the JSON to use.
    @FunctionName("Login")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        // get JSON body and parse to "account object"
        Optional<String> json = request.getBody();
        if (!json.isPresent()) {
            // 417 EXPECTATION FAILED
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E01 - Please pass login details.").build();
        }

        Gson gson = new Gson();
        LoginDetails details;
        try {
            details = gson.fromJson(json.get(), LoginDetails.class);
        } catch (JsonSyntaxException ex) {
            Logger.getGlobal().severe("Failed to parse JSON: " + json.get());
            Logger.getGlobal().severe(ex.toString());

            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("E02 - Please pass correct login details.").build();
        }

        int accountId = this.accountService.getAccountIdByLogin(details.getEmail(), details.getPassword());
        if (accountId <= 0) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).build();
        }

        String token = this.tokenService.createToken(accountId);
        if (token == null) {
            // some error occurred while storing the token
            return request.createResponseBuilder(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // reply with token in header
        return request.createResponseBuilder(HttpStatus.OK).header(TokenService.AUTHORIZE_HEADER, TokenService.AUTHORIZE_PREFIX + token).build();
    }
}
