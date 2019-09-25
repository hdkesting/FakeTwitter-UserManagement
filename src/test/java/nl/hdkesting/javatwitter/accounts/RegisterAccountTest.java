package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RegisterAccountTest {
    private AccountService accountService;

    @Test
    public void registerWithAccount_succeeds() {
        initializeTest();

        // create incoming request
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        // add body to request
        final Optional<String> queryBody = Optional.of("{ \"email\": \"somebody@example.com\"," +
                "\"password\": \"Geheim01\", " +
                "\"fullname\": \"Some Body\", " +
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

        // ACT
        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, context);

            // ASSERT
            assertEquals(HttpStatus.OK, ret.getStatus());
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

    }

    @Test
    public void registerWithKnownEmail_fails() {
        initializeTest();

        // create incoming request
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        // add body to request
        final Optional<String> queryBody = Optional.of("{ \"email\": \"sample@example.com\"," +
                "\"password\": \"Geheim01\", " +
                "\"fullname\": \"Some Body\", " +
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

        // ACT
        try {
            final HttpResponseMessage ret = new RegisterAccount(this.accountService).run(req, context);

            // ASSERT
            assertEquals(HttpStatus.EXPECTATION_FAILED, ret.getStatus());
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
