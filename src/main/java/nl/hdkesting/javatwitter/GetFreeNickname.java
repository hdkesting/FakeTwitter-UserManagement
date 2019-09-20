package nl.hdkesting.javatwitter;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;

import javax.management.InvalidApplicationException;

public class GetFreeNickname {
    private AccountService accountService;

    public GetFreeNickname(AccountService accountService) {
        this.accountService = accountService;
    }

    public GetFreeNickname() {
        this(new AccountService());
        // Fake DI
    }

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

        if (this.accountService.nicknameIsAvailable(nick)) {
            return request.createResponseBuilder(HttpStatus.OK).body(nick).build();
        }

        String freenick = this.accountService.getFreeNickname(nick);
        return request.createResponseBuilder(HttpStatus.OK).body(freenick).build();
    }
}
