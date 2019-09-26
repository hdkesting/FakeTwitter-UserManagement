package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.services.TokenService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoginLogoutTest {
    private AccountService accountService;
    private TokenService tokenService;

    private static final String SAMPLE_EMAIL = "testsample@invalid.com";
    private static final String SAMPLE_PASSWORD = "Pa$$w0rd";

    @Test
    public void registerLoginValidateAndLogout() {
        initializeTest();

        register();

        try {
            String token = login();

            if (!verifyToken(token, true)) {
                fail();
            } else {
                logout(token);

                assertFalse(verifyToken(token, false));
            }
        } catch (InvalidApplicationException ex) {
            fail();
        }
    }

    private void logout(String token) {
        Logger.getGlobal().info("Logging out");
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TokenService.AUTHORIZE_HEADER, TokenService.AUTHORIZE_PREFIX + token);
        doReturn(headers).when(req).getHeaders();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        // create execution context
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        try {
            final HttpResponseMessage ret = new Logout(this.tokenService).run(req, context);

            assertEquals(HttpStatus.NO_CONTENT, ret.getStatus());
        } catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    private boolean verifyToken(String token, boolean expectSuccess) {
        Logger.getGlobal().info("verifying token " + token);
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(TokenService.AUTHORIZE_HEADER, TokenService.AUTHORIZE_PREFIX + token);
        doReturn(headers).when(req).getHeaders();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        // create execution context
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        try {
            final HttpResponseMessage ret = new ValidateLogin(this.tokenService).run(req, context);

            if (expectSuccess) {
                assertEquals(HttpStatus.OK, ret.getStatus());
            } else {
                assertEquals(HttpStatus.UNAUTHORIZED, ret.getStatus());
            }
            return expectSuccess;
         } catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

        return false;
    }

    private String login() throws InvalidApplicationException {
        Logger.getGlobal().info("Logging in");
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        final Optional<String> queryBody = Optional.of("{ \"email\": \"" + SAMPLE_EMAIL + "\"," +
                "\"password\": \"" + SAMPLE_PASSWORD + "\"" +
                "}");
        doReturn(queryBody).when(req).getBody();

        // create response
        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        // create execution context
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        try {
            final HttpResponseMessage ret = new Login(this.accountService, this.tokenService).run(req, context);

            Object body = ret.getBody();
            if (body == null) body="null";
            Logger.getGlobal().info(body.toString());
            assertEquals(HttpStatus.OK, ret.getStatus());
            String token = ret.getHeader(TokenService.AUTHORIZE_HEADER);
            if (token == null || token.length() <= TokenService.AUTHORIZE_PREFIX.length()) {
                fail();
            } else {
                return token.substring(TokenService.AUTHORIZE_PREFIX.length());
            }
        } catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

        throw new InvalidApplicationException("Assertions failed");
    }

    // register an account
    private void register() {
        Logger.getGlobal().info("Registering");
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        // add body to request
        final Optional<String> queryBody = Optional.of("{ \"email\": \"" + SAMPLE_EMAIL + "\"," +
                "\"password\": \"" + SAMPLE_PASSWORD + "\", " +
                "\"fullname\": \"Example Name\", " +
                "\"nickname\": \"somebody\" " +
                "}");
        doReturn(queryBody).when(req).getBody();

        // create response
        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        // create execution context
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, context);

            assertEquals(HttpStatus.OK, ret.getStatus());
        } catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    private void initializeTest() {
        if (this.accountService == null) {
            try {
                this.accountService = new AccountService(ConnStr.H2());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (this.tokenService == null) {
            try {
                this.tokenService = new TokenService(ConnStr.H2());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
