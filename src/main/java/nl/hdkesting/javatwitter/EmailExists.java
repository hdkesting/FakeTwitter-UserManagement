package nl.hdkesting.javatwitter;

import java.sql.SQLException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;

import javax.management.InvalidApplicationException;

/**
 * A Function to test whether there already is an account for the specified email address.
 * It returns a status 200 when the address is known, or 404 when it isn't known.
 */
public class EmailExists {
    private AccountService accountService;

    public EmailExists(AccountService accountService) {
        if (accountService == null) {
            throw new IllegalArgumentException("accountService parameter should not be null.");
        }
        
        this.accountService = accountService;
    }

    public EmailExists() throws SQLException {
        this(new AccountService());
        // Fake DI
    }

    // the function, triggered by a GET HTTP request, with parameter "mail" containing the email address to check.
    @FunctionName("EmailExists")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        // Parse query parameter
        String email = request.getQueryParameters().get("mail");
        context.getLogger().info("HTTP trigger processed a request for EmailExists?mail=" + email);

        if (email == null || email.trim().isEmpty()) {
            // 417 EXPECTATION FAILED
            context.getLogger().warning("No email address was supplied.");
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("Please pass an email address to check.").build();
        }

        if (this.accountService == null) {
            context.getLogger().severe("EmailExists: Account service is NULL!");
            throw new NullPointerException("Account service is NULL!");
        }

        if (this.accountService.emailExists(email)) {
            // 200 OK
            context.getLogger().info("The supplied email address (" + email + ") IS known.");
            return request.createResponseBuilder(HttpStatus.OK).build();
        }

        // 404 NOT FOUND
        context.getLogger().info("The supplied email address (" + email + ") is NOT known.");
        return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
    }
}
