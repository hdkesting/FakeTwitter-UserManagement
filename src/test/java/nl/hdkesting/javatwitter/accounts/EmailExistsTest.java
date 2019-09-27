package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;

public class EmailExistsTest {
    private AccountService accountService;

    // NB: do note that testing happens against an in-memory H2 database, while live runs against Azure SqlServer.

    // @BeforeEach or @BeforeAll - both do NOT work in GitHub CI
    private void initializeTest() {
        if (this.accountService == null) {
            try {
                this.accountService = new AccountService(ConnStr.H2());
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
        assert performTest("", HttpStatus.BAD_REQUEST);
    }

    private boolean performTest(String emailToTest, HttpStatus expectedResponse) {
        // ARRANGE
        initializeTest();

        final HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addQueryParameter("mail", emailToTest)
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new EmailExists(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
            assertEquals(expectedResponse, ret.getStatus());
            return true;
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }

        return false;
    }
}
