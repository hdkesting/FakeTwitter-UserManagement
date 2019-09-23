package nl.hdkesting.javatwitter;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EmailExistsTest {
    private AccountService accountService;

    // NB: do note that testing happens against an in-memory H2 database, while live runs against Azure SqlServer.

    // @BeforeEach or @BeforeAll - both do NOT work in GitHub CI
    public void initializeTest() {
        if (this.accountService == null) {
            try {
                this.accountService = new AccountService("jdbc:h2:mem:accountdb;" +
                        "INIT=RUNSCRIPT FROM 'classpath:create_account.sql'\\;" +
                        "RUNSCRIPT FROM 'classpath:data_account.sql'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testEmailExists_unknownEmail() throws Exception {
        assert performTest("unknown@invalid.com", HttpStatus.NOT_FOUND);
    }

    @Test
    public void testEmailExists_knownEmail() throws Exception {
        assert performTest("sample@example.com", HttpStatus.OK);
    }

    @Test
    public void testEmailExists_emptyEmail() throws Exception {
        assert performTest("", HttpStatus.EXPECTATION_FAILED);
    }

    private boolean performTest(String emailToTest, HttpStatus expectedResponse) {
        // ARRANGE
        initializeTest();

        // create incoming request
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        // add parameter(s) to request
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mail", emailToTest);
        doReturn(queryParams).when(req).getQueryParameters();

        // add (empty) body to request
        final Optional<String> queryBody = Optional.empty();
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
            final HttpResponseMessage ret = new EmailExists(this.accountService).run(req, context);

            // ASSERT
            assertEquals(ret.getStatus(), expectedResponse);
            return true;
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

        return false;
    }
}
