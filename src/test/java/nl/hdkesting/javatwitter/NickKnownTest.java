package nl.hdkesting.javatwitter;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import nl.hdkesting.javatwitter.services.AccountService;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.management.InvalidApplicationException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NickKnownTest {
    private AccountService accountService;

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
    public void testGetFreeNick_unknownNick() {
        final String nickToTest = "q@q";
        boolean known = performTest(nickToTest);

        assertFalse(known);
    }

    @Test
    public void testGetFreeNick_knownNick() {
        final String nickToTest = "testsample";
        boolean known = performTest(nickToTest);

        assertTrue(known);
    }

    private boolean performTest(String nickToTest) {
        // ARRANGE
        initializeTest();

        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("nick", nickToTest);
        doReturn(queryParams).when(req).getQueryParameters();

        final Optional<String> queryBody = Optional.empty();
        doReturn(queryBody).when(req).getBody();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // ACT
        try {
            final HttpResponseMessage ret = new IsNickKnown(this.accountService).run(req, context);

            return ret.getStatus() == HttpStatus.OK;
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
            return false;
        }
    }
}
