package net.tool.service;


import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.OpenAPI;
import net.tool.component.OpenApiParser;
import net.tool.exception.EmptySpecException;
import net.tool.exception.SpecParsingException;
import net.tool.model.ApiEndpoint;
import net.tool.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scan Service Unit Tests")
public class ScanServiceUnitTests {

    @Mock
    private SecurityAnalyzer securityAnalyzer;
    @Mock
    private ReportBuilder reportBuilder;
    @Mock
    private OpenApiParser openApiParser;
    @InjectMocks
    private ScanService scanService;

    private static final String SPEC_URL = "https://example.com/api-docs";

    @BeforeEach
    public void setup() {

    }
    //TEST-1 SUCCESS PATH
    @Test
    @DisplayName("Success path- scan")
    public void scanTestSuccessPath() throws Exception {

        //Arrange
        List<ApiEndpoint> endpoints = List.of(mock(ApiEndpoint.class));
        String title = "title";
        String version = "version";
        OpenAPI openAPI = buildOpenAPI(title, version);
        Report report = new Report();

        when(openApiParser.parseOpenAPI(SPEC_URL)).thenReturn(openAPI);
        when(securityAnalyzer.analyze(openAPI)).thenReturn(endpoints);
        when(reportBuilder.buildReport(endpoints, title, version, openAPI))
                .thenReturn(report);

        //Act
        Report result = scanService.scan(SPEC_URL);

        //Assert
        assertNotNull(result);
        assertEquals(report, result);
        verify(openApiParser, times(1)).parseOpenAPI(SPEC_URL);
        verify(securityAnalyzer, times(1)).analyze(openAPI);
        verify(reportBuilder, times(1)).buildReport(endpoints, title, version, openAPI);
    }

    //TEST-2 FAIL WHEN NULL SPEC
    @Test
    @DisplayName("Fail: null spec")
    public void scanTestFailNullSpec() throws Exception {

        //Arrange
        when(openApiParser.parseOpenAPI(SPEC_URL)).thenReturn(null);

        //Act and Assert
        SpecParsingException ex =  assertThrows(
                SpecParsingException.class,
                () -> scanService.scan(SPEC_URL),
                "Expected SpecParsingException exception");

        assertEquals("Unable to parse OpenAPI spec", ex.getMessage());
        verify(openApiParser, times(1)).parseOpenAPI(SPEC_URL);
        verify(securityAnalyzer, never()).analyze(any());
    }

    //TEST-3 FAIL WHEN MALFORMED SPEC - NULL
    @Test
    @DisplayName("Fail: malformed exception, null")
    public void scanTestFailMalformedSpecNull() throws Exception {

        //Arrange
        String title = "title";
        String version = "version";
        OpenAPI openAPI = buildOpenAPI(title, version);
        openAPI.setPaths(null);

        when(openApiParser.parseOpenAPI(SPEC_URL)).thenReturn(openAPI);

        //Act and Assert
        EmptySpecException ex = assertThrows(
                EmptySpecException.class,
                () -> scanService.scan(SPEC_URL),
                "Expected EmptySpecException exception");
        assertEquals("OpenAPI spec contains no paths to analyze", ex.getMessage());
        verify(openApiParser, times(1)).parseOpenAPI(SPEC_URL);
        verify(securityAnalyzer, never()).analyze(any());
    }

    //TEST-4 FAIL WHEN MALFORMED SPEC - EMPTY
    @Test
    @DisplayName("Fail: malformed exception, empty")
    public void scanTestFailMalformedSpecEmpty() throws Exception {

        //Arrange
        String title = "title";
        String version = "version";
        OpenAPI openAPI = new OpenAPI();

        Info info = new Info();
        info.setTitle(title);
        info.setVersion(version);
        openAPI.setInfo(info);

        when(openApiParser.parseOpenAPI(SPEC_URL)).thenReturn(openAPI);

        //Act and Assert
        EmptySpecException ex = assertThrows(
                EmptySpecException.class,
                () -> scanService.scan(SPEC_URL),
                "Expected EmptySpecException exception");
        assertEquals("OpenAPI spec contains no paths to analyze", ex.getMessage());
        verify(openApiParser, times(1)).parseOpenAPI(SPEC_URL);
        verify(securityAnalyzer, never()).analyze(any());
    }

    //TEST-5 SUCCESS PATH WITH FALLBACK TO UNKNOWN
    @Test
    @DisplayName("Success path- unknown fallback")
    public void scanTestSuccessPathUnknownFallback() throws Exception {

        //Arrange
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();
        paths.addPathItem("/api", new PathItem());
        openAPI.setPaths(paths);

        List<ApiEndpoint> endpoints = List.of(mock(ApiEndpoint.class));
        //Report report = new Report();

        when(openApiParser.parseOpenAPI(SPEC_URL)).thenReturn(openAPI);
        when(securityAnalyzer.analyze(openAPI)).thenReturn(endpoints);
        when(reportBuilder.buildReport(eq(endpoints), eq("Unknown"), eq("Unknown"), eq(openAPI)))
                .thenReturn(mock(Report.class));
        //Act
        Report result = scanService.scan(SPEC_URL);

        //Assert
        assertNotNull(result);
        verify(openApiParser, times(1)).parseOpenAPI(SPEC_URL);
        verify(securityAnalyzer, times(1)).analyze(openAPI);
        verify(reportBuilder, times(1))
                .buildReport(endpoints, "Unknown", "Unknown", openAPI);
    }


    //helper
    public OpenAPI buildOpenAPI(String title, String version) {

        OpenAPI openAPI = new OpenAPI();

        Info info = new Info();
        info.setTitle(title);
        info.setVersion(version);
        openAPI.setInfo(info);

        Paths paths = new Paths();
        paths.addPathItem("/api", new PathItem());
        openAPI.setPaths(paths);

        return openAPI;
    }
}


