package net.tool.controller;

import io.swagger.v3.oas.models.OpenAPI;
import net.tool.component.OpenApiParser;
import net.tool.service.ReportBuilder;
import net.tool.dto.RequestDto;
import net.tool.dto.ResponseDto;
import net.tool.component.UrlValidator;
import net.tool.model.ApiEndpoint;
import net.tool.model.Report;
import net.tool.service.SecurityAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/scan")
public class ScanController {

    private final UrlValidator urlValidator;
    private final OpenApiParser openApiParser;
    private final SecurityAnalyzer securityAnalyzer;
    private final ReportBuilder reportBuilder;

    public ScanController(UrlValidator urlValidator,
                          OpenApiParser openApiParser,
                          SecurityAnalyzer securityAnalyzer,
                          ReportBuilder reportBuilder) {
        this.urlValidator = urlValidator;
        this.openApiParser = openApiParser;
        this.securityAnalyzer = securityAnalyzer;
        this.reportBuilder = reportBuilder;
    }
    @PostMapping("/validate")
    public ResponseEntity<ResponseDto> validate(@RequestBody RequestDto request) {

        // get urls from request
        String specUrl = request.getBaseUrl();
        String targetUrl = request.getTargetUrl();

        // validate url reachability
        if(!urlValidator.isSpecUrlValid(specUrl)){
            return ResponseEntity.badRequest()
                    .body(ResponseDto.error("OpenAPI spec URL is not reachable: " + specUrl));
        }
        if(!urlValidator.isServerReachable(targetUrl)){
            return ResponseEntity.badRequest()
                    .body(ResponseDto.error("Target server is not reachable: " + targetUrl));
        }

        OpenAPI openAPI;
        try {
            openAPI = openApiParser.parseOpenAPI(specUrl);
            // got openAPI model from result(SwaggerParseResult)
            if (openAPI == null) {
                return ResponseEntity.badRequest()
                        .body(ResponseDto.error("Unable to parse OpenAPI spec"));
            }
            System.out.println("\n✓ Ready for security scanning!");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.error("Failed to parse OpenAPI spec: " + e.getMessage()));
        }

        String title = openAPI.getInfo().getTitle();
        String version = openAPI.getInfo().getVersion();

        // check auth
        List<ApiEndpoint> endpoints = securityAnalyzer.analyze(openAPI);

        // do report
        Report report = reportBuilder.buildReport(endpoints, title, version);

        return ResponseEntity.ok().body(ResponseDto.success(report));
    }
}
