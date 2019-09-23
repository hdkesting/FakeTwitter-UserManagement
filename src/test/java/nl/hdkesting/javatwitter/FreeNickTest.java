package nl.hdkesting.javatwitter;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FreeNickTest {
    private AccountService accountService;

    @BeforeAll
    public void initializeTest() {
        try {
            this.accountService = new AccountService("jdbc:h2:mem:accountdb;" +
                    "INIT=RUNSCRIPT FROM 'classpath:create_account.sql'\\;" +
                    "RUNSCRIPT FROM 'classpath:data_account.sql'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFreeNick_unknownNick() {
        final String nickToTest = "q@q";
        String newnick = performTest(nickToTest);

        assertEquals(nickToTest, newnick);
    }

    @Test
    public void testGetFreeNick_knownNick() {
        final String nickToTest = "testsample";
        String newnick = performTest(nickToTest);

        assertNotEquals(nickToTest, newnick);
        assert newnick.startsWith(nickToTest);
    }

    private String performTest(String nickToTest) {
        // arrange
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

        // act
        try {
            final HttpResponseMessage ret = new GetFreeNickname(this.accountService).run(req, context);

            // assert
            assertEquals(ret.getStatus(), HttpStatus.OK);
            return ret.getBody().toString();
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

        return null;
    }
}
