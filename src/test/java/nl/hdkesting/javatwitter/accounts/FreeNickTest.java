package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;

import java.sql.SQLException;
import java.util.*;

import org.junit.jupiter.api.Test;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;

public class FreeNickTest {
    private AccountService accountService;

    // @BeforeEach or @BeforeAll - both do NOT work in GitHub CI
    public void initializeTest() {
        if (this.accountService == null) {
            try {
                this.accountService = new AccountService(ConnStr.H2());
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        // ARRANGE
        initializeTest();

        final HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addQueryParameter("nick", nickToTest)
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new GetFreeNickname(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
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
