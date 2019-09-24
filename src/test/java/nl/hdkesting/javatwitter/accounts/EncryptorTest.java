package nl.hdkesting.javatwitter.accounts;

import nl.hdkesting.javatwitter.accounts.services.Encryptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptorTest {
    @Test
    public void encryptTwice_givesDifferentResults() {
        String password = "Pa$$w0rd";

        String hash1 = Encryptor.encrypt(password);
        assertNotEquals(password, hash1);

        String hash2 = Encryptor.encrypt(password);
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void encryptedText_canBeVerified() {
        String password = "Pa$$w0rd";
        String hash = Encryptor.encrypt(password);

        assertTrue(Encryptor.validate(password, hash));
    }
}
