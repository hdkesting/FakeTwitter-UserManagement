package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterAccountTest {
    private AccountService accountService;

    @Test
    public void registerWithAccount_succeeds() {
        // ARRANGE
        initializeTest();

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addBody("{ \"email\": \"somebodynew@example.com\"," +
                        "\"password\": \"Geheim01\", " +
                        "\"fullname\": \"Some Body\", " +
                        "\"nickname\": \"somebodynew\" " +
                        "}")
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
            Object body = ret.getBody();
            if (body == null) body = "(no body)";
            Logger.getGlobal().info(body.toString());
            assertEquals(HttpStatus.OK, ret.getStatus());
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void registerWithKnownEmail_fails() {
        // ARRANGE
        initializeTest();

        HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addBody("{ \"email\": \"sample@example.com\"," +
                        "\"password\": \"Geheim01\", " +
                        "\"fullname\": \"Some Body\", " +
                        "\"nickname\": \"somebody2\" " +
                        "}")
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
            assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    // I would have preferred @BeforeEach or @BeforeAll - both do NOT work in GitHub CI
    private void initializeTest() {
        if (this.accountService == null) {
            try {
                this.accountService = new AccountService(ConnStr.H2());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
