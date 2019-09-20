package nl.hdkesting.javatwitter;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.services.AccountService;
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

public class EmailExistsTest {

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
        // arrange
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mail", emailToTest);
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
            final HttpResponseMessage ret = new EmailExists().run(req, context);

            // assert
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
