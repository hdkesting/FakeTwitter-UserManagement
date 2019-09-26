package nl.hdkesting.javatwitter.accounts.support;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import nl.hdkesting.javatwitter.accounts.services.TokenService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestBuilder {
    private final Map<String, String> queryParameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String queryBody;

    public RequestBuilder addQueryParameter(String key, String value) {
        this.queryParameters.put(key, value);
        return this;
    }

    public RequestBuilder addBody(String body) {
        this.queryBody = body;
        return this;
    }

    public RequestBuilder addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public RequestBuilder addAuthorizeHeader(String token) {
        return addHeader(TokenService.AUTHORIZE_HEADER, TokenService.AUTHORIZE_PREFIX + token);
    }

    public HttpRequestMessage<Optional<String>> build() {
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        if (!this.queryParameters.isEmpty()) {
            doReturn(this.queryParameters).when(req).getQueryParameters();
        }

        if (!this.headers.isEmpty()) {
            doReturn(this.headers).when(req).getHeaders();
        }

        doReturn(Optional.ofNullable(this.queryBody)).when(req).getBody();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        return req;
    }

    public static ExecutionContext getMockContext() {
        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();
        return context;
    }
}
