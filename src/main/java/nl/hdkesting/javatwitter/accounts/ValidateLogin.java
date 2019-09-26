package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import nl.hdkesting.javatwitter.accounts.services.TokenService;

import javax.management.InvalidApplicationException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class ValidateLogin {
    private TokenService tokenService;

    public ValidateLogin(TokenService tokenService) {
        if (tokenService == null) {
            throw new IllegalArgumentException("accountService and tokenService should not be null.");
        }

        this.tokenService = tokenService;
    }

    public ValidateLogin() throws SQLException {
        this(new TokenService());
        // Fake DI
    }

    // the function, triggered by a POST HTTP request, with body containing the JSON to use.
    @FunctionName("ValidateLogin")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws InvalidApplicationException {

        Map<String, String> headers = request.getHeaders();
        String token = headers.getOrDefault(TokenService.AUTHORIZE_HEADER, "");

        if (token != null && token.length() > TokenService.AUTHORIZE_PREFIX.length()) {
            token = token.substring(TokenService.AUTHORIZE_PREFIX.length());

            if (this.tokenService.tokenIsValid(token)) {
                return request.createResponseBuilder(HttpStatus.OK).build();
            }
        }

        return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).build();
    }
}
