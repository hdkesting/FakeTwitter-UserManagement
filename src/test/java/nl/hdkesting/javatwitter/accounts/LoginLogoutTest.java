package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.services.TokenService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;

public class LoginLogoutTest {
    private AccountService accountService;
    private TokenService tokenService;

    private static final String SAMPLE_EMAIL = "testsample@invalid.com";
    private static final String SAMPLE_PASSWORD = "Pa$$w0rd";

    //@Test apparently H2 resets frequently, thereby forgetting the registration
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

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addAuthorizeHeader(token)
                .build();

        try {
            final HttpResponseMessage ret = new Logout(this.tokenService).run(req, RequestBuilder.getMockContext());

            assertEquals(HttpStatus.NO_CONTENT, ret.getStatus());
        } catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    private boolean verifyToken(String token, boolean expectSuccess) {
        Logger.getGlobal().info("verifying token " + token);

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addAuthorizeHeader(token)
                .build();

        try {
            final HttpResponseMessage ret = new ValidateLogin(this.tokenService).run(req, RequestBuilder.getMockContext());

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

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addBody("{ \"email\": \"" + SAMPLE_EMAIL + "\"," +
                        "\"password\": \"" + SAMPLE_PASSWORD + "\"" +
                        "}")
                .build();

        try {
            final HttpResponseMessage ret = new Login(this.accountService, this.tokenService).run(req, RequestBuilder.getMockContext());

            Object body = ret.getBody();
            if (body == null) body = "(null)";
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

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addBody("{ \"email\": \"" + SAMPLE_EMAIL + "\"," +
                        "\"password\": \"" + SAMPLE_PASSWORD + "\", " +
                        "\"fullname\": \"Example Name\", " +
                        "\"nickname\": \"somebody\" " +
                        "}")
                .build();

        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, RequestBuilder.getMockContext());

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
