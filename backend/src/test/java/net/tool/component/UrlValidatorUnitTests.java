package net.tool.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Url validator component unit tests")
public class UrlValidatorUnitTests {


    private final UrlValidator urlValidator = new UrlValidator();

    @Test
    @DisplayName("Success: Public IP resolves")
    void resolveTestSuccessPath() throws Exception {

        //Arrange
        InetAddress address = InetAddress.getByName("8.8.8.8");
        //Act
        InetAddress result = urlValidator.resolve("8.8.8.8");
        //Assert
        assertEquals(address, result);
    }

    //TEST-2 FAIL: BLOCKLISTED ADDRESS THROWS EXCEPTION
    @Test
    @DisplayName("Fail: blocklisted address")
    void resolveTestFailBlocklistedAddress() throws Exception {

        //Arrange
        String blocklistedAddress = "127.0.0.1";

        //Act and Assert
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> urlValidator.resolve(blocklistedAddress),
                "Expected exception");
        assertTrue(exception.getMessage().contains("Blocklisted address"));
    }
}
