package nl.hdkesting.javatwitter;

import java.sql.SQLException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;

import javax.management.InvalidApplicationException;

public class EmailExists {
    private AccountService accountService;

    public EmailExists(AccountService accountService) {
        this.accountService = accountService;
    }

    public EmailExists() throws SQLException {
        this(new AccountService());
        // Fake DI
    }

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
