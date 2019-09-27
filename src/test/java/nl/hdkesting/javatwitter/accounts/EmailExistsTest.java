package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import javax.management.InvalidApplicationException;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(Parameterized.class)
public class EmailExistsTest {
    private AccountService accountService;

    @Parameterized.Parameters(name = "{index}: {0}->{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"unknown@invalid.com", HttpStatus.NOT_FOUND},
                {"sample@example.com", HttpStatus.OK},
                {"", HttpStatus.BAD_REQUEST}});
    }

    @Parameterized.Parameter(0)
    public String emailAddressToTest;

    @Parameterized.Parameter(1)
    public HttpStatus expectedResponseStatus;

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
    public void performTest() {
        Logger.getGlobal().info("Testing address '" + this.emailAddressToTest + "' for status " + this.expectedResponseStatus);

        // ARRANGE
        initializeTest();

        final HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addQueryParameter("mail", this.emailAddressToTest)
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new EmailExists(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
            assertEquals(this.expectedResponseStatus, ret.getStatus());
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }
}
