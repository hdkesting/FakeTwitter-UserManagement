package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;
import org.junit.jupiter.api.Test;

import javax.management.InvalidApplicationException;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NickKnownTest {
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

        final HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addQueryParameter("nick", nickToTest)
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new IsNickKnown(this.accountService).run(req, RequestBuilder.getMockContext());

            return ret.getStatus() == HttpStatus.OK;
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
            return false;
        }
    }
}
