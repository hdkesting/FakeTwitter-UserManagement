package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.TokenService;

import javax.management.InvalidApplicationException;

public class Logout {
    private TokenService tokenService;

    public Logout(TokenService tokenService) {
        Objects.requireNonNull(tokenService, "accountService and tokenService should not be null.");

        this.tokenService = tokenService;
    }

    public Logout() throws SQLException {
        this(new TokenService());
        // Fake DI
    }

    // the function, triggered by a GET or POST HTTP request, with header containing the token to revoke.
    @FunctionName("Logout")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST, HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        Map<String, String> headers = request.getHeaders();
        String token = headers.getOrDefault(TokenService.AUTHORIZE_HEADER, null);

        if (token != null && token.length() > TokenService.AUTHORIZE_PREFIX.length()) {
            token = token.substring(TokenService.AUTHORIZE_PREFIX.length());

            this.tokenService.revokeToken(token);
        }

        return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
    }
}
