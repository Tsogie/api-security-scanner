package net.tool.component;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenApiParser unit tests")
public class OpenApiParserUnitTests {

    @Mock
    private UrlValidator urlValidator;
    @InjectMocks
    private OpenApiParser openApiParser;

    private static final String SPEC_URL = "https://example.com/api-docs";

    //TEST-1 SUCCESS PATH
    @Test
    @DisplayName("Success: parseOpenAPI()")
    void successPathParseOpenAPI() throws Exception {

        //Arrange
        InetAddress mockAddress = InetAddress.getByName("93.184.216.34");
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(urlValidator.resolve("example.com")).thenReturn(mockAddress);
        when(urlValidator.openSafeConnection(any(URL.class), eq(mockAddress), eq("GET")))
                .thenReturn(mockConnection);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream())
                .thenReturn(getClass().getClassLoader()
                        .getResourceAsStream("specs/valid-spec.yaml"));

        //Act
        OpenAPI response = openApiParser.parseOpenAPI(SPEC_URL);

        //Assert
        assertNotNull(response);
        assertFalse(response.getPaths().isEmpty());
        verify(mockConnection).disconnect();

    }
}
