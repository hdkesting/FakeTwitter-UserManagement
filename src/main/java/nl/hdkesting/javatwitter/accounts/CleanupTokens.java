package nl.hdkesting.javatwitter.accounts;

import java.sql.SQLException;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import nl.hdkesting.javatwitter.accounts.services.TokenService;

public class CleanupTokens {
    private TokenService tokenService;

    public CleanupTokens(TokenService tokenService) {
        if (tokenService == null) {
            throw new IllegalArgumentException("accountService parameter should not be null.");
        }

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
