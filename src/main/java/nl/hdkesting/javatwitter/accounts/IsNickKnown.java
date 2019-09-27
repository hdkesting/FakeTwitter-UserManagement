package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;

import javax.management.InvalidApplicationException;

/**
 * A function to check whether a proposed nickname is already known.
 * It returns a status 200 when it is known, of 404 when it isn't.
 * This function can be used when finding people to follow. It is faster than GetFreeNickname as it doesn't have to find a free one.
 */
public class IsNickKnown {
    private AccountService accountService;

    public IsNickKnown(AccountService accountService) {
        Objects.requireNonNull(accountService, "accountService parameter should not be null.");

        this.accountService = accountService;
    }

    public IsNickKnown() throws SQLException {
        this(new AccountService());
        // Fake DI
    }

    @FunctionName("IsNickKnown")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        // Parse query parameter
        String nick = request.getQueryParameters().get("nick");
        context.getLogger().fine("HTTP trigger processed a request for IsNickKnown?nick=" + nick);

        if (nick == null || nick.trim().isEmpty()) {
            // 400 BAD REQUEST
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a nickname to check.").build();
        }

        if (!this.accountService.nicknameIsAvailable(nick)) {
            return request.createResponseBuilder(HttpStatus.OK).body(nick).build();
        }

        return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
    }
}
