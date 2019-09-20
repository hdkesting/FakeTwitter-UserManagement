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
        // arrange
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mail", "unknown@invalid.com");
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
        AccountService svc = new AccountService("jdbc:sqlserver://javatwitterdatabase.database.windows.net:1433;"+
                "database=JavaTwitterAccountDb;user=hdkesting@javatwitterdatabase;password=Geheim01;"+
                "encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
        EmailExists fnc = new EmailExists(svc);

        try {
            final HttpResponseMessage ret = fnc.run(req, context);

            // assert
            assertEquals(ret.getStatus(), HttpStatus.NOT_FOUND);
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }
}
