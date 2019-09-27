package nl.hdkesting.javatwitter.accounts;

import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.AccountService;
import nl.hdkesting.javatwitter.accounts.support.ConnStr;
import nl.hdkesting.javatwitter.accounts.support.RequestBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

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

    @ParameterizedTest()
    @CsvSource({"unknown@invalid.com,NOT_FOUND", "sample@example.com,OK", ",BAD_REQUEST"})
    public void performTest(String input, String expected) {
        Logger.getGlobal().info("Testing address '" + input + "' for status " + expected);
        HttpStatus expectedStatus = HttpStatus.valueOf(expected);

        // ARRANGE
        initializeTest();

        final HttpRequestMessage<Optional<String>> req = new RequestBuilder()
                .addQueryParameter("mail", input)
                .build();

        // ACT
        try {
            final HttpResponseMessage ret = new EmailExists(this.accountService).run(req, RequestBuilder.getMockContext());

            // ASSERT
            assertEquals(expectedStatus, ret.getStatus());
        }
        catch (InvalidApplicationException ex) {
            ex.printStackTrace();
            fail();
        }
    }
}
