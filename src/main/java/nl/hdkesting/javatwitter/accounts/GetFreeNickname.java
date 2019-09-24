package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;

import javax.management.InvalidApplicationException;

/**
 * A Function to get a free nickname, based on the supplied value.
 * It returns the value when it is free, or a generated free one.
 */
public class GetFreeNickname {
    private AccountService accountService;

    public GetFreeNickname(AccountService accountService) {
        if (accountService == null) {
            throw new IllegalArgumentException("accountService parameter should not be null.");
        }

        this.accountService = accountService;
    }

    public GetFreeNickname() throws SQLException {
        this(new AccountService());
        // Fake DI
    }

    // The function, triggered by a GET HTTP request, with parameter "nick" containing the proposed nickname.
    @FunctionName("GetFreeNickname")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        // Parse query parameter
        String nick = request.getQueryParameters().get("nick");
        context.getLogger().info("HTTP trigger processed a request for GetFreeNickname?nick=" + nick);

        if (nick == null || nick.trim().isEmpty()) {
            // 417 EXPECTATION FAILED
            return request.createResponseBuilder(HttpStatus.EXPECTATION_FAILED).body("Please pass a nickname to check.").build();
        }

        if (this.accountService == null) {
            context.getLogger().severe("GetFreeNickname: Account service is NULL!");
            throw new NullPointerException("Account service is NULL!");
        }

        if (this.accountService.nicknameIsAvailable(nick)) {
            return request.createResponseBuilder(HttpStatus.OK).body(nick).build();
        }

        String freenick = this.accountService.getFreeNickname(nick);
        return request.createResponseBuilder(HttpStatus.OK).body(freenick).build();
    }
}
