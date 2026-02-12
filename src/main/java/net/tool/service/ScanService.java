package net.tool.service;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import net.tool.component.OpenApiParser;
import net.tool.component.UrlValidator;
import net.tool.exception.EmptySpecException;
import net.tool.exception.SpecParsingException;
import net.tool.model.ApiEndpoint;
import net.tool.model.Report;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScanService {

    private final UrlValidator urlValidator;
    private final OpenApiParser openApiParser;
    private final SecurityAnalyzer securityAnalyzer;
    private final ReportBuilder reportBuilder;

    public ScanService(UrlValidator urlValidator,
                       OpenApiParser openApiParser,
                       SecurityAnalyzer securityAnalyzer,
                       ReportBuilder reportBuilder) {
        this.urlValidator = urlValidator;
        this.openApiParser = openApiParser;
        this.securityAnalyzer = securityAnalyzer;
        this.reportBuilder = reportBuilder;
    }

    public Report scan(String specUrl, String targetUrl) throws Exception {

        OpenAPI openAPI;

        // validate url reachability (throws InvalidUrlException on failure)
        urlValidator.validateSpecUrl(specUrl);
        urlValidator.validateServerUrl(targetUrl);

        openAPI = openApiParser.parseOpenAPI(specUrl);
        // got openAPI model from result(SwaggerParseResult)
        if (openAPI == null) {
            throw new SpecParsingException("Unable to parse OpenAPI spec", specUrl);
        }
        log.info("\n✓ Ready for security scanning!");

        // check malformed spec
        if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            throw new EmptySpecException(specUrl);
        }

        String title = openAPI.getInfo().getTitle();
        String version = openAPI.getInfo().getVersion();

        // check auth
        List<ApiEndpoint> endpoints = securityAnalyzer.analyze(openAPI);

        // do report
        return reportBuilder.buildReport(endpoints, title, version, openAPI);
    }
}
