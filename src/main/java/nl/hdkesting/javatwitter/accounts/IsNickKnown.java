package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;

import javax.management.InvalidApplicationException;

// how's this function is going to be used, given that GetFreeNickname either confirms that proposed nickname is available
// or suggests another one?

/**
 * A function to check whether a proposed nickname is already known.
 * It returns a status 200 when it is known, of 404 when it isn't.
 */
public class IsNickKnown {
    private AccountService accountService;

    public IsNickKnown(AccountService accountService) {
        // there's a nice tool Objects.requireNotNull(object, "error message") that does exactly this
        if (accountService == null) {
            throw new IllegalArgumentException("accountService parameter should not be null.");
        }

        this.accountService = accountService;
    }

    public IsNickKnown() throws SQLException {
        // does Azure provide some proper way to inject dependencies?
        this(new AccountService());
        // Fake DI
    }

    @FunctionName("IsNickKnown")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        // Parse query parameter
        String nick = request.getQueryParameters().get("nick");
        context.getLogger().info("HTTP trigger processed a request for IsNickKnown?nick=" + nick);

        if (nick == null || nick.trim().isEmpty()) {
            // 417 EXPECTATION FAILED
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("Please pass a nickname to check.").build();
        }

        if (this.accountService == null) {
            context.getLogger().severe("IsNickKnown: Account service is NULL!");
            throw new NullPointerException("Account service is NULL!");
        }

        if (!this.accountService.nicknameIsAvailable(nick)) {
            return request.createResponseBuilder(HttpStatus.OK).body(nick).build();
        }

        return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
    }
}
