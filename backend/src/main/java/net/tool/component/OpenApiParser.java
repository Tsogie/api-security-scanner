package net.tool.component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import net.tool.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OpenApiParser {

    private final UrlValidator urlValidator;

    public OpenApiParser(UrlValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    public OpenAPI parseOpenAPI(String urlString) throws Exception {
        String specContent = fetchSpecContent(urlString);
        return parseSpec(specContent);
    }
    /**
     * Helper, Fetch the OpenAPI spec content as a String
     */
    private String fetchSpecContent(String urlString) throws Exception {

        try {
            URL url = new URL(urlString);
            InetAddress resolvedAddress = urlValidator.resolve(url.getHost());
            HttpURLConnection connection = urlValidator.openSafeConnection(url, resolvedAddress, "GET");

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    throw new InvalidUrlException("Spec URL returned HTTP " + responseCode);
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }

            } finally {
                connection.disconnect();
            }
        } catch (InvalidUrlException e){
            throw e;
        } catch (Exception e) {
            throw new InvalidUrlException(
                    "Failed to reach spec URL: " + e.getMessage()
            );
        }
    }

    /**
     * Helper, Parse OpenAPI spec from JSON/YAML string
     */
    private OpenAPI parseSpec(String specContent) {

        // Validate size
        if (specContent.length() > 20_000_000) { // 20MB limit
            throw new RuntimeException("Spec too large");
        }
        try {

            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            ParseOptions options = new ParseOptions();
            options.setResolve(false); // ?
            options.setResolveFully(false); // Resolve only internal refs

            SwaggerParseResult result = parser.readContents(specContent, null, options);

            // Check for parsing errors
            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                log.warn("Parse messages for spec: {}", result.getMessages());
            }
            return result.getOpenAPI();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAPI spec: " + e.getMessage(), e);
        }
    }
}
