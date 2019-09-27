package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import java.util.Objects;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.TokenService;

/**
 * A Function that is called by a timer to remove expired tokens.
 * This prevents the database from filling up with tokens that were not cleaned up by explicitly logging out.
 */
public class CleanupTokens {
    private TokenService tokenService;

    public CleanupTokens(TokenService tokenService) {
        Objects.requireNonNull(tokenService, "accountService parameter should not be null.");

        this.tokenService = tokenService;
    }

    public CleanupTokens() throws SQLException {
        this(new TokenService());
        // Fake DI
    }

    @FunctionName("CleanupTokens")
    public void run(
            @TimerTrigger(schedule = "0 0 1 * *", name="req") String timerInfo,
            ExecutionContext context) {
        this.tokenService.cleanupExpiredTokens();
    }
}
